package com.group29.localtreasury.database

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject


class FirebaseDatabase {
    companion object{
        val CHAT = "Chat"
        val LISTING = "Listing"
        val LOGINFAILED = "FailedLogin"
        val SIGNUPFAILED = "FailedSignUp"
    }



    private val db = Firebase.firestore
    private val firebaseAuthentication = Firebase.auth

    fun getChats(){
        db.collection(CHAT)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("BG-Out", "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("BG-Out", "Error getting documents.", exception)
            }

    }

    fun adddata(item:ChatObject){
        db.collection(CHAT)
            .add(item)
            .addOnSuccessListener { documentReference ->
                Log.d("BG-In", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("BG-In", "Error adding document", e)
            }
    }

    fun signIn(email: String, password: String){
        firebaseAuthentication.signInWithEmailAndPassword(email, password).addOnSuccessListener { authentication ->
            Log.d("BG", authentication.user!!.uid)
        }.addOnFailureListener(){

        }
    }

    fun createAccount(email: String, password: String) {
        firebaseAuthentication.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authentication ->
                // On Success

            }.addOnFailureListener(){
                // On Failed Signup
            }
    }
}