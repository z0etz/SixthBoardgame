package com.katja.sixthboardgame

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil.setContentView
import androidx.recyclerview.widget.RecyclerView

class LeaderboardAdapter(private var highscores: List<Leaderboard>, private val onUserClick: (String) -> Unit) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {



    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val positionTextView: TextView = itemView.findViewById(R.id.scoreboard_position)
        val usernameTextView: TextView = itemView.findViewById(R.id.scoreboard_username)
        val scoreTextView: TextView = itemView.findViewById(R.id.scoreboard_score)
        val view: View = itemView

        init {
            // Set click listener for itemView
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val selectedUser = highscores[adapterPosition].username
            onUserClick(selectedUser)

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



data class Leaderboard(val username: String, val score: Int)