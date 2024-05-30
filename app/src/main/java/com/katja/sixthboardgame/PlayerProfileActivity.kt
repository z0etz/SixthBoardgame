package com.katja.sixthboardgame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.katja.sixthboardgame.databinding.ActivityPlayerProfileBinding


class PlayerProfileActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    private lateinit var dao: UserDao
    lateinit var binding: ActivityPlayerProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        dao = UserDao()

        val currentUserId = auth.currentUser?.uid
        println(currentUserId)

        // Show username
        dao.fetchUsernameById(currentUserId ?: "Unknown") { username ->
            if (username != null) {
                Log.d("PlayerProfileActivity", "Username: $username")
                binding.textViewUsername.text = username
            } else {
                Log.e("PlayerProfileActivity", "Failed to get username")
            }
        }

        //Show current score
        dao.fetchUserScoreById(currentUserId ?: "Unknown") { score ->
            if (score != null) {
                Log.d("PlayerProfileActivity", "Score: $score")
                val scoreString = getString(R.string.score) + " " + score.toString()
                binding.textViewScore.text = scoreString
            } else {
                Log.e("PlayerProfileActivity", "Failed to get score")
            }
        }

        binding.textButtonSignOut.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.textButtonDeleteAccount.setOnClickListener {
            val user = auth.currentUser
            user?.delete()
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val userId = user.uid
                        val userDocRef =
                            FirebaseFirestore.getInstance().collection("users").document(userId)
                        userDocRef.delete()
                        Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show()
                        val exception = task.exception
                        Log.e("DeleteAccount", "Failed to delete account", exception)
                    }
                }
        }
    }
}