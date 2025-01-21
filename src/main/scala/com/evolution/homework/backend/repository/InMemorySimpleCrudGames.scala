package com.evolution.homework.backend.repository

import cats.Applicative
import cats.effect.kernel.{Ref, Sync}
import cats.syntax.all._
import com.evolution.homework.backend.GameId
import com.evolution.homework.backend.model.Game
class InMemorySimpleCrudGames[F[_] : Applicative](ref: Ref[F, Map[GameId, Game]]) extends SimpleCrud.Service[F, GameId, Game] {


  override def find(gameId: GameId): F[Option[Game]] = ref.get.map { map => map.get(gameId)}

  override def add(gameId: GameId, game: Game): F[Unit] =
    ref.update(oldMap => oldMap.updated(gameId, game))
}

object InMemorySimpleCrudGames {
  def apply[F[_] : Applicative](implicit F: Sync[F]): InMemorySimpleCrudGames[F] =
  new InMemorySimpleCrudGames[F](Ref.unsafe[F, Map[GameId, Game]](Map[GameId, Game]().empty))

}
