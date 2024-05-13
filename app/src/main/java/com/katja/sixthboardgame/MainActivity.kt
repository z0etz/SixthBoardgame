package com.katja.sixthboardgame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.katja.sixthboardgame.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var bindning: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindning = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindning.root)


        bindning.textButtonSignUp.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }
        bindning.textButtonLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
