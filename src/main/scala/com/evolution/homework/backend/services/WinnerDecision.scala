package com.evolution.homework.backend.services

import cats.effect.IO
import cats.implicits.toTraverseOps
import com.evolution.homework.backend.CardGameDecision.Fold
import com.evolution.homework.backend.repository.InMemorySimpleCrudPlayers
import com.evolution.homework.backend.rules.GameRules
import com.evolution.homework.backend.{Card, CardGameDecision, Player}

object WinnerDecision {
  def showDownAndResult(decisions: Map[Player, (CardGameDecision, Set[Card])], inMemorySimpleCrudPlayers: InMemorySimpleCrudPlayers[IO], gameRules: GameRules): IO[Unit] = {
    val (playersWhoHaveFoldedMap,playersWhoHavePlayMap)  = decisions.partition { case (_, decision) => decision._1 == Fold}
    val (playersWhoHaveFolded,playersWhoHavePlay) = (playersWhoHaveFoldedMap.map{ case (player, dec) => player -> dec._2}, playersWhoHavePlayMap.map{ case (player, dec) => player -> dec._2})
    if (playersWhoHavePlay.isEmpty) {
      for {
        players <- IO.pure(playersWhoHaveFolded.keySet.toList)
        _ <- players.traverse(pl => updateTokens(pl, -gameRules.tokensForEveryoneFolds, inMemorySimpleCrudPlayers))
      } yield ()
      // everyone folded
    } else if (playersWhoHavePlay.keySet.size == 1) {
      // only one play
      for {
        players <- IO.pure(playersWhoHaveFolded.keySet.toList)
        _ <- players.traverse(pl => updateTokens(pl, -gameRules.tokensForOnePlayAndTheOthersFolds, inMemorySimpleCrudPlayers))
        _ <- updateTokens(playersWhoHavePlay.keySet.head, gameRules.tokensForOnePlayAndTheOthersFolds, inMemorySimpleCrudPlayers)
      } yield ()
    } else {

        // at least 2 played, find the possible winner
        val playersWithOrderedCards = playersWhoHavePlay.map { case (player, cards) => player -> cards.toList.sortBy(_.rank.intValue)}
        val possibleWinner = whoWin(playersWithOrderedCards)

        possibleWinner match {
          case Some(winner) =>
            val losers = playersWhoHavePlay.filter{ case (player, _) => player != winner}.keySet.toList
            for {
              players <- IO.pure(playersWhoHaveFolded.keySet.toList)
              _ <- players.traverse(pl => updateTokens(pl, -gameRules.tokensForEveryoneFolds, inMemorySimpleCrudPlayers))
              _ <- losers.traverse(pl => updateTokens(pl, -gameRules.tokensForMoreThanOnePlays, inMemorySimpleCrudPlayers))
              _ <- updateTokens(winner, gameRules.tokensForMoreThanOnePlays, inMemorySimpleCrudPlayers)
            } yield()
          case None => for {
            players <- IO.pure(playersWhoHaveFolded.keySet.toList)
            _ <- players.traverse(pl => updateTokens(pl, -gameRules.tokensForEveryoneFolds, inMemorySimpleCrudPlayers))
          } yield()
        }
    }
  }

  private def whoWin( players: Map[Player, List[Card]]): Option[Player] = {
    if (players.isEmpty || players.keySet.size > 1 && players.values.head.isEmpty) None
    else if (players.keySet.size == 1) Some(players.keySet.head)
    else {
      val playersWithHighestCardRank = players.map { case (player, cards) => (player , cards.head)}
      val findTheHighestCard = playersWithHighestCardRank.values.toList.sortBy(- _.rank.intValue).head
      val newPlayers = playersWithHighestCardRank.filter { case (_, card) => card.rank.intValue == findTheHighestCard.rank.intValue}.keySet
      val newPlayersWithCard: Map[Player, List[Card]] = newPlayers.map{ player => player -> players.get(player).get.tail }.toMap
      whoWin(newPlayersWithCard)
    }
  }

  private def updateTokens(player: Player, value: Int, inMemorySimpleCrudPlayers: InMemorySimpleCrudPlayers[IO]): IO[Unit] = for {
    playerAssetOpt <- inMemorySimpleCrudPlayers.find(player)
    playerAsset = playerAssetOpt.get
    currentTokes = playerAsset.tokens
    _ <- inMemorySimpleCrudPlayers.add(player, playerAsset.copy(tokens =  currentTokes.copy(amount = currentTokes.amount + value) ))
  } yield()
}
