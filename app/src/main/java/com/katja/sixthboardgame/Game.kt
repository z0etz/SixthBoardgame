package com.katja.sixthboardgame

import java.util.UUID
import java.util.concurrent.CountDownLatch

class Game(private val userDao: UserDao, playerIdsList: List<String>) {
    var id = UUID.randomUUID().toString()
    val playerIds: List<String> = playerIdsList.shuffled()
    var nextPlayer: String = playerIds.first()
    var freeDiscsGray = 15
    var freeDiscsBrown = 15
    var gameboard: List<List<Stack<Stack.DiscColor>>> = List(5) { List(5) { Stack(mutableListOf()) } }

    // Function to fetch usernames.
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