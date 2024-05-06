package com.katja.sixthboardgame

import java.util.UUID

class Game(playerIdsList: List<String>) {
    var id = UUID.randomUUID().toString()
    val playerIds: List<String> = playerIdsList.shuffled()
    var nextPlayer: String = playerIds.first()
    var freeDiscsGray = 15
    var freeDiscsBrown = 15
    var gameboard: List<List<Stack<DiscColor>>> = List(5) { List(5) { Stack(emptyList()) } }
}