package com.evolution.homework.backend

import cats.effect.IO

trait Facade {

  /** @return   The balance of "tokens" that a player has. The initial balance should be 0. */
  def getPlayerTokens(player: Player): IO[Tokens]

  /** Requests that a player joins a new game of a particular game type.
    *
    * @return    `GameId` irregardless if there were enough or not enough players to start a game.
    */
  def joinGame(idGenerator: GameIdGenerator, player: Player, gameType: GameType): IO[GameId]

  /** @return   The player's cards if the game is in progress, or `None` if the game is not in progress. */
  def getPlayerCards(gameId: GameId, player: Player): IO[Option[Set[Card]]]

  /** Called to allow the implementation to do the card dealing step. You can assume that there will not be
    *  multiple concurrent invocations of this method, and rely that this method will be called after
    *  `joinGame` has been called for all players.
    */
  def dealCardsForGame(dealer: Dealer, gameId: GameId): IO[Unit]

  /** Applies a player's decision for a game in progress. If the game finishes upon this decision, the
    * implementation should apply the results to player balances.
    */
  def makeDecision(gameId: GameId, player: Player, decision: CardGameDecision): IO[Unit]
}

object Facade {
  // TODO:  Replace with your own implementation.
  def create: IO[Facade] = ???
}
