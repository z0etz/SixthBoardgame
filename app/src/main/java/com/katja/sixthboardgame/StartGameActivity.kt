package com.katja.sixthboardgame

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.katja.sixthboardgame.databinding.ActivityStartGameBinding

class StartGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartGameBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userDao: UserDao
    private var userNameList: List<String>? = null
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private val userMap = mutableMapOf<String?, String?>()
    private lateinit var firestore: FirebaseFirestore
    private var selectedUsersList = mutableListOf<String>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var pendingInviteAdapter: PendingInviteAdapter
    private lateinit var sentInviteAdapter: SentInviteAdapter
    private lateinit var inviteDao: InviteDao
    private val invitationsCollection = FirebaseFirestore.getInstance().collection("game_invitations")
    private var receiverId: String? = null
    private var sentInvitesList = mutableListOf<String>()
    private val inviteMap = mutableMapOf<String, Invite>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = Firebase.auth
        userDao = UserDao()
        inviteDao = InviteDao()

        //Set up received invites list
        autoCompleteTextView = binding.autoTv
        adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line)
        autoCompleteTextView.setAdapter(adapter)

        recyclerView = findViewById(R.id.invitesRecyclerView)
        pendingInviteAdapter = PendingInviteAdapter(this, selectedUsersList, receiverId ?: "", onDeleteClickListener = { position ->
            val receiverName = selectedUsersList[position]
            val receiverId = userMap[receiverName]
            receiverId?.let {
                deleteInvite(firebaseAuth.currentUser?.uid!!, it)
            }
        }
        )
        recyclerView.adapter = pendingInviteAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        firestore = FirebaseFirestore.getInstance()


        //Set up sent invites list
        val sentRecyclerView = findViewById<RecyclerView>(R.id.sentInvitesRecyclerView)
        sentInviteAdapter = SentInviteAdapter(this, sentInvitesList) { position ->
            val invite = sentInvitesList[position]
            showSentInviteDialog(invite)
        }
        sentRecyclerView.adapter = sentInviteAdapter
        sentRecyclerView.layoutManager = LinearLayoutManager(this)

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            inviteDao.listenForSentInvitations(currentUser.uid) { sentInvitations ->
                processSentInvitations(sentInvitations)
            }
            inviteDao.listenForInvitations(currentUser.uid) { receivedInvitations ->
                processReceivedInvitations(receivedInvitations)
            }
        }

        userDao.fetchUserNames { names ->
            userNameList = names.distinct() // Remove duplicates
            Log.d("StartGameActivity", "Unique user names: $userNameList")
            adapter.clear() // Clear existing data
            userNameList?.let {
                adapter.addAll(it)
                Log.d("StartGameActivity", "Adapter populated with: $it")
            }
        }



        autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
            val selectedUser = parent.getItemAtPosition(position) as String
            getReceiverId(selectedUser) // Update receiverId
            val senderId = firebaseAuth.currentUser?.uid

            if (senderId != null) {
                if (receiverId == senderId) {
                    Toast.makeText(this, "You cannot send an invitation to yourself.", Toast.LENGTH_SHORT).show()
                } else {
                    receiverId?.let {
                        val inviteId = invitationsCollection.document().id
                        inviteDao.sendInvitation(senderId, receiverId ?: "Unknown", inviteId) { invitationData ->
                            // Convert invitationData to Invite object
                            val invite = Invite(
                                inviteId = invitationData[inviteDao.INVITE_ID_KEY] as String,
                                senderId = invitationData[inviteDao.SENDER_ID_KEY] as String,
                                receiverId = invitationData[inviteDao.RECEIVER_ID_KEY] as String,
                                status = invitationData[inviteDao.STATUS_KEY] as String
                            )

                            // Add invite to inviteMap
                            inviteMap[inviteId] = invite
                            }
                        } ?: run {
                        Toast.makeText(this, "Receiver ID not found", Toast.LENGTH_SHORT).show()
                    }
                    // line below is the culprit to the infamous bug of the showing sender.
                    // selectedUsersList.add(selectedUser)
                    pendingInviteAdapter.notifyDataSetChanged()
                }
            } else {
                Toast.makeText(this, "Sender ID is null", Toast.LENGTH_SHORT).show()
            }
        }


        getAllUsers()
    }

   private fun showSentInviteDialog(inviteId: String) {
       println("Clicked inviteId: $inviteId")
       println("Invite details: ${inviteMap[inviteId]}")

       val invite = inviteMap[inviteId]

       if (invite != null) {
           val dialog = Dialog(this)
           dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
           dialog.setCancelable(false)
           dialog.setContentView(R.layout.activity_cancel_invite)

           val buttonDelete = dialog.findViewById<TextView>(R.id.textButtonDelete)
           val buttonCancel = dialog.findViewById<TextView>(R.id.textButtonCancel)

           buttonDelete.setOnClickListener {
               // Handle deletion logic here
               deleteInvite(invite.senderId, invite.receiverId)
               dialog.dismiss()
           }

           buttonCancel.setOnClickListener {
               dialog.dismiss()
           }

           dialog.show()
       } else {
           // Logging: Print message if invite not found
           Log.d("InviteDialog", "Invite not found for inviteId: $inviteId")
           Toast.makeText(this, "Invite not found", Toast.LENGTH_SHORT).show()
       }
   }

    private fun getReceiverId(selectedUser: String) {
        receiverId = userMap[selectedUser]
    }

    private fun getAllUsers() {
        val usersCollection = firestore.collection("users")
        usersCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val usersList = mutableListOf<String>()
                for (document in querySnapshot.documents) {
                    val fullName = document.getString("UserName")
                    val user2Id = document.getString("id")
                    if (!userMap.containsKey(fullName)) {
                        fullName?.let { usersList.add(it) }
                        userMap[fullName] = user2Id
                    }
                }
                adapter.clear() // clear old data
                adapter.addAll(usersList.distinct()) // Add actual names with specific id
                adapter.notifyDataSetChanged() // Notify adapter for changes
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Failed to fetch users: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun processReceivedInvitations(invitations: List<Map<String, Any>>) {
        val incomingInvites = mutableListOf<String>()
        val currentUser = firebaseAuth.currentUser
        val currentUserId = currentUser?.uid

        for (invitation in invitations) {
            val senderId = invitation[inviteDao.SENDER_ID_KEY] as String
            val receiverId = invitation[inviteDao.RECEIVER_ID_KEY] as String

            if (currentUserId == receiverId) {
                incomingInvites.add(senderId)
            }
        }

        // Add all invites to the list
        pendingInviteAdapter.updateInvitationsList(incomingInvites)
    }

    private fun processSentInvitations(sentInvitations: List<Map<String, Any>>) {
        val sentInvites = mutableListOf<String>()
        val currentUser = firebaseAuth.currentUser
        val currentUserId = currentUser?.uid

        inviteMap.clear()

        for (invitation in sentInvitations) {
            val inviteId = invitation[inviteDao.INVITE_ID_KEY] as String
            val senderId = invitation[inviteDao.SENDER_ID_KEY] as String
            val receiverId = invitation[inviteDao.RECEIVER_ID_KEY] as String
            val status = invitation[inviteDao.STATUS_KEY] as String

            val invite = Invite(inviteId, senderId, receiverId, status)
            inviteMap[inviteId] = invite

            // Add logic here to ensure that the invitation was sent by the current user
            if (currentUserId == senderId) {
                // Add the receiverId to the list of sent invites
                sentInvites.add(inviteId)
            }
        }
        // Logging: Print contents of inviteMap
        Log.d("InviteMap", "InviteMap: $inviteMap")
        // Update the UI with the sent invites
        sentInviteAdapter.updateInvitationsList(sentInvites)
    }

    private fun deleteInvite(senderId: String, receiverId: String) {
        // Delete invitation from Firestore
        inviteDao.deleteInvitation(senderId, receiverId)
            .addOnSuccessListener {
                val position = selectedUsersList.indexOf(receiverId)
                if (position != -1) {
                    selectedUsersList.removeAt(position)
                    pendingInviteAdapter.notifyItemRemoved(position)
                }
                Toast.makeText(
                    this,
                    "Invitation deleted successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { exception ->
                Log.e("DeleteInvite", "Failed to delete invitation: ${exception.message}", exception)
                Toast.makeText(
                    this,
                    "Failed to delete invitation: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onResume() {
        super.onResume()
        autoCompleteTextView.setText("")
    }
}
