package com.katja.sixthboardgame

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import javax.security.auth.callback.Callback
import com.google.gson.Gson


class GameDao {

    private val KEY_ID = "id"
    private val KEY_PLAYERIDS = "player_ids"
    private val KEY_NEXTPLAYER = "next_player"
    private val KEY_FREE_DISCS_GRAY = "free_discs_gray"
    private val KEY_FREE_DISCS_BROWN = "free_discs_brown"
    private val KEY_GAMEBOARD = "gameboard"


    fun addGame(game: Game) {
        val dataToStore = hashMapOf(
            KEY_ID to game.id,
            KEY_PLAYERIDS to game.playerIds,
            KEY_NEXTPLAYER to game.nextPlayer,
            KEY_FREE_DISCS_GRAY to game.freeDiscsGray,
            KEY_FREE_DISCS_BROWN to game.freeDiscsBrown
        )

        val gameBoardJson = Gson().toJson(game.gameboard)
        dataToStore[KEY_GAMEBOARD] = gameBoardJson

        FirebaseFirestore.getInstance()
            .document("Games/${game.id}")
            .set(dataToStore)
            .addOnSuccessListener {
                Log.i("SUCCESS", "Added a new Game to Firestore with id: ${game.id}")
            }
            .addOnFailureListener { exception ->
                Log.i("error", "failed to add game to FireStore with exception: ${exception.message}")
            }
    }

    // need to filter the required games from the big list
    fun fetchGamesAgainstOpponent(
        currentId: String?,
        opponentId: String?,
        callback: (MutableList<Game>) -> Unit
    ) {
        val gameList = mutableListOf<Game>()

        FirebaseFirestore
            .getInstance()
            .collection("Games")
            .get()
            .addOnSuccessListener { result ->

                for (document in result) {
                    val data = document.data
                    if (data != null) {

                        val id = data.get(KEY_ID) as String
                        val playerIds = data[KEY_PLAYERIDS] as List<String>
                        val nextPlayer = data[KEY_NEXTPLAYER] as String
                        val freeDiscsGray = (data[KEY_FREE_DISCS_GRAY] as Long).toInt()
                        val freeDiscsBrown = (data[KEY_FREE_DISCS_BROWN] as Long).toInt()
                        val gameBoardJson = data[KEY_GAMEBOARD] as String
                        val gameBoard = Gson().fromJson(gameBoardJson, GameBoard::class.java)

                        val game = Game(UserDao(), playerIds)
                        game.id = id
                        game.nextPlayer = nextPlayer
                        game.freeDiscsGray = freeDiscsGray
                        game.freeDiscsBrown = freeDiscsBrown
                        game.gameboard = gameBoard

                        gameList.add(game)
                    }
                }

                callback(filterUserOpponentGames(currentId, opponentId, gameList))
            }
            .addOnFailureListener { exception ->
                callback(mutableListOf())
                Log.i(
                    "error",
                    "failed to fetch games from FireStore with exception: ${exception.message}"
                )
            }
    }


    fun fetchAllUserGmes(currentId: String?, callback: (MutableList<Game>) -> Unit) {
        val gameList = mutableListOf<Game>()

        FirebaseFirestore
            .getInstance()
            .collection("Games")
            .get()
            .addOnSuccessListener { result ->

                for (document in result) {
                    val data = document.data
                    if (data != null) {

                        val id = data.get(KEY_ID) as String
                        val playerIds = data[KEY_PLAYERIDS] as List<String>
                        val nextPlayer = data[KEY_NEXTPLAYER] as String
                        val freeDiscsGray = (data[KEY_FREE_DISCS_GRAY] as Long).toInt()
                        val freeDiscsBrown = (data[KEY_FREE_DISCS_BROWN] as Long).toInt()
                        val gameBoardJson = data[KEY_GAMEBOARD] as String
                        val gameBoard = Gson().fromJson(gameBoardJson, GameBoard::class.java)

                        val game = Game(UserDao(), playerIds)
                        game.id = id
                        game.nextPlayer = nextPlayer
                        game.freeDiscsGray = freeDiscsGray
                        game.freeDiscsBrown = freeDiscsBrown
                        game.gameboard = gameBoard

                        gameList.add(game)
                    }
                }

                callback(filterCurrentUserGames(currentId, gameList))
            }
            .addOnFailureListener { exception ->
                callback(mutableListOf())
                Log.i(
                    "error",
                    "failed to fetch games from FireStore with exception: ${exception.message}"
                )
            }


    }

    fun fetchGameById(gameId: String?, callback: (Game) -> Unit) {

        FirebaseFirestore
            .getInstance()
            .document("Games/${gameId}")
            .get()
            .addOnSuccessListener { result ->
                val data = result.data
                if (data != null) {
                    println(data)
                    val id = data.get(KEY_ID) as String
                    val playerIds = data[KEY_PLAYERIDS] as List<String>
                    val nextPlayer = data[KEY_NEXTPLAYER] as String
                    val freeDiscsGray = (data[KEY_FREE_DISCS_GRAY] as Long).toInt()
                    val freeDiscsBrown = (data[KEY_FREE_DISCS_BROWN] as Long).toInt()
                    val gameBoardJson = data[KEY_GAMEBOARD] as String
                    val gameBoard = Gson().fromJson(gameBoardJson, GameBoard::class.java)

                    val game = Game(UserDao(), playerIds)
                    game.id = id
                    game.nextPlayer = nextPlayer
                    game.freeDiscsGray = freeDiscsGray
                    game.freeDiscsBrown = freeDiscsBrown
                    game.gameboard = gameBoard

                    callback(game)


                }

            }
            .addOnFailureListener { exception ->
                Log.i(
                    "error",
                    "failed to fetch games from FireStore with exception: ${exception.message}"
                )
            }
    }


    fun filterCurrentUserGames(
        currentUserId: String?,
        oldGameList: MutableList<Game>
    ): MutableList<Game> {

        val newGameList = mutableListOf<Game>()

        for (game in oldGameList) {

            if (currentUserId in game.playerIds) {
                newGameList.add(game)
            }

        }




        return newGameList

    }

    fun filterUserOpponentGames(
        currentUserId: String?,
        opponentId: String?,
        oldGameList: MutableList<Game>
    ): MutableList<Game> {

        val newGameList = mutableListOf<Game>()

        for (game in oldGameList) {

            if (currentUserId in game.playerIds && opponentId in game.playerIds) {
                newGameList.add(game)
            }

        }

        return newGameList

    }
}