package com.katja.sixthboardgame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.katja.sixthboardgame.databinding.ActivityLoseDialogBinding

class LoseDialogActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoseDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoseDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser?.uid

        println(currentUserId)
        //TODO: Change text below to show the name of the user instead of ID
        binding.usernameDialogTextView.text = currentUserId

        binding.textButtonContinue.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }

    }
}