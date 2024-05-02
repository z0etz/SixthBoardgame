package com.katja.sixthboardgame

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.katja.sixthboardgame.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.logInTextButton.setOnClickListener {

            // welcome activity should be replaced with logInActivity when it is available
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }

    }
}