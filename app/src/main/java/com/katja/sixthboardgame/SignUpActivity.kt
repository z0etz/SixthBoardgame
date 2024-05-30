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
    private lateinit var userNameList: List<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fireBaseAuth = Firebase.auth
        userDao = UserDao()

        // TODO: This takes time, see if it can be handled faster somehow.
        userDao.fetchUserNames {

            userNameList = it
            println(userNameList.toString())
        }

        binding.logInTextButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.signUpButton.setOnClickListener {
            register()
        }
    }

    fun register() {

        val userName = binding.etUsername.text.toString().trim()
        val usermail = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val highscore = 0
        val confirmPassWord = binding.etConfirmPassword.text.toString().trim()

        if (userName.isEmpty() || usermail.isEmpty() || password.isEmpty() || confirmPassWord.isEmpty()) {

            Toast.makeText(this, "please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // All following code needs to be kept inside this function to avoid conflict
        // between asynchronous code and synchronous code
            for (oldUserName in userNameList) {
                println(oldUserName)
                println(oldUserName.indices)
                if (oldUserName == userName) {
                    Toast.makeText(this, "Username is taken", Toast.LENGTH_SHORT).show()
                    return
                }
            }
            if (password != confirmPassWord) {

                Toast.makeText(this, "Passwords don't match ", Toast.LENGTH_SHORT).show()
                return
            }

            fireBaseAuth.createUserWithEmailAndPassword(usermail, password)
                .addOnSuccessListener { authResult ->
                    val user = fireBaseAuth.currentUser

                    val newUser = User(user?.uid.toString(), userName, usermail, highscore)
                    userDao.addUser(newUser)

                    Toast.makeText(this, "Welcome: $userName", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, WelcomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Failed to sign up: ${exception.message}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
    }
}