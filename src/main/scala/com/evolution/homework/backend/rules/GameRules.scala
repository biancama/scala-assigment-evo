package com.evolution.homework.backend.rules

import com.evolution.homework.backend.Card
import com.evolution.homework.backend.CardGameDecision
import com.evolution.homework.backend.Player

final case class Result(isADraw: Boolean, winner: Player, loser: Player)
final case class Decision(player: Player, decision: CardGameDecision, cards: Set[Card])
trait GameRules {
  def tokensForEveryoneFolds: Int  // specify absolute value
  def tokensForOnePlayAndTheOthersFolds: Int // specify absolute value
  def tokensForMoreThanOnePlays: Int // specify absolute value

}