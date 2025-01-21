package com.evolution.homework.backend.repository

import cats.Applicative
import cats.effect.kernel.{Ref, Sync}
import com.evolution.homework.backend.Player
import cats.syntax.all._
class InMemorySimpleCrud[F[_] : Applicative](ref: Ref[F, Map[Player, PlayerAsset]]) extends SimpleCrud.Service[F] {


  override def find(player: Player): F[Option[PlayerAsset]] = ref.get.map { map => map.get(player)}

  override def add(player: Player, playerAsset: PlayerAsset): F[Unit] =
    ref.update(oldMap => oldMap.updated(player, playerAsset))
}

object InMemorySimpleCrud {
  def apply[F[_] : Applicative](implicit F: Sync[F]): InMemorySimpleCrud[F] =
  new InMemorySimpleCrud[F](Ref.unsafe[F, Map[Player, PlayerAsset]](Map[Player, PlayerAsset]().empty))

}
