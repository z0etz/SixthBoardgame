package com.katja.sixthboardgame

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.katja.sixthboardgame.databinding.ActivityHighscoreBinding

class HighscoreActivity : AppCompatActivity() {

    lateinit var binding: ActivityHighscoreBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: ArrayAdapter<String>
    private val userDao = UserDao()
    private var receiverId: String? = null
    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private lateinit var firebaseAuth: FirebaseAuth
    private val invitationsCollection = FirebaseFirestore.getInstance().collection("game_invitations")
    private var selectedUsersList = mutableListOf<Invite>()
    private lateinit var pendingInviteAdapter: PendingInviteAdapter
    private val userMap = mutableMapOf<String?, String?>()
    private val inviteDao = InviteDao()
    private val gameDao = GameDao()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHighscoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)

        pendingInviteAdapter = PendingInviteAdapter(
            this,
            selectedUsersList,
            receiverId ?: "",
            onDeleteClickListener = { position ->
                val invite = selectedUsersList[position]
                val receiverId = invite.receiverId
                receiverId.let {
                }
            },
            inviteDao = inviteDao,
            userDao = userDao,
            gameDao = gameDao
        )
        getAllUsers()

        leaderboardAdapter = LeaderboardAdapter(
            emptyList(),
            this,
            firebaseAuth,
            userMap,
            invitationsCollection,
            selectedUsersList,
            pendingInviteAdapter
        )

        binding.recyclerViewHighscore.adapter = leaderboardAdapter
        binding.recyclerViewHighscore.layoutManager = LinearLayoutManager(this)

        fetchAndDisplayLeaderboard()
    }

    private fun fetchAndDisplayLeaderboard() {
        userDao.fetchLeaderboard { leaderboard ->
            leaderboardAdapter.updateHighscores(leaderboard)
        }
    }

    private fun getAllUsers() {
        val usersCollection = firestore.collection("users")
        usersCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val usersList = mutableListOf<String>()
                for (document in querySnapshot.documents) {
                    val fullName = document.getString("UserName")
                    val user2Id = document.getString("id")
                    fullName?.let { usersList.add(it) }
                    userMap[fullName] = user2Id
                }
                adapter.addAll(usersList)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Failed to fetch users: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
