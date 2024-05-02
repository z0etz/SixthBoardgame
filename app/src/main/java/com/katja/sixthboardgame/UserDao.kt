package com.katja.sixthboardgame

import android.content.ContentValues
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class UserDao {


    val ID_KEY = "id"
    val USER_NAME_KEY = "UserName"
    val EMAIL_KEY = "email"
    fun addUser(user: User) {
        val dataToStore = HashMap<String, Object>()
        dataToStore.put(ID_KEY, user.id as Object)
        dataToStore.put(USER_NAME_KEY, user.userName as Object)
        dataToStore.put(EMAIL_KEY, user.email as Object)

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
                val user = User(id, UserName, email)
                // val user = documentSnapshot.toObject(User::class.java)
                completion(user)
            }
            .addOnFailureListener { exception ->
                Log.e(ContentValues.TAG, "Failed to fetch user from Firestore", exception)
                completion(null)
            }
    }
}