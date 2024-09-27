package com.evolution.homework.backend

final case class Card(rank: Rank, suit: Suit) {
  override def toString: String = s"$rank$suit"
}
