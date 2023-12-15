package com.example.rental_app.repositories

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.example.rental_app.models.User
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.toObject
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await

class UserRepository(private val context: Context) {
    private val TAG = this.toString();
    //get an instance of firestore database
    private val db = Firebase.firestore

    private val COLLECTION_USERS = "Users"
    private val FIELD_FIRST_NAME = "firstName"
    private val FIELD_LAST_NAME = "lastName"
    private val FIELD_EMAIL = "email"
    private val FIELD_PHONE_NUMBER = "phoneNumber"
    private val FIELD_PASSWORD = "password"
    private val FIELD_FAVOURITES = "shortListing"
    private val FIELD_LISTINGS = "listings"
    val gson = Gson()

    fun addUserToDB(newUser: User) {
        try {
            val data: MutableMap<String, Any> = HashMap()

            data[FIELD_EMAIL] = newUser.email
            data[FIELD_FIRST_NAME] = newUser.firstName
            data[FIELD_LAST_NAME] = newUser.lastName
            data[FIELD_PHONE_NUMBER] = newUser.phoneNumber
            data[FIELD_PASSWORD] = newUser.password
            data[FIELD_FAVOURITES] = newUser.shortListing
            data[FIELD_LISTINGS] = newUser.listings

            db.collection(COLLECTION_USERS)
                .document(newUser.email)
                .set(data)
                .addOnSuccessListener { docRef ->
                    Log.d(TAG, "addUserToDB: New user document created with ID: $docRef")
                }
                .addOnFailureListener{ex ->
                    Log.d(TAG, "addUserToDB: Unable to create user document. Error: $ex")
                }

        } catch (ex : java.lang.Exception) {
            Log.e(TAG, "addUserToDB: Unable to create user : $ex", )
        }
    }

    suspend fun getLoggedInUser(userEmail: String?): User? {
        if(!userEmail.isNullOrEmpty()) {
            val userRef = db.collection(COLLECTION_USERS).document(userEmail)
            return try {
                val documentSnapshot = userRef.get().await()
                if(documentSnapshot.exists()) {
                    val userData = documentSnapshot.toObject(User::class.java)
                    userData
                } else {
                    null
                }

            } catch (ex: java.lang.Exception) {
                Log.e(TAG, "getLoggedInUser: Unable to get user : $ex",)
                null
            }
        }
        return null
    }

    suspend fun getLoggedByEmail(userEmail: String?): User? {
        if(!userEmail.isNullOrEmpty()) {
            val userRef = db.collection(COLLECTION_USERS).document(userEmail)
            return try {
                val documentSnapshot = userRef.get().await()
                if(documentSnapshot.exists()) {
                    val userData = documentSnapshot.toObject(User::class.java)
                    userData
                } else {
                    null
                }

            } catch (ex: java.lang.Exception) {
                Log.e(TAG, "getLoggedInUser: Unable to get user : $ex",)
                null
            }
        }
        return null
    }
}