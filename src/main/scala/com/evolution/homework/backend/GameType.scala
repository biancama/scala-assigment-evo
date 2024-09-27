package com.evolution.homework.backend

sealed trait GameType {
  def playerPerGame: Int
  def cardsPerPlayer: Int
}

object GameType {
  case object SingleCardGame extends GameType {
    override def playerPerGame: Int = 2
    override def cardsPerPlayer: Int = 1
  }

  case object DoubleCardGame extends GameType {
    override def playerPerGame: Int = 2
    override def cardsPerPlayer: Int = 2
  }
}
