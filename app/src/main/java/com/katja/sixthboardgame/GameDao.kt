package com.katja.sixthboardgame

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import java.util.Date


class GameDao {

    private val KEY_ID = "id"
    private val KEY_PLAYERIDS = "player_ids"
    private val KEY_NEXTPLAYER = "next_player"
    private val KEY_FREE_DISCS_GRAY = "free_discs_gray"
    private val KEY_FREE_DISCS_BROWN = "free_discs_brown"
    private val KEY_GAMEBOARD = "gameboard"
    private val KEY_TIMESTAMP = "timestamp"
    private val KEY_LASTTURNTIME = "timeSinceLastTurn"
    private val KEY_GAMEENDED = "gameEnded"
    private val KEY_TURNTIME = "turnTime"

    private val db = FirebaseFirestore.getInstance()

    fun addGame(currentUserId: String, receiverId: String) {
        val game = Game()
        game.playerIds = listOf(currentUserId, receiverId)
        updateGame(game)
    }

    fun updateGame(game: Game) {
        val dataToStore = hashMapOf(
            KEY_ID to game.id,
            KEY_PLAYERIDS to game.playerIds,
            KEY_NEXTPLAYER to game.nextPlayer,
            KEY_FREE_DISCS_GRAY to game.freeDiscsGray,
            KEY_FREE_DISCS_BROWN to game.freeDiscsBrown,
            KEY_TIMESTAMP to game.timestamp,
            KEY_LASTTURNTIME to game.lastTurnTime,
            KEY_GAMEENDED to game.gameEnded,
            KEY_TURNTIME to game.turnTime
        )
        println("Last turn taken at ${game.lastTurnTime}")
        val gameBoardJson = Gson().toJson(game.gameboard)
        dataToStore[KEY_GAMEBOARD] = gameBoardJson

        FirebaseFirestore.getInstance()
            .document("Games/${game.id}")
            .set(dataToStore)
            .addOnSuccessListener {
                Log.i("SUCCESS", "Updated game in Firestore with id: ${game.id}")
            }
            .addOnFailureListener { exception ->
                Log.i(
                    "error",
                    "Failed to update game in FireStore with exception: ${exception.message}"
                )
            }
    }

    fun fetchGameById(gameId: String, callback: (Game?) -> Unit) {
        val gameRef = db.collection("Games").document(gameId)
        gameRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val gameData = document.toObject(GameDataObject::class.java)
                    val game = Game()
                    game.id = gameData!!.id
                    game.playerIds = gameData.player_ids
                    game.nextPlayer = gameData.next_player
                    game.freeDiscsGray = gameData.free_discs_gray
                    game.freeDiscsBrown = gameData.free_discs_brown
                    game.gameboard = Gson().fromJson(gameData.gameboard, GameBoard::class.java)
                    game.timestamp = gameData.timestamp
                    game.lastTurnTime = gameData.last_turn_time
                    game.gameEnded = gameData.game_ended
                    game.turnTime = gameData.turn_time
                    callback(game)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("GameDao", "Error getting game document", exception)
                callback(null)
            }
    }

    fun removeGameFromFirebase(gameId: String) {

    }

// TODO: This function will not currently load a game correctly, change to the same structure as the fetchGameById function
fun listenForCurrentUserGamesUpdates(currentId: String?, callback: (List<Game>) -> Unit) {
    FirebaseFirestore
        .getInstance()
        .collection("games")
        .addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.i(
                    "error",
                    "Failed to fetch games from Firestore with exception: ${exception.message}"
                )
                callback(emptyList())
                return@addSnapshotListener
            }

            val gameList = mutableListOf<Game>()

            snapshot?.documents?.forEach { document ->
                val data = document.data
                if (data != null) {
                    val id = data[KEY_ID] as String
                    val playerIds = data[KEY_PLAYERIDS] as List<String>
                    val nextPlayer = data[KEY_NEXTPLAYER] as String
                    val freeDiscsGray = (data[KEY_FREE_DISCS_GRAY] as Long).toInt()
                    val freeDiscsBrown = (data[KEY_FREE_DISCS_BROWN] as Long).toInt()
                    val gameBoardJson = data[KEY_GAMEBOARD] as String

                    // Deserialize gameboard from JSON string to GameBoard object
                    val gameBoard = Gson().fromJson(gameBoardJson, GameBoard::class.java)

                    if (currentId in playerIds) {
                        val game = Game()
                        game.id = id
                        game.playerIds = playerIds
                        game.nextPlayer = nextPlayer
                        game.freeDiscsGray = freeDiscsGray
                        game.freeDiscsBrown = freeDiscsBrown
                        game.gameboard = gameBoard

                        gameList.add(game)
                    }
                }
            }

            callback(gameList)
        }
}


}