package com.evolution.homework.backend

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.implicits._
import com.evolution.homework.backend.CardGameDecision.{Fold, Play}
import com.evolution.homework.backend.GameType.{DoubleCardGame, SingleCardGame}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers._

import java.util.UUID

abstract class FacadeTest(facade: IO[Facade]) extends AsyncFreeSpec with AsyncIOSpec {
  private def scenario(
    gameType: GameType,
    cA: Set[Card],
    cB: Set[Card],
    decisionA: CardGameDecision,
    decisionB: CardGameDecision,
    tokensA: Int,
    tokensB: Int,
  ): IO[Unit] = {
    val A = Player("A")
    val B = Player("B")

    val dealer = new Dealer {
      override def deal(gameId: GameId, cardsPerPlayer: Int, players: Set[Player]): IO[Map[Player, Set[Card]]] = {
        require(cardsPerPlayer == cA.size)
        require(cardsPerPlayer == cB.size)

        IO.pure(
          Map(
            A -> cA,
            B -> cB,
          )
        )
      }
    }

    val gameId = GameId(UUID.randomUUID())
    val idGenerator = new GameIdGenerator {
      override def generate: IO[GameId] = IO.pure(gameId)
    }

    for {
      facade <- facade

      startTokensA <- facade.getPlayerTokens(A)
      startTokensB <- facade.getPlayerTokens(B)

      // initially, no cards are dealt as no one has joined the game
      cardsA <- facade.getPlayerCards(gameId, A)
      _ = cardsA shouldBe none
      cardsB <- facade.getPlayerCards(gameId, B)
      _ = cardsB shouldBe none

      // one player joining the game is not enough for the game to start
      aJoinedGameId <- facade.joinGame(idGenerator, A, gameType)
      cardsA <- facade.getPlayerCards(gameId, A)
      _ = cardsA shouldBe none
      cardsB <- facade.getPlayerCards(gameId, B)
      _ = cardsB shouldBe none

      // two players joining the game is enough for the game to start, cards are dealt
      bJoinedGameId <- facade.joinGame(idGenerator, B, gameType)
      _ = aJoinedGameId shouldEqual bJoinedGameId
      _ <- facade.dealCardsForGame(dealer, gameId)

      cardsA <- facade.getPlayerCards(gameId, A)
      _ = cardsA shouldBe cA.some
      cardsB <- facade.getPlayerCards(gameId, B)
      _ = cardsB shouldBe cB.some

      _ <- facade.makeDecision(gameId, A, decisionA)

      // one decision is not enough for the game to end
      intermediateTokensA <- facade.getPlayerTokens(A)
      _ = intermediateTokensA shouldEqual startTokensA
      intermediateTokensB <- facade.getPlayerTokens(B)
      _ = intermediateTokensB shouldEqual startTokensB

      _ <- facade.makeDecision(gameId, B, decisionB)
      // both decisions are made, the game can end and winnings calculated and applied to balances

      // check that tokens were rightly calculated as game is finished
      endTokensA <- facade.getPlayerTokens(A)
      _ = (endTokensA.amount - startTokensA.amount) shouldEqual tokensA
      endTokensB <- facade.getPlayerTokens(B)
      _ = (endTokensB.amount - startTokensB.amount) shouldEqual tokensB
    } yield ()
  }

  private val As = Card(Rank.Ace, Suit.Spades)
  private val Ac = Card(Rank.Ace, Suit.Clubs)
  private val Js = Card(Rank.Jack, Suit.Spades)
  private val Jc = Card(Rank.Jack, Suit.Clubs)
  private val Ah = Card(Rank.Ace, Suit.Hearts)
  private val Th = Card(Rank.Ten, Suit.Hearts)

  "SingleCardGame" - {
    "both fold" in scenario(SingleCardGame, Set(As), Set(Th), Fold, Fold, -1, -1)
    "one plays" in scenario(SingleCardGame, Set(As), Set(Th), Fold, Play, -3, +3)
    "both play" in scenario(SingleCardGame, Set(As), Set(Th), Play, Play, +10, -10)
    "draw" in scenario(SingleCardGame, Set(As), Set(Ac), Play, Play, 0, 0)
  }

  "DoubleCardGame" - {
    "both fold" in scenario(DoubleCardGame, Set(As, Js), Set(Ah, Th), Fold, Fold, -2, -2)
    "one plays" in scenario(DoubleCardGame, Set(As, Js), Set(Ah, Th), Fold, Play, -5, +5)
    "both play" in scenario(DoubleCardGame, Set(As, Js), Set(Ah, Th), Play, Play, +20, -20)
    "draw" in scenario(DoubleCardGame, Set(As, Js), Set(Ac, Jc), Play, Play, 0, 0)
  }
}

class ImplementationFacadeTest extends FacadeTest(Facade.create)
