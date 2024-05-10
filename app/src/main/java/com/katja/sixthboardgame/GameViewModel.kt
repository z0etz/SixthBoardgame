package com.katja.sixthboardgame

import javax.security.auth.callback.Callback

class GameViewModel {

    val gameDao = GameDao()
    var game: Game = Game(mutableListOf("1", "2", "3"))
    fun loadGame(gameId: String): Game {
        gameDao.fetchGameById(gameId){game: Game ->

            this.game = game
        }

        return this.game

        /*//TODO: Load existing game from Firebase, change return below to return that game
        return Game(listOf("1","2"))

         */
    }

    fun createNewGame(playerIdsList: List<String>): Game {
            return Game(playerIdsList)
    }

    fun getGameById(gameId: String?, callback: (Game) -> Unit){
        gameDao.fetchGameById(gameId){game: Game ->

            callback(game)
        }
    }

    fun saveGame(game: Game){

        gameDao.addGame(game)
    }

}