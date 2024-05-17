package com.katja.sixthboardgame

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.katja.sixthboardgame.databinding.FragmentWinDialogBinding

class WinDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentWinDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fetch user information
        val auth = FirebaseAuth.getInstance()
        val dao = UserDao()
        val currentUserId = auth.currentUser?.uid

        println(currentUserId)

        // Show username in dialog
        dao.fetchUsernameById(currentUserId ?: "Unknown") { username ->
            if (username != null) {
                Log.d("WinDialogFragment", "Username: $username")
                binding.usernameDialogTextView.text = username
            } else {
                Log.e("WinDialogFragment", "Failed to get username")
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWinDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textButtonContinue.setOnClickListener {
            val intent = Intent(activity, WelcomeActivity::class.java)
            startActivity(intent)
            activity?.finish() // Finish GameActivity
            dismiss() // Dismiss the dialog
        }
    }
}