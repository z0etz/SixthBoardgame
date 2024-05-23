package com.katja.sixthboardgame

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import java.util.Date

interface GameDeletionCallback {
    fun onGameDeleted(success: Boolean)
}
class GameDao {

    private val KEY_ID = "id"
    private val KEY_PLAYERIDS = "player_ids"
    private val KEY_NEXTPLAYER = "next_player"
    private val KEY_FREE_DISCS_GRAY = "free_discs_gray"
    private val KEY_FREE_DISCS_BROWN = "free_discs_brown"
    private val KEY_GAMEBOARD = "gameboard"
    private val KEY_TIMESTAMP = "timestamp"
    private val KEY_LASTTURNTIME = "last_turn_time"
    private val KEY_GAMEENDED = "game_ended"
    private val KEY_TURNTIME = "turn_time"

    private val db = FirebaseFirestore.getInstance()

    fun addGame(currentUserId: String, receiverId: String) {
        val game = Game()
        game.playerIds = listOf(currentUserId, receiverId)
        game.nextPlayer = game.playerIds[0]
        updateGame(game)
        println("Timestamp: ${game.timestamp} Last turn time: ${game.lastTurnTime}")
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


    fun removeGameFromFirebase(gameId: String, callback: GameDeletionCallback) {
        val gameRef = FirebaseFirestore.getInstance().collection("Games").document(gameId)

        // Verify if the document exists before attempting deletion
        gameRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                println("Document with ID: $gameId exists. Proceeding to delete.")

                // Proceed with deletion
                gameRef.delete()
                    .addOnSuccessListener {
                        println("Game deleted successfully with ID: $gameId")
                        callback.onGameDeleted(true)
                    }
                    .addOnFailureListener { e ->
                        println("Error deleting game with ID $gameId: $e")
                        callback.onGameDeleted(false)
                    }
            } else {
                println("Document with ID: $gameId does not exist.")
                callback.onGameDeleted(false)
            }
        }.addOnFailureListener { e ->
            println("Error fetching game with ID $gameId: $e")
            callback.onGameDeleted(false)
        }
    }


// TODO: This function will not currently load a game correctly, change to the same structure as the fetchGameById function if needed,
//       it may not be as only id and playerIds seem to be used.
fun listenForCurrentUserGamesUpdates(currentId: String?, callback: (List<Game>) -> Unit) {
    FirebaseFirestore
        .getInstance()
        .collection("Games")
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