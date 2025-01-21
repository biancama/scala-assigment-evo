package com.evolution.homework.backend.model

import com.evolution.homework.backend.{Card, GameType, Player}

case class Deal(player: Player, cards:Set[Card] = Set.empty[Card])
case class Game(gameType: GameType, deals: Map[Player, Set[Card]] = Map.empty)
