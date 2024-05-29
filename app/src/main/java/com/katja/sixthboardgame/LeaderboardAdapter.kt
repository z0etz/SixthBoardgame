package com.katja.sixthboardgame

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference

class LeaderboardAdapter(
    private var highscores: List<Leaderboard>,
    private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val userMap: Map<String?, String?>,
    private val invitationsCollection: CollectionReference,
    private val selectedUsersList: MutableList<Invite>,
    private val pendingInviteAdapter: PendingInviteAdapter
) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val positionTextView: TextView = itemView.findViewById(R.id.scoreboard_position)
        val usernameTextView: TextView = itemView.findViewById(R.id.scoreboard_username)
        val scoreTextView: TextView = itemView.findViewById(R.id.scoreboard_score)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val leaderboard = highscores[position]
                val selectedUser = leaderboard.username

                pendingInviteAdapter.showPopup(
                    context,
                    selectedUser,
                    userMap,
                    firebaseAuth,
                    invitationsCollection,
                    selectedUsersList,
                    pendingInviteAdapter
                )
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

class CustomPopupWindow(context: Context) : Dialog(context) {

    init {
        // Set the custom layout for the popup window
        setContentView(R.layout.activity_pop_up_invite)

        // Find the "Start Game" button and set its click listener
        val startGameButton = findViewById<TextView>(R.id.btn_yes)
        startGameButton.setOnClickListener {
            // Start the game activity (replace with your desired action)
            val intent = Intent(context, StartGameActivity::class.java)
            context.startActivity(intent)
            // Dismiss the popup window
            dismiss()
        }

        val cancelButton = findViewById<TextView>(R.id.btn_no)
        cancelButton.setOnClickListener{
            dismiss()
        }

        // Ensure that the popup window is not dismissed when clicking outside of it
        setCanceledOnTouchOutside(false)
    }

}

data class Leaderboard(val username: String, val score: Int)