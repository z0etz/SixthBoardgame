package com.katja.sixthboardgame

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class UserDao {
    val ID_KEY = "id"
    val USER_NAME_KEY = "UserName"
    val EMAIL_KEY = "email"
    val LEADERBOARD_KEY = "leaderboard" //for leaderboard
    fun addUser(user: User) {
        val dataToStore = HashMap<String, Any>()
        dataToStore.put(ID_KEY, user.id as Any)
        dataToStore.put(USER_NAME_KEY, user.userName as Any)
        dataToStore.put(EMAIL_KEY, user.email as Any)
        dataToStore.put(LEADERBOARD_KEY, user.leaderboard as Any)

        FirebaseFirestore
            .getInstance()
            .document("users/${user.id}")
            .set(dataToStore)
            .addOnSuccessListener { log ->
                Log.w(
                    ContentValues.TAG,
                    "User added to firestore with id: ${user.id}"
                )
            }
            .addOnFailureListener { log -> Log.w(ContentValues.TAG, "Failed to add user to firestore") }
    }

    // Function to fetch a specific user by ID
    fun fetchUserById(userId: String, completion: (User?) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val id = documentSnapshot.getString(ID_KEY) ?: ""
                val UserName = documentSnapshot.getString(USER_NAME_KEY) ?: ""
                val email = documentSnapshot.getString(EMAIL_KEY) ?: ""
                val highscore = documentSnapshot.getLong(LEADERBOARD_KEY)?.toInt() ?: 0
                val user = User(id, UserName, email, highscore)
                // val user = documentSnapshot.toObject(User::class.java)
                completion(user)
            }
            .addOnFailureListener { exception ->
                Log.e(ContentValues.TAG, "Failed to fetch user from Firestore", exception)
                completion(null)
            }
    }

    fun fetchUserNames(completion: (List<String>) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .get()
            .addOnSuccessListener { result ->
                val userList = mutableListOf<String>()
                for (document in result) {
                    val userName = document.getString(USER_NAME_KEY) ?: ""
                    userList.add(userName)
                }
                completion(userList)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to fetch user list from Firestore", exception)
                completion(emptyList())
            }
    }


    fun fetchUserNames2(completion: (List<String>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val userList = mutableListOf<String>()

        db.collection("users")
            .limit(100)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val userName = document.getString(USER_NAME_KEY) ?: ""
                    userList.add(userName)
                }
                completion(userList)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to fetch user list from Firestore", exception)
                completion(emptyList())
            }
    }

    fun fetchLeaderboard(completion: (List<Leaderboard>) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .orderBy("leaderboard", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val highscoreList = mutableListOf<Leaderboard>()
                for (document in result) {
                    val userName = document.getString("UserName") ?: ""
                    val leaderboard = document.getLong("leaderboard")?.toInt() ?: 0
                    val leaderboardEntry = Leaderboard(userName, leaderboard)
                    highscoreList.add(leaderboardEntry)
                }
                completion(highscoreList)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to fetch leaderboard from Firestore", exception)
                completion(emptyList())
            }
    }

    fun fetchUsernameById(userId: String, completion: (String?) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val username = documentSnapshot.getString(USER_NAME_KEY)
                completion(username)
            }
            .addOnFailureListener { exception ->
                Log.e(ContentValues.TAG, "Failed to fetch username from Firestore", exception)
                completion(null)
            }
    }

    fun fetchUserScoreById(userId: String, completion: (Int?) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val score = documentSnapshot.getLong(LEADERBOARD_KEY)?.toInt()
                completion(score)
            }
            .addOnFailureListener { exception ->
                Log.e(ContentValues.TAG, "Failed to fetch user score from Firestore", exception)
                completion(null)
            }
    }

    fun updateUserScoreById(userId: String, increment: Int, completion: (Boolean) -> Unit) {
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

        FirebaseFirestore.getInstance().runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val newScore = (snapshot.getLong(LEADERBOARD_KEY)?.toInt() ?: 0) + increment
            transaction.update(userRef, LEADERBOARD_KEY, newScore)
        }.addOnSuccessListener {
            completion(true)
        }.addOnFailureListener { exception ->
            Log.e(ContentValues.TAG, "Failed to update user score in Firestore", exception)
            completion(false)
        }
    }
    
}


