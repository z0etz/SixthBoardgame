package com.katja.sixthboardgame

import java.util.UUID
import java.util.concurrent.CountDownLatch

class Game(private val userDao: UserDao, playerIdsList: List<String>) {
    var id = UUID.randomUUID().toString()
    val playerIds: List<String> = playerIdsList
    var nextPlayer: String = playerIds.first()
    var freeDiscsGray = 15
    var freeDiscsBrown = 15
    var gameboard = GameBoard()


    //a boolean can be added to track whether the game ended or not
  //  var gameboard: List<List<Stack<Stack.DiscColor>>> = List(5) { List(5) { Stack(mutableListOf()) } }


    constructor(userDao: UserDao, gameId: String, playerIdsList: List<String>) : this(userDao, playerIdsList) {
        this.id = gameId
    }



    fun fetchUsernames(completion: (List<String>) -> Unit) {
        val usernames = mutableListOf<String>()
        val countDownLatch = CountDownLatch(playerIds.size)

        for (playerId in playerIds) {
            userDao.fetchUserById(playerId) { user ->
                user?.let {
                    usernames.add(user.userName)
                }
                countDownLatch.countDown()
            }
        }
        countDownLatch.await()
        completion(usernames)
    }
}
