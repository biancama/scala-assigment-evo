package com.evolution.homework.backend

sealed trait CardGameDecision

object CardGameDecision {
  final case object Play extends CardGameDecision
  final case object Fold extends CardGameDecision
}
