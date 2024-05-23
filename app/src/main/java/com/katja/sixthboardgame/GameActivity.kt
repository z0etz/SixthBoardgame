package com.katja.sixthboardgame

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.katja.sixthboardgame.databinding.ActivityGameBinding
import java.util.Date

class GameActivity : AppCompatActivity() {

    //Hej

    private lateinit var binding: ActivityGameBinding
    private var gameBoardSize = 354
    private var screenWidth = 0
    private var screenHeight = 0
    private var playerDiscStackClicked = false
    private var playerDiscColor = DiscStack.DiscColor.BROWN
    private var discStackSelected: DiscStack? = null
    private var discStackSelectedView: FrameLayout? = null
    private var numberOfDiscs = 0
    private var discsToMove = 0
    private var availableMoveSquares: MutableList<FrameLayout> = mutableListOf()
    private lateinit var game: Game
    private lateinit var gameId: String
    private lateinit var gameRef: DocumentReference
    private var winnerId = "Unknown"
    private lateinit var auth: FirebaseAuth
    private var currentUserId: String? = null
    private var opponentId: String? = null
    private lateinit var userDao: UserDao
    private lateinit var gameDao: GameDao
    private var gameListener: ListenerRegistration? = null
    private var countDownTimer: CountDownTimer? = null
    private lateinit var viewModel: GameViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userDao = UserDao()
        gameDao = GameDao()
        viewModel = GameViewModel()

        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid

        getScreenSize()
        calcGameBoardSize()

        gameId = intent.getStringExtra("GAME_ID") ?: return
        gameRef = FirebaseFirestore.getInstance().document("Games/$gameId")
        println("current game's id = $gameId")

        // Set size of game board and the square views on it according to screen size
        binding.gameBoard.layoutParams.apply {
            width = gameBoardSize
            height = gameBoardSize
        }

        // Load the game by ID
        viewModel.loadGameById(gameId) { loadedGame ->
            if (loadedGame != null) {
                game = loadedGame
                println(gameId)
                setupPlayerDiscColor()

                for (i in 0..4) {
                    for (j in 0..4) {
                        val squareId = resources.getIdentifier("square$i$j", "id", packageName)
                        val viewSquare = findViewById<FrameLayout>(squareId)
                        viewSquare?.let {
                            val layoutParams = it.layoutParams as ViewGroup.LayoutParams
                            layoutParams.width = (gameBoardSize - 9) / 5
                            layoutParams.height = (gameBoardSize - 9) / 5
                            it.layoutParams = layoutParams
                        }
                        updateViewSquare(game.gameboard.matrix[i][j], viewSquare)
                    }
                }

                // Add Firestore listener to sync the game data and start game timer
               if(!game.gameEnded) {
                   addGameListener()
               }
                startTimer()

                updateFreeDiscsView(this)
                updateFreeDiscsView(this, playerDiscs = false)
                updateWhosTurn()

                // Show players username
                userDao.fetchUsernameById(currentUserId ?: "Unknown") { username ->
                    if (username != null) {
                        Log.d("GameActivity", "Player username: $username")
                        binding.textPlayerName.text = username
                    } else {
                        Log.e("GameActivity", "Failed to get player username")
                    }
                }

                // Show opponents username
                opponentId = game.playerIds.firstOrNull { it != currentUserId }
                userDao.fetchUsernameById(opponentId ?: "Unknown") { username ->
                    if (username != null) {
                        Log.d("GameActivity", "Opponent username: $username")
                        binding.textOpponentName.text = username
                    } else {
                        Log.e("GameActivity", "Failed to get opponent username")
                    }
                }

                // Handle glicks and game logic
                binding.gameBackground.setOnClickListener {
                    resetAvailableMoveSquares()
                }

                binding.playersDiscs.setOnClickListener {
                    if (!game.gameEnded && game.nextPlayer == currentUserId) {
                        println("Clicked player disc stack")
                        resetAvailableMoveSquares()
                        if (playerDiscColor == DiscStack.DiscColor.BROWN && game.freeDiscsBrown > 0 ||
                            playerDiscColor == DiscStack.DiscColor.GRAY && game.freeDiscsGray > 0
                        ) {
                            playerDiscStackClicked = true
                            makeEmptySquaresAvailable()
                        }
                    }
                }

                // Set onClickListener for all squares on the game board
                for (i in 0..4) {
                    for (j in 0..4) {
                        val squareId = resources.getIdentifier("square$i$j", "id", packageName)
                        val squareView = findViewById<FrameLayout>(squareId)
                        squareView?.setOnClickListener {

                            //Handle different cases of game logic as long as the game has not ended
                            if (!game.gameEnded && game.nextPlayer == currentUserId) {
                                // Identify connection between the view square and the parameters for it in the game board of the game object
                                println("Clicked square: Row $i, Column $j")

                                //Place new disc on the board
                                if (playerDiscStackClicked && game.gameboard.matrix[i][j].discs.isEmpty()) {
                                    game.gameboard.matrix[i][j].push(playerDiscColor)
                                    if (playerDiscColor == DiscStack.DiscColor.BROWN) {
                                        game.freeDiscsBrown -= 1
                                        println("freeDiscsBrown: ${game.freeDiscsBrown}")
                                    } else {
                                        game.freeDiscsGray -= 1
                                        println("freeDiscsGray: ${game.freeDiscsGray}")
                                    }
                                    println("Added disc")
                                    updateViewSquare(game.gameboard.matrix[i][j], squareView)
                                    resetAvailableMoveSquares()
                                    updateFreeDiscsView(this)
                                    finishTurn()
                                }

                                // Move discs from stackSelected to the clicked stack
                                else if (discStackSelected != null && availableMoveSquares.contains(
                                        squareView
                                    )
                                ) {
                                    var indexOffsetToRemove = 0
                                    for (index in (numberOfDiscs - discsToMove) until numberOfDiscs) {
                                        println("Removed disc at: " + (index + indexOffsetToRemove))
                                        val discColor =
                                            discStackSelected!!.discs.removeAt(index + indexOffsetToRemove)
                                        println("Removed $discColor disk")
                                        game.gameboard.matrix[i][j].push(discColor)
                                        println("Added $discColor disk")
                                        indexOffsetToRemove--
                                    }
                                    // Update view squares for both stacks
                                    if (discStackSelectedView != null) {
                                        updateViewSquare(
                                            discStackSelected!!,
                                            discStackSelectedView!!
                                        )
                                    }
                                    updateViewSquare(game.gameboard.matrix[i][j], squareView)
                                    resetAvailableMoveSquares()
                                    // Check if the game ended
                                    if (game.gameboard.matrix[i][j].discs.size >= 6) {
                                        val winnerColor =
                                            game.gameboard.matrix[i][j].discs.lastOrNull()
                                                ?: DiscStack.DiscColor.GRAY
                                        val winnerColorString = winnerColor.name
                                        println("$winnerColorString won!")
                                        winnerId =
                                            if (playerDiscColor == winnerColor) currentUserId
                                                ?: "Unknown"
                                            else game.playerIds.find { it != currentUserId }
                                                ?: "Unknown"
                                        val looserId =
                                            game.playerIds.find { it != winnerId } ?: "Unknown"
                                        game.gameEnded = true
                                        gameDao.updateGame(game)
                                        viewModel.endGame(game.id, winnerId, looserId)
                                        gameListener?.remove()
                                        finishTurn()
                                        stopTimer()
                                        showGameEndDialogue()
                                    }
                                    finishTurn()
                                }

                                //Select stack on the game board to move discs from
                                else if (game.gameboard.matrix[i][j].discs.isNotEmpty()) {
                                    resetAvailableMoveSquares()
                                    discStackSelected = game.gameboard.matrix[i][j]
                                    discStackSelectedView = squareView
                                    numberOfDiscs = discStackSelected?.discs?.size ?: 0
                                    discsToMove = numberOfDiscs
                                    println("Stack selected $i$j contains $numberOfDiscs")

                                    // Logic to follow the rules of the game in how different stack sizes can move
                                    when (numberOfDiscs) {
                                        1 -> {
                                            val adjacentPositions = listOf(
                                                Pair(i - 1, j), // One step up
                                                Pair(i + 1, j), // One step down
                                                Pair(i, j - 1), // One step left
                                                Pair(i, j + 1)  // One step right
                                            )
                                            // Check if adjacent positions are valid and mark corresponding squares as available moves
                                            adjacentPositions.forEach { (row, column) ->
                                                if (row in 0 until 5 && column in 0 until 5) {
                                                    val adjacentStack =
                                                        game.gameboard.matrix[row][column]
                                                    if (adjacentStack.discs.isNotEmpty()) {
                                                        val squareId = resources.getIdentifier(
                                                            "square$row$column",
                                                            "id",
                                                            packageName
                                                        )
                                                        val adjacentSquareView =
                                                            findViewById<FrameLayout>(squareId)
                                                        setAvailableMoveSquare(adjacentSquareView)
                                                    }
                                                }
                                            }
                                        }

                                        2 -> {
                                            // Define directions to move (up, down, left, right)
                                            val directions = listOf(
                                                Pair(-1, 0), // Up
                                                Pair(1, 0),  // Down
                                                Pair(0, -1), // Left
                                                Pair(0, 1)   // Right
                                            )

                                            // Iterate over each direction and find available moves
                                            directions.forEach { (dRow, dColumn) ->
                                                findAvailableMovesInDirection(i, j, dRow, dColumn)
                                            }
                                        }

                                        3 -> {
                                            // Define possible knight moves on the board
                                            val knightMoves = listOf(
                                                Pair(-2, -1),  // Two steps up and one step left
                                                Pair(-2, 1),   // Two steps up and one step right
                                                Pair(-1, -2),  // Two steps left and one step up
                                                Pair(-1, 2),   // Two steps right and one step up
                                                Pair(1, -2),   // Two steps left and one step down
                                                Pair(1, 2),    // Two steps right and one step down
                                                Pair(2, -1),   // Two steps down and one step left
                                                Pair(2, 1)     // Two steps down and one step right
                                            )

                                            // Iterate over each knight move to find available moves
                                            knightMoves.forEach { (dRow, dColumn) ->
                                                val newRow = i + dRow
                                                val newColumn = j + dColumn
                                                // Checks if the new position is within the game board
                                                if (newRow in 0 until 5 && newColumn in 0 until 5) {
                                                    // Check if the square is empty or occupied
                                                    val adjacentStack =
                                                        game.gameboard.matrix[newRow][newColumn]
                                                    if (adjacentStack.discs.isNotEmpty()) {
                                                        val squareId = resources.getIdentifier(
                                                            "square$newRow$newColumn",
                                                            "id",
                                                            packageName
                                                        )
                                                        val adjacentSquareView =
                                                            findViewById<FrameLayout>(squareId)
                                                        setAvailableMoveSquare(adjacentSquareView)
                                                    }
                                                }
                                            }
                                        }

                                        4 -> {
                                            // Define diagonal direction to move (up-left, up-right, down-left, down-right)
                                            val diagonalDirections = listOf(
                                                Pair(-1, -1), // Up-left
                                                Pair(-1, 1),  // Up-right
                                                Pair(1, -1),  // Down-left
                                                Pair(1, 1)    // Down-right
                                            )

                                            // Iterate over each diagonal direction and find available moves
                                            diagonalDirections.forEach { (dRow, dColumn) ->
                                                findAvailableMovesInDirection(i, j, dRow, dColumn)
                                            }
                                        }

                                        5 -> {
                                            // Define directions to move (up, down, left, right, and diagonally)
                                            val directions = listOf(
                                                Pair(-1, 0),   // Up
                                                Pair(1, 0),    // Down
                                                Pair(0, -1),   // Left
                                                Pair(0, 1),    // Right
                                                Pair(-1, -1),  // Up-left
                                                Pair(-1, 1),   // Up-right
                                                Pair(1, -1),   // Down-left
                                                Pair(1, 1)     // Down-right
                                            )

                                            // Iterate over each direction and find available moves
                                            directions.forEach { (dRow, dColumn) ->
                                                findAvailableMovesInDirection(i, j, dRow, dColumn)
                                            }
                                        }

                                        else -> {
                                            resetAvailableMoveSquares()
                                        }
                                    }
                                    if (numberOfDiscs in 2..5 && !availableMoveSquares.isEmpty()) {
                                        binding.discsTooMoveDialogue.visibility = View.VISIBLE
                                        binding.discsToMoveText.text =
                                            getString(R.string.discs_to_move) + " " + discsToMove
                                    }
                                } else {
                                    resetAvailableMoveSquares()
                                }
                            }
                        }
                    }
                }
            } else {
                // Handle the case where the game could not be loaded
                Log.e("GameActivity", "Failed to load game with ID $gameId")
                // Optionally, show an error message to the user and close the activity
                finish()
            }
        }

        //Set on-click listeners for buttons in the choose number of discs dialogue
        binding.buttonMinus.setOnClickListener {
            if (discsToMove > 1 && !game.gameEnded) {
                discsToMove--
                binding.discsToMoveText.text = getString(R.string.discs_to_move) + " " + discsToMove
                println(getString(R.string.discs_to_move) + discsToMove)
            }
        }
        //Set on-click listeners for buttons in the choose number of discs dialogue
        binding.buttonPlus.setOnClickListener {
            if (discsToMove < numberOfDiscs && !game.gameEnded) {
                discsToMove++
                binding.discsToMoveText.text = getString(R.string.discs_to_move) + " " + discsToMove
                println(getString(R.string.discs_to_move) + discsToMove)
            }
        }

    }

    private fun calcGameBoardSize() {
        gameBoardSize = if (screenHeight > screenWidth) screenWidth - 50 else screenWidth / 2
    }

    private fun getScreenSize() {
        val version = Build.VERSION.SDK_INT

        // Get screen size, alternatives for API 30+ and (deprecated) version for older API:s
        if (version >= Build.VERSION_CODES.R) {
            @RequiresApi(Build.VERSION_CODES.R)
            screenWidth = windowManager.currentWindowMetrics.bounds.width()
            @RequiresApi(Build.VERSION_CODES.R)
            screenHeight = windowManager.currentWindowMetrics.bounds.height()
        } else {
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            screenWidth = displayMetrics.widthPixels
            screenHeight = displayMetrics.heightPixels
        }
    }

    private fun updateViewSquare(discStack: DiscStack, squareView: FrameLayout) {
        // Determine the starting index based on the number of discs in the stack
        val startIndex = maxOf(0, discStack.discs.size - 6)

        // Iterate through the last 6 disc of the list to set views in the view square accordingly
        for (i in 0 until 6) {
            val discViewId = resources.getIdentifier("disc${i}", "id", packageName)
            val discView = squareView.findViewById<View>(discViewId)

            if (i < discStack.discs.size) {
                // If there are more discs in the stack than there are disc views in the square, turn on the corresponding disc view
                discView?.visibility = View.VISIBLE

                // Show disc of correct color
                val discColor = discStack.discs[i]
                discView?.background = ContextCompat.getDrawable(
                    this,
                    when (discColor) {
                        DiscStack.DiscColor.GRAY -> R.drawable.player_piece_gray
                        else -> R.drawable.player_piece_brown
                    }
                )
            } else {
                // If there are fewer discs in the stack than there are disc views in the square, turn off the corresponding disc view
                discView?.visibility = View.GONE
            }
        }
        // Show plus view if there are more than 6 discs in the list
        val plusView = squareView.findViewById<View>(R.id.plus_sign)
        plusView?.visibility = if (discStack.discs.size > 6) View.VISIBLE else View.GONE

        var numberOfdiscsText = squareView.findViewById<TextView>(R.id.text_number_of_discs)
        numberOfdiscsText.text = discStack.discs.size.toString()
        if (discStack.discs.size > 1 && numberOfdiscsText != null) {
            numberOfdiscsText.visibility = View.VISIBLE
        } else {
            numberOfdiscsText?.visibility = View.GONE
        }
    }

    private fun setAvailableMoveSquare(squareView: FrameLayout) {
        val squareBackgroundView = squareView.findViewById<View>(R.id.square_background)
        squareBackgroundView.setBackgroundColor(ContextCompat.getColor(this, R.color.green_dark))
        availableMoveSquares.add(squareView)
    }

    private fun resetAvailableMoveSquares() {
        availableMoveSquares.forEach { squareView ->
            val squareBackgroundView = squareView.findViewById<View>(R.id.square_background)
            squareBackgroundView.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        }
        availableMoveSquares.clear()
        discStackSelected = null
        discStackSelectedView = null
        playerDiscStackClicked = false
        numberOfDiscs = 0
        discsToMove = 0
        binding.discsTooMoveDialogue.visibility = View.GONE
    }

    private fun makeEmptySquaresAvailable() {
        for (i in 0..4) {
            for (j in 0..4) {
                // Check if the square is empty and set it as an available move if it is.
                val notEmpty = game.gameboard.matrix[i][j].discs.any { it != null }
                if (!notEmpty) {
                    val squareId = resources.getIdentifier("square$i$j", "id", packageName)
                    val squareView = findViewById<FrameLayout>(squareId)
                    setAvailableMoveSquare(squareView)
                }
            }
        }
    }

    private fun findAvailableMovesInDirection(i: Int, j: Int, dRow: Int, dColumn: Int) {
        var currentRow = i + dRow
        var currentColumn = j + dColumn

        while (currentRow in 0 until 5 && currentColumn in 0 until 5) {
            val adjacentStack = game.gameboard.matrix[currentRow][currentColumn]
            if (adjacentStack.discs.isNotEmpty()) {
                val squareId =
                    resources.getIdentifier("square$currentRow$currentColumn", "id", packageName)
                val adjacentSquareView = findViewById<FrameLayout>(squareId)
                setAvailableMoveSquare(adjacentSquareView)
                break
            }
            currentRow += dRow
            currentColumn += dColumn
        }
    }

    private fun updateFreeDiscsView(activity: Activity, playerDiscs: Boolean = true) {
        val discContainer = if (playerDiscs) {
            findViewById<LinearLayout>(R.id.players_discs)
        } else {
            findViewById<LinearLayout>(R.id.opponents_discs)
        }
        discContainer.removeAllViews()

        // Set drawable color and parameter to reed free discs from dependant on the players disc color
        var discDrawable = R.drawable.player_piece_brown
        var numDiscsToShow = game.freeDiscsBrown
        if (playerDiscs && playerDiscColor != DiscStack.DiscColor.BROWN ||
            (!playerDiscs && playerDiscColor == DiscStack.DiscColor.BROWN)
        ) {
            discDrawable = R.drawable.player_piece_gray
            numDiscsToShow = game.freeDiscsGray
        }

        // Add disc views to the container
        for (i in 0 until numDiscsToShow) {
            val discView = ImageView(this) // Create a new ImageView for each disc
            discView.setImageResource(discDrawable) // Set the image resource for the disc
            val layoutParams = FrameLayout.LayoutParams(
                ((gameBoardSize - 9) / 5) - 66,
                ((gameBoardSize - 9) / 5) - 66
            )
            if (i > 0) {
                layoutParams.marginStart = -screenWidth / 10 // Overlapping margin
            }
            discView.layoutParams = layoutParams
            discContainer.addView(discView)
        }
    }

    private fun showGameEndDialogue() {
        val dialog = Dialog(this)
        if (winnerId == currentUserId) {
            val winDialogFragment = WinDialogFragment()
            winDialogFragment.show(supportFragmentManager, "WinDialogFragment")
        } else {
            val looseDialogFragment = LooseDialogFragment()
            looseDialogFragment.show(supportFragmentManager, "LooseDialogFragment")
        }
        gameListener?.remove()
    }

    fun fetchPlayerIdsForGame(gameId: String, callback: (List<String>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Games")
            .whereEqualTo("id", gameId)
            .get()
            .addOnSuccessListener { documents ->
                val playerIds = mutableListOf<String>()
                for (document in documents) {
                    val playerIdsFromDoc = document.get("player_ids") as? List<String>
                    if (playerIdsFromDoc != null) {
                        playerIds.addAll(playerIdsFromDoc)
                    }
                }
                callback(playerIds)
            }
            .addOnFailureListener { exception ->
                Log.w("GameActivity", "Error getting documents: ", exception)
                callback(emptyList())
            }
    }

    private fun setupPlayerDiscColor() {
        fetchPlayerIdsForGame(gameId) { playerIds ->
            if (playerIds.isNotEmpty()) {
                println("Player IDs for game $gameId: $playerIds")
                if (playerIds[0] == currentUserId) {
                    playerDiscColor = DiscStack.DiscColor.GRAY
                }
            } else {
                println("No player IDs found for game $gameId")
            }
        }

        // Set playerDiscColor to Stack.DiscColor.GRAY if the current player is the first in the list of playerIds of the game
        if (game.playerIds.isNotEmpty() && game.playerIds[0] == currentUserId) {
            playerDiscColor = DiscStack.DiscColor.GRAY
        }
    }

    private fun finishTurn() {
        println("Finised turn")
        game.lastTurnTime = Date()
        game.nextPlayer = game.playerIds.firstOrNull { it != game.nextPlayer }.toString()
        println("${game.nextPlayer}´s turn")
        startTimer()
        gameDao.updateGame(game)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove Firestore listener when the activity is destroyed
        gameListener?.remove()
    }

    private fun addGameListener() {
        gameListener = gameRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("GameActivity", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val gameId = snapshot.id // Get the ID of the game from the snapshot
                // Fetch the game data by its ID
                gameDao.fetchGameById(gameId) { loadedGame ->
                    if (loadedGame != null) {
                        game = loadedGame
                        println("Game data updated: $game")

                        // Update UI with the new game data
                        updateGameBoard()
                        updateFreeDiscsView(this)
                        updateFreeDiscsView(this, playerDiscs = false)
                        updateWhosTurn()
                        startTimer()
                        if (game.gameEnded) {
                            showGameEndDialogue()
                        }
                    } else {
                        Log.d("GameActivity", "Failed to fetch game data.")
                    }
                }
            } else {
                Log.d("GameActivity", "Current data: null")
            }
        }
    }

    private fun updateGameBoard() {
        for (i in 0..4) {
            for (j in 0..4) {
                val discStack = game.gameboard.matrix[i][j]
                val squareViewId = resources.getIdentifier("square$i$j", "id", packageName)
                val squareView = findViewById<FrameLayout>(squareViewId)
                updateViewSquare(discStack, squareView)
            }
        }
    }

    private fun updateWhosTurn() {
        userDao.fetchUsernameById(game.nextPlayer ?: "Unknown") { username ->
            if (username != null) {
                Log.d("GameActivity", "Next player: $username")
                binding.playersTurn.text = username + getString(R.string.s_turn)
            } else {
                Log.e("GameActivity", "Failed to get next players username")
            }
        }
    }

    private fun startTimer() {
        countDownTimer?.cancel() // Cancel any existing timer

        if(!game.gameEnded) {
            val timeSinceLastTurn = Date().time - game.lastTurnTime.time
            val turnTimeLeft = game.turnTime - timeSinceLastTurn

            countDownTimer = object : CountDownTimer(turnTimeLeft, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val hours = millisUntilFinished / (1000 * 60 * 60)
                    val minutes = (millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60)
                    val seconds = (millisUntilFinished % (1000 * 60)) / 1000
                    binding.timeLeft.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                }

                override fun onFinish() {
                    binding.timeLeft.text = "00:00:00"
                    val looserId = game.nextPlayer
                    winnerId = game.playerIds.find { it != looserId } ?: "Unknown"
                    game.gameEnded = true
                    gameDao.updateGame(game)
                    viewModel.endGame(game.id, winnerId, looserId)
                    stopTimer()
                    showGameEndDialogue()
                }
            }.start()
        } else {
            binding.timeLeft.text = getString(R.string.game_ended)
        }
    }
    private fun stopTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

}
