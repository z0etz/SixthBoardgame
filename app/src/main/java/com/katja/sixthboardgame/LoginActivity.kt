package com.katja.sixthboardgame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.katja.sixthboardgame.databinding.ActivityLoginBinding
import com.katja.sixthboardgame.databinding.ActivitySignUpBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        if (auth.currentUser != null){


            auth.signOut()

        }

        binding.logInButton.setOnClickListener {
            logIn()
        }

        binding.goToSignUpTextButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }


    }

    fun logIn(){
        val userMail = binding.etUsermail.text.toString()
        val password = binding.etPassword.text.toString()

        if(userMail.isEmpty() || password.isEmpty()){

            Toast.makeText(this, "Password or mail is missing", Toast.LENGTH_SHORT).show()
            return
        }




        auth.signInWithEmailAndPassword(userMail, password)
            .addOnSuccessListener{ AuthResult ->

                Toast.makeText(this, "Welcome to the game", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, WelcomeActivity::class.java)
                startActivity(intent)
                finish()

            }
            .addOnFailureListener{ exception ->
                println("failed to log user in ${exception.message}")
                Toast.makeText(this, "failed to login:  ${exception.message}", Toast.LENGTH_SHORT).show()
            }



    }
}