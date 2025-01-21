package com.evolution.homework.backend.rules

class DoubleGameCardGameRule extends GameRules {

  override def tokensForEveryoneFolds: Int = 2

  override def tokensForOnePlayAndTheOthersFolds: Int = 5

  override def tokensForMoreThanOnePlays: Int = 20

}