package com.katja.sixthboardgame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.katja.sixthboardgame.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {

    lateinit var binding: ActivityWelcomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textButtonNewGame.setOnClickListener{
            val intent = Intent(this, StartGameActivity::class.java)
            startActivity(intent)

        }

        binding.textButtonScoreboard.setOnClickListener{
            val intent = Intent(this, HighscoreActivity::class.java)
            startActivity(intent)

        }

        binding.textButtonInstructions.setOnClickListener{
            val intent = Intent(this, GameInstructionActivity::class.java)
            startActivity(intent)

        }

        binding.textButtonPlayerProfile.setOnClickListener{
            val intent = Intent(this, PlayerProfileActivity::class.java)
            startActivity(intent)

        }

    }
}