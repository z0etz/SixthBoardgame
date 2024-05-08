package com.katja.sixthboardgame

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.katja.sixthboardgame.databinding.ActivityGameBinding

class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding
    private var gameBoardSize = 354
    private var screenWidth = 0
    private var screenHeight = 0
    private var discStackClicked = false
    private var playerDiscColor = Stack.DiscColor.BROWN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userDaoInstance = UserDao()

        val viewModel = GameViewModel(userDaoInstance)

        getScreenSize()
        calcGameBoardSize()

        //TODO: change initiation of game to load the current game from the view model by correct game id
        val game = viewModel.loadGame("1", listOf("player1", "player2"))
        //TODO: set playerDiscColor to Stack.DiscColor.GRAY if the current player is the first (id) in the list of playerIds of the game


        binding.gameBoard.layoutParams.apply {
            width = gameBoardSize
            height = gameBoardSize
        }
        for (i in 0..4) {
            for (j in 0..4) {
                val squareId = resources.getIdentifier("square$i$j", "id", packageName)
                val viewSquare = findViewById<FrameLayout>(squareId)
                viewSquare?.let {
                    val layoutParams = it.layoutParams as ViewGroup.LayoutParams
                    layoutParams.width = (gameBoardSize - 4) / 5
                    layoutParams.height = (gameBoardSize - 4) / 5
                    it.layoutParams = layoutParams
                }
            }
        }

        binding.gameBackground.setOnClickListener {
            discStackClicked = false
            println("Deselected")
        }

        binding.playersDiscs.setOnClickListener {
            discStackClicked = true
            println("Clicked player discs")
        }


        for (i in 0..4) {
            for (j in 0..4) {
                val squareId = resources.getIdentifier("square$i$j", "id", packageName)
                val squareView = findViewById<FrameLayout>(squareId)
                squareView?.setOnClickListener {

                    val rowNumber = i
                    val columnNumber = j

                    println("Clicked square: Row $rowNumber, Column $columnNumber")

                    if(discStackClicked && game.gameboard[i][j].discs.isEmpty()) {
                        game.gameboard[i][j].push(playerDiscColor)
                        println("Added disc")
                        updateViewSquare(game.gameboard[i][j], squareView)
                    }

                }
            }
        }
    }


    private fun calcGameBoardSize() {
        gameBoardSize = if (screenHeight > screenWidth) screenWidth - 50 else screenWidth / 2
    }

    private fun getScreenSize() {
        val version = Build.VERSION.SDK_INT


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

    private fun updateViewSquare(stack: Stack<Stack.DiscColor>, squareView: FrameLayout) {

        stack.discs.forEachIndexed { index, discColor ->
            val discViewId = resources.getIdentifier("disc${index}", "id", packageName)
            val discView = squareView.findViewById<View>(discViewId)


            discView?.visibility = if (discColor != null) View.VISIBLE else View.GONE


            discView?.background = ContextCompat.getDrawable(
                this,
                when (discColor) {
                    Stack.DiscColor.GRAY -> R.drawable.player_piece_gray
                    else -> R.drawable.player_piece_brown
                }
            )
        }
    }
}
