package com.katja.sixthboardgame


import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.inflate
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.withContext


class PendingInviteAdapter(
    private val context: Context,
    private val inviteList: MutableList<String>,
    private val onDeleteClickListener:
    (Int) -> Unit) : RecyclerView.Adapter<PendingInviteAdapter.InviteViewHolder>() {
    lateinit var gameDao: GameDao
    lateinit var game: Game





    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InviteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pending_invites, parent, false)
        return InviteViewHolder(view)
    }

    override fun onBindViewHolder(holder: InviteViewHolder, position: Int) {
        holder.bind(inviteList[position])
    }

    override fun getItemCount(): Int {
        return inviteList.size
    }

    inner class InviteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerNameTextView: TextView = itemView.findViewById(R.id.invitePlayerName)

        init {
            itemView.setOnClickListener{
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION){
                    showGameDialog(inviteList[position])
                }
            }
        }

        init {
            itemView.setOnLongClickListener {
                onDeleteClickListener(adapterPosition)

                true
            }
        }

        fun bind(playerName: String) {
            playerNameTextView.text = playerName
        }
    }



    fun updateInvitationsList(newInvites: List<String>) {
        inviteList.clear()
        inviteList.addAll(newInvites)
        notifyDataSetChanged()
    }

    private fun showGameDialog(playerName: String){
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.activity_game_dialog)

        val buttonContinue = dialog.findViewById<TextView>(R.id.textButtonContinue)
        val buttonCancel = dialog.findViewById<TextView>(R.id.textButtonCancel)

        buttonContinue.setOnClickListener{
            val intent = Intent(context, GameActivity::class.java)
            gameDao.addGame(game)
            context.startActivity(intent)

        }
        buttonCancel.setOnClickListener(){
            dialog.dismiss()
        }

        dialog.show()
    }
}

