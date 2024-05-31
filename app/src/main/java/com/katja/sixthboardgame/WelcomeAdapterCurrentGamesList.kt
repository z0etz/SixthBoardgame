package com.katja.sixthboardgame

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.katja.sixthboardgame.databinding.ListItemGameBinding
import java.util.Date

class WelcomeAdapterCurrentGamesList(private val context: Context, private val dataList: List<Game>, private val onItemClick: (String) -> Unit,private val fetchOpponentName: (String, (String) -> Unit) -> Unit)   : RecyclerView.Adapter<WelcomeAdapterCurrentGamesList.ViewHolder>() {

    private var handler: Handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable

    // Here you can adjust the time.
    init {
        runnable = object : Runnable {
            override fun run() {
                notifyDataSetChanged()  // This will refresh the RecyclerView each second
                handler.postDelayed(this, 1000)
            }
        }
        handler.postDelayed(runnable, 1000)
    }

    // ViewHolder class to hold references to the views in the item layout using View Binding
    class ViewHolder(private val binding: ListItemGameBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(context: Context, game: Game, onItemClick: (String) -> Unit, fetchOpponentName: (String, (String) -> Unit) -> Unit) {
            val opponentId = game.playerIds.firstOrNull { it != FirebaseAuth.getInstance().currentUser?.uid } ?: "Unknown"
            fetchOpponentName(opponentId) { opponentName ->
                binding.gamelistOpponent.text = opponentName
            }

            //val timeLeft = game.getTimeLeft()

            val timeSinceLastTurn = Date().time - game.lastTurnTime.time
            val timeLeft = game.turnTime - timeSinceLastTurn


            if (timeLeft < 0){
                binding.gamelistTimeLeft.text = "Game ended"
            }
            else {



                if(game.nextPlayer != opponentId) {
                    val hours = timeLeft / (1000 * 60 * 60)
                    val minutes = (timeLeft % (1000 * 60 * 60)) / (1000 * 60)
                    val seconds = (timeLeft % (1000 * 60)) / 1000
                    binding.gamelistTimeLeft.text =
                        String.format("%02d:%02d:%02d", hours, minutes, seconds)
                }
                else{
                    binding.gamelistTimeLeft.text = "It is opponent's turn"
                }
            }




            itemView.setOnClickListener {
                onItemClick(game.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemGameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(context, dataList[position], onItemClick, fetchOpponentName)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun stopUpdating() {
        handler.removeCallbacks(runnable)
    }
}