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


    fun addGame(game: Game){
        val dataToStore = HashMap<String, Any>()
        dataToStore[KEY_ID] = game.id as Any
        dataToStore[KEY_PLAYERIDS] = game.playerIds as Any
        dataToStore[KEY_NEXTPLAYER] = game.nextPlayer as Any
        dataToStore[KEY_FREE_DISCS_GRAY] = game.freeDiscsGray as Any
        dataToStore[KEY_FREE_DISCS_BROWN] = game.freeDiscsBrown as Any

        var gameBoard = Gson().toJson(game.gameboard)
        dataToStore[KEY_GAMEBOARD] = gameBoard as Any

        FirebaseFirestore
            .getInstance()
            .document("Games/${game.id}")
            .set(dataToStore)
            .addOnSuccessListener {
                Log.i(
                    "SUCCESS",
                    "Added a new Game to Firestore with id: ${game.id}"
                )
            }
            .addOnFailureListener {exception ->
                Log.i("error", "failed to add game to FireStore with exception: ${exception.message}")
            }


    }

    fun fetchGameById(gameId: String?, callback: (Game) -> Unit){

        FirebaseFirestore
            .getInstance()
            .document("Games/${gameId}")
            .get()
            .addOnSuccessListener { result ->
                val data = result.data
                if(data != null) {
                    println(data)
                    val id = data.get(KEY_ID) as String
                    val playerIds = data[KEY_PLAYERIDS] as List<String>
                    val nextPlayer = data[KEY_NEXTPLAYER] as String
                    val freeDiscsGray = (data[KEY_FREE_DISCS_GRAY] as Long).toInt()
                    val freeDiscsBrown = (data[KEY_FREE_DISCS_BROWN] as Long).toInt()
                    val gameBoardJson = data[KEY_GAMEBOARD] as String
                    val gameBoard = Gson().fromJson(gameBoardJson, GameBoard::class.java)

                    val game = Game(playerIds)
                    game.id = id
                    game.nextPlayer = nextPlayer
                    game.freeDiscsGray = freeDiscsGray
                    game.freeDiscsBrown = freeDiscsBrown
                    game.gameboard = gameBoard

                    callback(game)



                }

            }
    }







    fun fetchGmes(callback: (List<Game>) -> Unit){
        val gameList = mutableListOf<Game>()
        FirebaseFirestore
            .getInstance()
            .collection("games")
            .get()
            .addOnSuccessListener { QuerySnapshot ->
                for (document in QuerySnapshot) {
                   // val id = document.getString(KEY_ID) ?: ""
                    val game = document as Game
                    gameList.add(game)
                }

            }

    }
}