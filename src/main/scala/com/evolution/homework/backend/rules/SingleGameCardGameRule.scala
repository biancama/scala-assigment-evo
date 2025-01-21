package com.evolution.homework.backend.rules


class SingleGameCardGameRule extends GameRules {

  override def tokensForEveryoneFolds: Int = 1

  override def tokensForOnePlayAndTheOthersFolds: Int = 3

  override def tokensForMoreThanOnePlays: Int = 10

}
