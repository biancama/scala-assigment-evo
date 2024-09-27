package com.evolution.homework.backend

sealed abstract class Suit private (val value: Char) {
  override def toString: String = value.toString
}

object Suit {
  case object Clubs extends Suit('c')
  case object Hearts extends Suit('h')
  case object Spades extends Suit('s')
  case object Diamonds extends Suit('d')
}
