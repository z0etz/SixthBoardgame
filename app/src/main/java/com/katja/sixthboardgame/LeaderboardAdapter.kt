package com.katja.sixthboardgame

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth

class LeaderboardAdapter(private var highscores: List<Leaderboard>) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val positionTextView: TextView = itemView.findViewById(R.id.scoreboard_position)
        val usernameTextView: TextView = itemView.findViewById(R.id.scoreboard_username)
        val scoreTextView: TextView = itemView.findViewById(R.id.scoreboard_score)

        private var alertDialog: AlertDialog? = null
        init {
            // Set click listener for itemView
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {

            val context = view.context
            val receiverId = highscores[adapterPosition]

            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid

            // test

            if (receiverId != null) {
                Log.d("LeaderboardAdapter", "User Clicked: $receiverId")
                val popupWindow = CustomPopupWindow(context)
                popupWindow.show()
            } else {
                Log.d("LeaderboardAdapter", "recieverId is null for position: $adapterPosition")
            }
        }
    }




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_score, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val leaderboard = highscores[position]
        holder.positionTextView.text = (position + 1).toString()
        holder.usernameTextView.text = leaderboard.username
        holder.scoreTextView.text = leaderboard.score.toString()
    }

    override fun getItemCount(): Int {
        return highscores.size
    }

    fun updateHighscores(newHighscores: List<Leaderboard>) {
        highscores = newHighscores
        notifyDataSetChanged()
    }
}

class CustomPopupWindow(context: Context) : Dialog(context, R.style.CustomDialog) {

    init {
        setContentView(R.layout.activity_pop_up_invite)

        val startGameButton = findViewById<Button>(R.id.btn_yes)
        startGameButton.setOnClickListener {
            val intent = Intent(context, StartGameActivity::class.java)
            context.startActivity(intent)
            dismiss()
        }

        val cancelButton = findViewById<Button>(R.id.btn_no)
        cancelButton.setOnClickListener{
            dismiss()
        }

        setCanceledOnTouchOutside(false)

        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER)
    }
}

data class Leaderboard(val username: String, val score: Long)