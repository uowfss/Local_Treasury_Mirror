package com.group29.localtreasury.database

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


class FirebaseDatabase {
    private val db = Firebase.firestore

    fun getdata(){
        db.collection("users")
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

    fun adddata(item:Int){
        val data = mapOf(
            "int" to item,
        )
        db.collection("users")
            .add(data)
            .addOnSuccessListener { documentReference ->
                Log.d("BG-In", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("BG-In", "Error adding document", e)
            }
    }
}