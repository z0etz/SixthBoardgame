package com.katja.sixthboardgame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HighscoreAdapter(private val highscores: List<Highscore>) : RecyclerView.Adapter<HighscoreAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val positionTextView: TextView = itemView.findViewById(R.id.scoreboard_position)
        val usernameTextView: TextView = itemView.findViewById(R.id.scoreboard_username)
        val scoreTextView: TextView = itemView.findViewById(R.id.scoreboard_score)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_score, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val highscore = highscores[position]
        holder.positionTextView.text = (position + 1).toString()
        holder.usernameTextView.text = highscore.username
        holder.scoreTextView.text = highscore.score.toString()
    }

    override fun getItemCount(): Int {
        return highscores.size
    }
}
data class Highscore(val username: String, val score: Int)