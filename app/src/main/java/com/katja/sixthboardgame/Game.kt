package com.katja.sixthboardgame

import java.util.UUID

class Game(playerIdsList: List<String>) {

    // let the current userID be first in the list and put the other users Id at the second place with index 1
    var id = UUID.randomUUID().toString()
    val playerIds: List<String> = playerIdsList
    var nextPlayer: String = playerIds.first()
    var freeDiscsGray = 15
    var freeDiscsBrown = 15
    var gameboard = GameBoard()

    //TODO think of a way to find a proper game id.
    //a boolean can be added to track whether the game ended or not
  //  var gameboard: List<List<Stack<Stack.DiscColor>>> = List(5) { List(5) { Stack(mutableListOf()) } }
}
//    var id = UUID.randomUUID().toString()