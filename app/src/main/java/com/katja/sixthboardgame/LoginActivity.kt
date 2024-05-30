package com.katja.sixthboardgame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.katja.sixthboardgame.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        autoLogin()


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


    private fun authenticateUser(username: String, password: String) {
        if (!Patterns.EMAIL_ADDRESS.matcher(username).matches() || password.isEmpty()) {
            Toast.makeText(
                this, "Both or one Field is incorrect/empty",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            login(username, password)
        }
    }

    private fun login(username: String, password: String) {

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

    private fun autoLogin(){
        if (auth.currentUser != null){

            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
      }
    }
}
