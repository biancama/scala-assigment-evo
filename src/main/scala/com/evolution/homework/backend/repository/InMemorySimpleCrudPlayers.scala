package com.evolution.homework.backend.repository

import cats.Applicative
import cats.effect.kernel.{Ref, Sync}
import com.evolution.homework.backend.Player
import cats.syntax.all._
import com.evolution.homework.backend.model.PlayerAsset
class InMemorySimpleCrudPlayers[F[_] : Applicative](ref: Ref[F, Map[Player, PlayerAsset]]) extends SimpleCrud.Service[F, Player, PlayerAsset] {


  override def find(player: Player): F[Option[PlayerAsset]] = ref.get.map { map => map.get(player)}

  override def add(player: Player, playerAsset: PlayerAsset): F[Unit] =
    ref.update(oldMap => oldMap.updated(player, playerAsset))
}

object InMemorySimpleCrudPlayers {
  def apply[F[_] : Applicative](implicit F: Sync[F]): InMemorySimpleCrudPlayers[F] =
  new InMemorySimpleCrudPlayers[F](Ref.unsafe[F, Map[Player, PlayerAsset]](Map[Player, PlayerAsset]().empty))

}
