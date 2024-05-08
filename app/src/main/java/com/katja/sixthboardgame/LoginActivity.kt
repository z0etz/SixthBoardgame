package com.katja.sixthboardgame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
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
//        if (auth.currentUser != null){
//
//            val intent = Intent(this, WelcomeActivity::class.java)
//            startActivity(intent)
//            finish()
//        }

        binding.logInButton.setOnClickListener {
            val userMail = binding.etUsermail.text.toString()
            val password = binding.etPassword.text.toString()
            authenticateUser(userMail, password)
        }

        binding.goToSignUpTextButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }


    }


    fun authenticateUser(username: String, password: String) {
        if (!Patterns.EMAIL_ADDRESS.matcher(username).matches() || password.isEmpty()) {
            Toast.makeText(
                this, "Both or one Field is incorrect/empty",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            login(username, password)
        }

    }

    fun login(username: String, password: String) {

        auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(username, password)
            .addOnSuccessListener {
                val intent = Intent(this, WelcomeActivity::class.java)
                startActivity(intent)
                finish()
            }.addOnFailureListener { error ->
                Toast.makeText(
                    this, "Email or password is not correct",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}


/*
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
*/
