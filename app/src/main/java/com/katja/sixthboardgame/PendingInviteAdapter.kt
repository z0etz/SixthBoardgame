package com.katja.sixthboardgame


import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.inflate
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.katja.sixthboardgame.R.color.transparent
import kotlinx.coroutines.withContext


    class PendingInviteAdapter(
        private val context: Context,
        private val inviteList: MutableList<String>,
        private val receiverId: String,
        private val onDeleteClickListener: (Int) -> Unit
    ) : RecyclerView.Adapter<PendingInviteAdapter.InviteViewHolder>() {



        interface OnItemInteractionListener {
            fun onSendButtonClick(position: Int)
        }

        private var listener: OnItemInteractionListener? = null



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
            val button = itemView.findViewById<Button>(R.id.invite_send_button)


            init {
                button.setOnClickListener {
                    button.text = "invitation sent"
                    button.background =  R.color.transparent.toDrawable() // R.drawable.button_white.toDrawable()
                    val position = adapterPosition
                    listener?.onSendButtonClick(position)

                }
            }
            init {
                itemView.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        showGameDialog(receiverId, inviteList[position])
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

        fun setOnItemInteractionListener(listener: OnItemInteractionListener) {
            this.listener = listener
        }

        fun updateInvitationsList(newInvites: List<String>) {
            inviteList.clear()
            inviteList.addAll(newInvites)
            notifyDataSetChanged()
        }

        private fun openGame(gameId: String) {
            val intent = Intent(context, GameActivity::class.java)
            intent.putExtra("GAME_ID", gameId)
            context.startActivity(intent)
        }

        private fun showGameDialog(senderId: String, receiverId: String) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val currentUserId = currentUser?.uid

        if (currentUserId != null) {
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.activity_game_dialog)

                val buttonContinue = dialog.findViewById<TextView>(R.id.textButtonContinue)
                val buttonCancel = dialog.findViewById<TextView>(R.id.textButtonCancel)

            buttonContinue.setOnClickListener {
                // Create a new a new game and add it to Firestore
                GameDao().addGame(currentUserId, receiverId)
                // Close dialog
                dialog.dismiss()
            }

            buttonCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        } else {
            // Handle case when current user ID is null
            Toast.makeText(context, "Current user ID is null", Toast.LENGTH_SHORT).show()
        }
    }
}
