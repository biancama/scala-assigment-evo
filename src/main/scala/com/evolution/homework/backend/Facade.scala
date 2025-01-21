package com.evolution.homework.backend

import cats.effect.IO
import com.evolution.homework.backend.GameType.{DoubleCardGame, SingleCardGame}
import com.evolution.homework.backend.model.{Game, PlayerAsset}
import com.evolution.homework.backend.repository.{InMemorySimpleCrudGames, InMemorySimpleCrudPlayers}
import com.evolution.homework.backend.rules.{DoubleGameCardGameRule, GameRules, SingleGameCardGameRule}
import com.evolution.homework.backend.services.WinnerDecision

trait Facade {

  /** @return   The balance of "tokens" that a player has. The initial balance should be 0. */
  def getPlayerTokens(player: Player): IO[Tokens]

  /** Requests that a player joins a new game of a particular game type.
    *
    * @return    `GameId` irregardless if there were enough or not enough players to start a game.
    */
  def joinGame(idGenerator: GameIdGenerator, player: Player, gameType: GameType): IO[GameId]

  /** @return   The player's cards if the game is in progress, or `None` if the game is not in progress. */
  def getPlayerCards(gameId: GameId, player: Player): IO[Option[Set[Card]]]

  /** Called to allow the implementation to do the card dealing step. You can assume that there will not be
    *  multiple concurrent invocations of this method, and rely that this method will be called after
    *  `joinGame` has been called for all players.
    */
  def dealCardsForGame(dealer: Dealer, gameId: GameId): IO[Unit]

  /** Applies a player's decision for a game in progress. If the game finishes upon this decision, the
    * implementation should apply the results to player balances.
    */
  def makeDecision(gameId: GameId, player: Player, decision: CardGameDecision): IO[Unit]
}

object Facade {

  private val gameRules: Map[GameType, GameRules] = Map(
    SingleCardGame -> new SingleGameCardGameRule,
    DoubleCardGame -> new DoubleGameCardGameRule
  )

  // TODO:  Replace with your own implementation.
  def create: IO[Facade] = IO {
    val inMemoryPlayersDb = InMemorySimpleCrudPlayers.apply[IO]
    val inMemoryGamesDb = InMemorySimpleCrudGames.apply[IO]
    new Facade {
      /** @return The balance of "tokens" that a player has. The initial balance should be 0. */
      override def getPlayerTokens(player: Player): IO[Tokens] = for {
        playerInDb <- inMemoryPlayersDb.find(player)
        tokens = playerInDb match {
          case Some(playerAsset) => playerAsset.tokens
          case None => Tokens.zero
        }
      } yield (tokens)

      /** Requests that a player joins a new game of a particular game type.
       *
       * @return `GameId` irregardless if there were enough or not enough players to start a game.
       */
      override def joinGame(idGenerator: GameIdGenerator, player: Player, gameType: GameType): IO[GameId] =
        for {
          id <- idGenerator.generate
          _ <- setInitialTokens(player)
          game <- findGame(id)
          _ <- if (game.isDefined) {
            addNewPlayerToTheGame(id, game.get, player)
          } else {
            storeGame(id, gameType, player)
          }
        } yield (id)

      private def findGame(gameId: GameId): IO[Option[Game]] = inMemoryGamesDb.find(gameId)

      private def setInitialTokens(player: Player) = inMemoryPlayersDb.add(player, PlayerAsset(Tokens.zero))

      private def storeGame(id: GameId, gameType: GameType, player: Player) = inMemoryGamesDb.add(id, Game(gameType, Map(player -> Set.empty)))
      private def addNewPlayerToTheGame(id: GameId, game: Game, player: Player) = {
        val currentPlayerDeals = game.deals
        val newGame = game.copy(deals = currentPlayerDeals.updated(player, Set.empty))
        inMemoryGamesDb.add(id, newGame)
      }


      /** @return The player's cards if the game is in progress, or `None` if the game is not in progress. */
      override def getPlayerCards(gameId: GameId, player: Player): IO[Option[Set[Card]]] = for {
        game <- findGame(gameId)
        cards <- if (game.isDefined) {
          fetchCards(game.get, player)
        } else {
          IO.pure(None)
        }
      } yield (cards)

      private def fetchCards(game: Game, player: Player) = game.deals.get(player) match {
        case Some(s) if (!s.isEmpty) => IO.pure(Some(s))
        case _ => IO.pure(None)
      }
      /** Called to allow the implementation to do the card dealing step. You can assume that there will not be
       * multiple concurrent invocations of this method, and rely that this method will be called after
       * `joinGame` has been called for all players.
       */
      override def dealCardsForGame(dealer: Dealer, gameId: GameId): IO[Unit] = for {
        game <- findGame(gameId)
        players = game.get.deals.keySet
        newDeal <- dealer.deal(gameId, game.get.gameType.cardsPerPlayer, players)
        _ <- updateGame(newDeal, game.get, gameId)
      } yield ()

      private def updateGame(newDeal: Map[Player, Set[Card]], game: Game, gameId: GameId) = {
        val newGame = game.copy(deals = newDeal)
        inMemoryGamesDb.add(gameId, game = newGame)
      }


      /** Applies a player's decision for a game in progress. If the game finishes upon this decision, the
       * implementation should apply the results to player balances.
       */
      override def makeDecision(gameId: GameId, player: Player, decision: CardGameDecision): IO[Unit] = for {
        game <- inMemoryGamesDb.find(gameId)
        decs <- addNewDecisionToTheGame(gameId, game.get, player, decision)
        gameAfterTheNewDecision <- inMemoryGamesDb.find(gameId)
        _ <- if (isGameReady(decs, gameAfterTheNewDecision.get)) {
          val gameDecisions: Map[Player, (CardGameDecision, Set[Card])] = createGameDecision(gameAfterTheNewDecision.get)
          WinnerDecision.showDownAndResult(gameDecisions, inMemoryPlayersDb, findGameRules(gameAfterTheNewDecision.get))
        } else {
          IO()
        }
      } yield ()

      private def findGameRules(game:Game): GameRules = gameRules.get(game.gameType).get
      
      private def addNewDecisionToTheGame(gameId: GameId, game: Game, player: Player, decision: CardGameDecision) = {
        val decs = game.decisions
        val newGame = game.copy(decisions = decs.updated(player, decision))
        for {
          _ <- inMemoryGamesDb.add(gameId, newGame)
        } yield (decs)
      }

      private def isGameReady(decisions: Map[Player, CardGameDecision], game: Game)= decisions.keySet.size >= game.gameType.playerPerGame - 1

      private def createGameDecision(game: Game): Map[Player, (CardGameDecision, Set[Card])] =
        game.deals.map { case (player, cards) =>
          val decision = game.decisions.get(player).get
          player -> (decision, cards)
        }
    }
  }
}
