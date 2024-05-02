package com.katja.sixthboardgame

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.katja.sixthboardgame.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var fireBaseAuth: FirebaseAuth
    private lateinit var userDao: UserDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fireBaseAuth = Firebase.auth
        userDao = UserDao()

        binding.logInTextButton.setOnClickListener {

            // welcome activity should be replaced with logInActivity when it is available
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }

        binding.signUpButton.setOnClickListener {
            register()
        }



    }

    fun register(){

        val username = binding.etUsername.text.toString().trim()
        val usermail = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassWord = binding.etConfirmPassword.text.toString().trim()

        if(username.isEmpty() || usermail.isEmpty() || password.isEmpty() || confirmPassWord.isEmpty()){

            Toast.makeText(this, "please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        if(password != confirmPassWord){

            Toast.makeText(this,"Passwords don't match ", Toast.LENGTH_SHORT).show()
            return
        }


        fireBaseAuth.createUserWithEmailAndPassword(usermail, password)
            .addOnSuccessListener { authResult ->
                val user = fireBaseAuth.currentUser

                val newUser = User(user?.uid.toString(), username, usermail)
                userDao.addUser(newUser)

                Toast.makeText(this, "Welcome: $username", Toast.LENGTH_SHORT).show()

                // should replace the welcome Activity wtih the right activity
                val intent = Intent(this, WelcomeActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to sign up: ${exception.message}", Toast.LENGTH_SHORT)
                    .show()
            }


    }
}