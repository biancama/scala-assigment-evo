package com.evolution.homework.backend.rules


class SingleGameCardGameRule extends GameRules {

  override def tokensForBothFolds: Int = 1

  override def tokensForOnePlayAndOneFold: Int = 3

  override def tokensForBothPlays: Int = 10

}
