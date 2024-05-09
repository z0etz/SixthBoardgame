package com.katja.sixthboardgame

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
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
    private lateinit var originalList: List<String>
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private val userMap = mutableMapOf<String?, String?>()
    private lateinit var firestore: FirebaseFirestore
    private var selectedUsersList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = Firebase.auth
        userDao = UserDao()

        userDao.fetchUserNames { names ->
            userNameList = names
            firestore = FirebaseFirestore.getInstance()
            autoCompleteTextView = binding.autoTv
            getAllUsers()
        }

        autoCompleteTextView = binding.autoTv
        adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line)
        autoCompleteTextView.setAdapter(adapter)

        autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
            val selectedUser = parent.getItemAtPosition(position) as String
            val userId2: String? = userMap[selectedUser]
            val userArray = arrayListOf(selectedUser, userId2)
            // Uppdatera listan med valda användare
            selectedUsersList.add(selectedUser)
            // Meddela RecyclerView-adaptern att data har ändrats
            adapter.notifyDataSetChanged()
        }

        val recyclerView: RecyclerView = findViewById(R.id.invitesRecyclerView)
        val layoutManager = LinearLayoutManager(this)
        val adapter = PendingInviteAdapter(selectedUsersList) // Uppdatera med valda användare
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
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
                originalList = usersList
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
    override fun onResume() {
        super.onResume()
        autoCompleteTextView.setText("")
    }
}
