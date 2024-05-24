import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.katja.sixthboardgame.R
import com.katja.sixthboardgame.UserDao

class PendingInviteAdapter(
    private val context: Context,
    private val inviteList: MutableList<String>,
    private val onDeleteClickListener: (Int) -> Unit
) : RecyclerView.Adapter<PendingInviteAdapter.InviteViewHolder>() {

    private val userDao = UserDao()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InviteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pending_invites, parent, false)
        return InviteViewHolder(view)
    }

    override fun onBindViewHolder(holder: InviteViewHolder, position: Int) {
        val userId = inviteList[position]
        // Fetch username associated with the user ID
        userDao.fetchUsernameById(userId) { username ->
            (context as Activity).runOnUiThread {
                holder.bind(username ?: "Unknown")
            }
        }
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
                    // Handle item click
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
}
