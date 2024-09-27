# Game Server task

## Introduction

### Overview

Please develop a back-end for two simple games described below.

To do this, you have to implement the `create` method in the [Facade](src/main/scala/com/evolution/homework/backend/Facade.scala)
companion object. ScalaDocs and [tests](src/test/scala/com/evolution/homework/backend/FacadeTest.scala) 
are available to guide the implementation.

The goal of this task is to demonstrate your skills in designing, structuring and writing code, especially:
* Defining useful abstractions and achieving modularity and code reuse (this is why it includes two similar
  games)
* Correctly handling shared mutable state.

We understand that depending on your experience with similar tasks you may find it straightforward or
difficult.

Therefore, we will review and consider incomplete implementations. However, in this case we ask that you 
briefly list all the limitations (such as missing or incomplete functionality) of your solution.

## Description

### Starting the game

#### Player chooses game type

Player informs the server about the game they want to play using `joinGame` method.

#### Server starts game

The server matches two players who have selected the same game type and starts a new game of this type.

### 'Single-card game'

#### Server deals cards

A single card from the deck is dealt to each player (`dealCardsForGame` method).

#### Player makes a decision

Each player independently of each other sends the server a decision (`makeDecision` method) which is one of:
* `play`
* `fold`

#### Showdown and results

* In case both players picked `fold` each player loses 1 token.
* In case one player picked `play` and the other player picked `fold` then the player who picked `play` wins
  3 tokens, and the player who picked `fold` loses 3 tokens.
* In case both players picked `play` then the cards are compared (see below) - the winning player wins 10
  tokens, and the losing player loses 10 tokens. In case of a draw, neither player wins or loses any tokens.

The cards are compared as follows:
* The player whose card has the highest rank is considered the winning player, and the other player
  is considered the losing player.
* If both cards are equal in rank then the game is a draw.

##### Example 1

* `Player A` has `Jack of Clubs` and selects `fold`.
* `Player B` has `Ten of Hearts` and selects `play`.
* `Player A` loses 3 tokens, and `Player B` wins 3 tokens.

##### Example 2

* `Player A` has `Queen of Diamonds` and selects `play`.
* `Player B` has `Nine of Spades` and selects `play`.
* `Player A` wins 10 tokens, and `Player B` loses 10 tokens.

### 'Double-card game'

#### Server deals cards

Two cards from the deck are dealt to each player (`dealCardsForGame` method). These two cards
are henceforth referred to as "the hand".

#### Player makes a decision

Each player independently of each other sends the server a decision (`makeDecision` method) which is one of:
* `play`
* `fold`

#### Showdown and results

* In case both players picked `fold` each player loses 2 tokens.
* In case one player picked `play` and the other player picked `fold` then the player who picked `play` wins
  5 tokens, and the player who picked `fold` loses 5 tokens.
* In case both players picked `play` then the cards are compared (see below) - the winning player wins 20
  tokens, and the losing player loses 20 tokens. In case of a draw, neither player wins or loses any tokens.

First, the cards with the highest rank from each hand are compared. If they differ, the player whose card has
the highest rank is considered the winner.

If they are equal, then the cards with the lowest rank from each hand are compared.

If those are also equal, then the game is a draw.

##### Example 3

* `Player A` has `Jack of Clubs` and `Nine of Hearts` and selects `play`.
* `Player B` has `Jack of Diamonds` and `Ten of Diamonds` and selects `play`.
* `Player A` loses 20 tokens, and `Player B` wins 20 tokens.

### Finishing the game

Upon finishing of the game, the server applies the game results to player balances, and informs the players
about the game result as well as their respective updated balances.

After this, both players are returned to "Player chooses game type" stage.

## Implementation Notes

The deck used should be a [52-card standard deck](https://en.wikipedia.org/wiki/Standard_52-card_deck).

The rank values should have the following ordering:
* Ace (strongest)
* King
* Queen
* Jack
* Ten
* Nine
* Eight
* Seven
* Six
* Five
* Four
* Three
* Two (weakest)

The [Facade](src/main/scala/com/evolution/homework/backend/Facade.scala) API and tests for it use the
[Cats Effect](https://typelevel.org/cats-effect/) `IO` monad. If you are not familiar with it, please 
consult [Cats Effect IO documentation](https://typelevel.org/cats-effect/api/3.x/cats/effect/IO.html).

You are free to add methods to existing classes (e.g. to `GameType` or `Card`) if you think this is the best
decision for your implementation. You should not need to modify any existing code (besides replacing the `???`
in `Facade.create` with your own implementation), especially method signatures, so please ask before doing so.
## License

At CodeScreen, we strongly value the integrity and privacy of our assessments. As a result, this repository is under exclusive copyright, which means you **do not** have permission to share your solution to this test publicly (i.e., inside a public GitHub/GitLab repo, on Reddit, etc.). <br>

## Submitting your solution

Please push your changes to the `main branch` of this repository. You can push one or more commits. <br>

Once you are finished with the task, please click the `Submit Solution` link on <a href="https://app.codescreen.com/candidate/24751a32-4906-4c8e-bbd8-bd075567625f" target="_blank">this screen</a>.