package com.katja.sixthboardgame

import java.util.UUID

class Game(playerIdsList: List<String>) {
    var id = "1"
    val playerIds: List<String> = playerIdsList.shuffled()
    var nextPlayer: String = playerIds.first()
    var freeDiscsGray = 15
    var freeDiscsBrown = 15
    var gameboard = GameBoard()

    //TODO think of a way to find a proper game id.
    //a boolean can be added to track whether the game ended or not
  //  var gameboard: List<List<Stack<Stack.DiscColor>>> = List(5) { List(5) { Stack(mutableListOf()) } }
}
//    var id = UUID.randomUUID().toString()