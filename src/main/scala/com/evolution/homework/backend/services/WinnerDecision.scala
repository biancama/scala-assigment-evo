package com.evolution.homework.backend.services

import cats.effect.IO
import cats.effect.syntax._
import cats.implicits.toTraverseOps
import com.evolution.homework.backend.CardGameDecision.Fold
import com.evolution.homework.backend.repository.InMemorySimpleCrudPlayers
import com.evolution.homework.backend.{Card, CardGameDecision, Player}

object WinnerDecision {
  def showDownAndResult(decisions: Map[Player, (CardGameDecision, Set[Card])], inMemorySimpleCrudPlayers: InMemorySimpleCrudPlayers[IO]): IO[Unit] = {
    val (playersWhoHaveFoldedMap,playersWhoHavePlayMap)  = decisions.partition { case (_, decision) => decision._1 == Fold}
    val (playersWhoHaveFolded,playersWhoHavePlay) = (playersWhoHaveFoldedMap.map{ case (player, dec) => player -> dec._2}, playersWhoHavePlayMap.map{ case (player, dec) => player -> dec._2})
    if (playersWhoHavePlay.isEmpty) {
      for {
        players <- IO.pure(playersWhoHaveFolded.keySet.toList)
        _ <- players.traverse(pl => updateTokens(pl, -1, inMemorySimpleCrudPlayers))
      } yield ()
      // everyone folded
    } else if (playersWhoHavePlay.keySet.size == 1) {
      // only one play
      for {
        players <- IO.pure(playersWhoHaveFolded.keySet.toList)
        _ <- players.traverse(pl => updateTokens(pl, -3, inMemorySimpleCrudPlayers))
        _ <- updateTokens(playersWhoHavePlay.keySet.head, 3, inMemorySimpleCrudPlayers)
      } yield ()
    } else {

        // at least 2 played, find the possible winner
        val playersWithOrderedCards = playersWhoHavePlay.map { case (player, cards) => player -> cards.toList.sortBy(_.rank.intValue)}
        val possibleWinner = whoWin(playersWithOrderedCards)

        possibleWinner match {
          case Some(winner) =>
            val losers = playersWhoHavePlay.filter{ case (player, _) => player != winner}.keySet.toList
            for {
              _ <- losers.traverse(pl => updateTokens(pl, -10, inMemorySimpleCrudPlayers))
              _ <- updateTokens(winner, 10, inMemorySimpleCrudPlayers)
            } yield()
          case None => IO()
        }
    }
  }

  private def whoWin( players: Map[Player, List[Card]]): Option[Player] = {
    if (players.isEmpty || players.values.size == 0) None
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
