package com.evolution.homework.backend.repository

import com.evolution.homework.backend.{Player, Tokens}

final case class PlayerAsset (tokens: Tokens)

trait SimpleCrud [F[_]] {
  val simpleCrudService: SimpleCrud.Service[F]
}
object SimpleCrud {
  trait Service[F[_]] {
    def add(player: Player, playerAsset: PlayerAsset): F[Unit]

    def find(player: Player): F[Option[PlayerAsset]]
  }
}