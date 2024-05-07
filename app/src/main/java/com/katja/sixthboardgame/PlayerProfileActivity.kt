package com.katja.sixthboardgame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.katja.sixthboardgame.databinding.ActivityPlayerProfileBinding


    class PlayerProfileActivity : AppCompatActivity() {

        lateinit var auth: FirebaseAuth
        lateinit var binding: ActivityPlayerProfileBinding
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityPlayerProfileBinding.inflate(layoutInflater)
            setContentView(binding.root)

            auth = FirebaseAuth.getInstance()

            binding.textButtonSignOut.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
             }
            
            binding.textButtonDeleteAccount.setOnClickListener{
                val user = auth.currentUser
                user?.delete()
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        }else{
                            Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show()
                            val exception = task.exception
                            Log.e("DeleteAccount","Failed to delete account",exception)
                        }
                    }
            }



        }


    }