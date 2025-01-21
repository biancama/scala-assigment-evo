package com.evolution.homework.backend.rules

import com.evolution.homework.backend.Card
import com.evolution.homework.backend.CardGameDecision
import com.evolution.homework.backend.Player

final case class Result(isADraw: Boolean, winner: Player, loser: Player)
final case class Decision(player: Player, decision: CardGameDecision, cards: Set[Card])
trait GameRules {
  def tokensForBothFolds: Int  // specify absolute value
  def tokensForOnePlayAndOneFold: Int // specify absolute value
  def tokensForBothPlays: Int // specify absolute value
  def decideWhoWin(decisionForFirstPlayer: Decision, decisionForSecondPlayer: Decision): Result = {
    val firstPlayerCards = decisionForFirstPlayer.cards.toList.sortBy(_.rank.intValue)
    val secondPlayerCards = decisionForSecondPlayer.cards.toList.sortBy(_.rank.intValue)
    val firstCardWithDifferentRank = (firstPlayerCards zip secondPlayerCards).dropWhile { case (card1, card2) => card1.rank.intValue == card2.rank.intValue }

    if (firstCardWithDifferentRank.isEmpty) {
      Result(true, decisionForFirstPlayer.player, decisionForSecondPlayer.player)
    } else if (firstCardWithDifferentRank.head._1.rank.intValue > firstCardWithDifferentRank.head._2.rank.intValue) {
      Result(false, decisionForFirstPlayer.player, decisionForSecondPlayer.player)
    } else {
      Result(false, decisionForSecondPlayer.player, decisionForFirstPlayer.player)
    }
  }
}