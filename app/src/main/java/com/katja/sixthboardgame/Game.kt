package com.katja.sixthboardgame

import com.google.gson.annotations.SerializedName
import java.util.UUID
import java.util.concurrent.CountDownLatch

class Game() {
    var id = UUID.randomUUID().toString()
    var playerIds= listOf("")
    var nextPlayer = ""
    var freeDiscsGray = 15
    var freeDiscsBrown = 15
    var gameboard = GameBoard()
}

data class GameDataObject(
    val id: String = "",
    val player_ids: List<String> = listOf(),
    val next_player: String = "",
    val free_discs_gray: Int = 0,
    val free_discs_brown: Int = 0,
    val gameboard: String = ""
)

//    constructor(userDao: UserDao, gameId: String, playerIdsList: List<String>) : this(userDao, playerIdsList) {
//        this.id = gameId
//    }


//    fun fetchUsernames(completion: (List<String>) -> Unit) {
//        val usernames = mutableListOf<String>()
//        val countDownLatch = CountDownLatch(playerIds.size)
//
//        for (playerId in playerIds) {
//            userDao.fetchUserById(playerId) { user ->
//                user?.let {
//                    usernames.add(user.userName)
//                }
//                countDownLatch.countDown()
//            }
//        }
//        countDownLatch.await()
//        completion(usernames)
//    }
