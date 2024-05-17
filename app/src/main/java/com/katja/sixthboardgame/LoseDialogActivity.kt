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
        val dao = UserDao()
        val currentUserId = auth.currentUser?.uid
        var currentUserName = getString(R.string.username_not_loaded)

        println(currentUserId)

        dao.fetchUsernameById(currentUserId?: "Unknown") { username ->
            if (username != null) {
                currentUserName = username
                println("Username: $username")
            } else {
                println("Failed to get username")
            }
        }

        binding.usernameDialogTextView.text = currentUserName

        binding.textButtonContinue.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }

    }
}