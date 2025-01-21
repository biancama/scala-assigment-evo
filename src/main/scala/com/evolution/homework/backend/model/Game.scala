package com.evolution.homework.backend.model

import com.evolution.homework.backend.{Card, CardGameDecision, GameType, Player}

case class Game(gameType: GameType, deals: Map[Player, Set[Card]] = Map.empty, decisions: Map[Player, CardGameDecision] = Map.empty)
