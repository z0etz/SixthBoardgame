import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.google.gson.Gson
import com.katja.sixthboardgame.UserDao
import java.util.UUID

class Game(private val userDao: UserDao, playerIdsList: List<String>) {
    var id: String = UUID.randomUUID().toString()
    var playerIds: List<String> = playerIdsList.ifEmpty { listOf("defaultPlayerId1", "defaultPlayerId2") }.shuffled()
    var nextPlayer: String? = playerIds.first()
    var freeDiscsGray: Int = 15
    var freeDiscsBrown: Int = 15
    var gameboard: GameBoard = GameBoard()

    // No-argument constructor for Firebase deserialization
    constructor() : this(UserDao(), emptyList())

    // Exclude gameboard from serialization to avoid nesting arrays
    @get:Exclude
    val gameboardArray: List<List<DiscStack>>
        get() = gameboard.matrix

    // Convert gameboard to JSON for Firestore serialization
    @Exclude
    fun gameboardToJson(): String {
        return Gson().toJson(gameboard)
    }

    // Convert JSON to gameboard for Firestore deserialization
    @Exclude
    fun gameboardFromJson(json: String) {
        Log.d("Game", "JSON string before deserialization: $json") // Debug statement
        gameboard = Gson().fromJson(json, GameBoard::class.java)
    }

    // Convert DocumentSnapshot to Game object
    companion object {
        fun fromSnapshot(snapshot: DocumentSnapshot): Game {
            val game = snapshot.toObject(Game::class.java) ?: error("Failed to convert snapshot to Game")
            game.id = snapshot.id
            return game
        }
    }
}

class GameBoard(matrix: List<List<DiscStack>> = List(5) { List(5) { DiscStack(mutableListOf()) } }) {
    var matrix: List<List<DiscStack>> = matrix

    // No-argument constructor required for Firestore deserialization
    constructor() : this(List(5) { List(5) { DiscStack(mutableListOf()) } })

    // Convert a flat list to a game board
    companion object {
        fun fromFlatList(flatList: List<Map<String, Any>>): GameBoard {
            val matrix = flatList.chunked(5) { row ->
                row.map { DiscStack.fromMap(it) }
            }
            return GameBoard(matrix)
        }
    }
}

class DiscStack(var discs: MutableList<DiscColor>) {
    enum class DiscColor {
        GRAY,
        BROWN
    }

    fun push(discColor: DiscColor) {
        discs.add(discColor)
    }

// Converts the DiscStack to a map
    fun toMap(): Map<String, Any> {
    // Convert DiscColor enum to String
        return mapOf("discs" to discs.map { it.name })
    }

    companion object {
        fun fromMap(map: Map<String, Any>): DiscStack {
            val discs = (map["discs"] as? List<*>)?.map { DiscColor.valueOf(it as String) }?.toMutableList() ?: mutableListOf()
            return DiscStack(discs)
        }
    }
}