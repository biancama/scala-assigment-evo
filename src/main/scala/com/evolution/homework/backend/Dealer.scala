package com.evolution.homework.backend

import cats.effect.IO

trait Dealer {
  def deal(gameId: GameId, cardsPerPlayer: Int, players: Set[Player]): IO[Map[Player, Set[Card]]]
}
