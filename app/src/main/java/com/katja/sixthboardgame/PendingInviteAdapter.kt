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
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.withContext

    class PendingInviteAdapter(
        private val context: Context,
        private val inviteList: MutableList<String>,
        private val receiverId: String,
        private val onDeleteClickListener: (Int) -> Unit
    ) : RecyclerView.Adapter<PendingInviteAdapter.InviteViewHolder>() {

        lateinit var gameDao: GameDao
        lateinit var firestore: FirebaseFirestore
        lateinit var game: Game
        lateinit var userDao: UserDao

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

            if (!this::game.isInitialized) {
                Log.e("InviteAdapter", "Game property not initialized")
                // Handle this situation appropriately
                Toast.makeText(context, "Game property is not initialized", Toast.LENGTH_SHORT).show()
                return
            }

            gameDao = GameDao()
            userDao = UserDao()
            val gameId = FirebaseFirestore.getInstance().document("Games/${game.id}").toString()

            if (currentUserId != null) {
                Log.d("InviteAdapter", "Showing game dialog for receiver ID: $receiverId")
                val dialog = Dialog(context)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCancelable(false)
                dialog.setContentView(R.layout.activity_game_dialog)

                val buttonContinue = dialog.findViewById<TextView>(R.id.textButtonContinue)
                val buttonCancel = dialog.findViewById<TextView>(R.id.textButtonCancel)

                buttonContinue.setOnClickListener {
                    Log.d("InviteAdapter", "Continue button clicked. Creating and adding new game.")
                    val newGame = Game(UserDao(), listOf(currentUserId, receiverId))
                    GameDao().addGame(newGame)
                    gameDao.fetchGameById(gameId) { game ->
                        openGame(game.id)
                    }
                    dialog.dismiss()
                }

                buttonCancel.setOnClickListener {
                    Log.d("InviteAdapter", "Cancel button clicked. Dismissing dialog.")
                    dialog.dismiss()
                }

                dialog.show()
            } else {
                Log.e("InviteAdapter", "Current user ID is null. Cannot show game dialog.")
                Toast.makeText(context, "Current user ID is null", Toast.LENGTH_SHORT).show()
            }
        }
    }

