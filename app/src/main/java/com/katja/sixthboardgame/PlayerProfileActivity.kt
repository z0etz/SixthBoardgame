package com.katja.sixthboardgame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.katja.sixthboardgame.databinding.ActivityPlayerProfileBinding

class PlayerProfileActivity : AppCompatActivity() {

    lateinit var binding: ActivityPlayerProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textButtonSignOut.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

        }
    }
}