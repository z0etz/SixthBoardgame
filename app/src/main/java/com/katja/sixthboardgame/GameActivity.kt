package com.katja.sixthboardgame

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import androidx.annotation.RequiresApi
import androidx.core.view.children
import androidx.core.view.forEachIndexed
import androidx.core.view.size
import com.katja.sixthboardgame.databinding.ActivityGameBinding

class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding
    private var gameBoardSize = 354
    private var screenWidth = 0
    private var screenHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel = GameViewModel()

        getScreenSize()
        calcGameBoardSize()

        //TODO: change initiation of game to load the current game from the view model by correct game id
        val game = viewModel.loadGame("1")

        // Set size of game board and the square views on it according to screen size
        binding.gameBoard.layoutParams.apply {
            width = gameBoardSize
            height = gameBoardSize
        }
        for (i in 1..5) {
            for (j in 1..5) {
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

        binding.playersDiscs.setOnClickListener {
            // Handle onClick event for player's discs here
            // Example:
            println("Clicked player discs")
        }

        // Set onClickListener for all squares on the game board
        binding.gameBoard.children.forEach { row ->
            if (row is GridLayout) {
                row.forEachIndexed { index, view ->
                    if (view is FrameLayout) {
                        view.setOnClickListener {
                            // Handle onClick event for each square here
                            val rowNumber = index / row.size
                            val columnNumber = index % row.size
                            // Example: Log the row and column number of the clicked square
                            println("Clicked square: Row $rowNumber, Column $columnNumber")
                        }
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

        // Get screen size, alternatives for API 30+ and (depricated) version for older API:s
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
}