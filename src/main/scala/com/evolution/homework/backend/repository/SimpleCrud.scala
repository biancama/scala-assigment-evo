package com.evolution.homework.backend.repository


trait SimpleCrud [F[_], K, V] {
  val simpleCrudService: SimpleCrud.Service[F, K, V]
}
object SimpleCrud {
  trait Service[F[_], K, V] {
    def add(key: K, v: V): F[Unit]

    def find(k: K): F[Option[V]]
  }
}