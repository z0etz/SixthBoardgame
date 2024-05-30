package com.katja.sixthboardgame

import java.util.Date
import java.util.UUID

class Game {
    var id = UUID.randomUUID().toString()
    var playerIds= listOf("")
    var nextPlayer = ""
    var winnerId = "Unknown"
    var freeDiscsGray = 15
    var freeDiscsBrown = 15
    var gameboard = GameBoard()
    var timestamp = Date()
    var lastTurnTime = Date()
    var gameEnded = false
    var turnTime = 172800000 //Turn time in millis, defaulted to 48 hours

    fun getTimeLeft(): Long {
        val timeSinceLastTurn = Date().time - lastTurnTime.time
        return turnTime - timeSinceLastTurn
    }
}

//Separate game object needed to retrieve data from Firebase in order to decode game board as it contains nested arrays.
//TODO: See it is possible to use Camel case in the GameDataObject and cast it by keys to Firebase like in the the Game object.
data class GameDataObject(
    val id: String = "",
    val player_ids: List<String> = listOf(),
    val next_player: String = "",
    val winner_id: String = "Unknown",
    val free_discs_gray: Int = 0,
    val free_discs_brown: Int = 0,
    val gameboard: String = "",
    var timestamp: Date = Date(),
    var last_turn_time: Date = Date(),
    var game_ended: Boolean = false,
    var turn_time: Int = 172800000
)

