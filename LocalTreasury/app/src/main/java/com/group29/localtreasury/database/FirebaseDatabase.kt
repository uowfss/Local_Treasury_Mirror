package com.group29.localtreasury.database

import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage


class FirebaseDatabase {
    companion object{
        val CHAT = "Chat"
        val LISTING = "Listing"
        val LOGINFAILED = "FailedLogin"
        val SIGNUPFAILED = "FailedSignUp"
    }



    private val db = Firebase.firestore
    private val firebaseAuthentication = Firebase.auth


    /*
    How to use create account:
    do something like this

    firebaseDatabase.createAccount(email, password) { userId ->
        if (userId != null) {
            //Login
        } else {
            // Signup failed
        }
    }
    */
    fun createAccount(email: String, password: String, callback: (String?) -> Unit) {
        firebaseAuthentication.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid
                callback(userId)
            }
            .addOnFailureListener {
                callback(SIGNUPFAILED)
            }
    }

    // Sign In Function
    /*
    firebaseDatabase.signIn(email, password) { userId ->
        if (userId != LOGINFAILED) {
            //Login
        } else {
            // Wrong Credintels
        }
    }

     */
    fun signIn(email: String, password: String, callback: (String?) -> Unit) {
        firebaseAuthentication.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid
                callback(userId)
            }
            .addOnFailureListener {
                callback(LOGINFAILED)
            }
    }

    // Need to save a Username after signup in the callback
    fun saveUsername(userId: String, username: String, email: String) {
        val user = mapOf(
            "username" to username,
            "email" to email
        )
        db.collection("users").document(userId).set(user)
    }

    // Makes the post needs the image URI and an ItemPost Object no need to set a postid for the object
    fun createPost(item: ItemPostObject, imageUri: Uri?) {
        val postRef = db.collection("posts").document()
        val postId = postRef.id
        item.PostID = postId
        postRef.set(item)
            .addOnSuccessListener {
                imageUri?.let { uploadPostImage(postId, it) }
            }
    }

// uploads the image to the storage
    fun uploadPostImage(postId: String, imageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().getReference("images/posts/$postId.jpg")
        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val postRef = db.collection("posts").document(postId)
                    postRef.update("imageURL", uri.toString())
                }
            }

    }

    // Returns the list of ItemPostsObject
    // Simular to the signin needs a callback function
    fun getUserPosts(searchQuery: String? = null, callback: (List<ItemPostObject>) -> Unit) {
        val query = if (searchQuery.isNullOrEmpty()) {
            db.collection("posts") // Fetch all posts
        } else {
            db.collection("posts")
                .whereGreaterThanOrEqualTo("itemName", searchQuery)
                .whereLessThanOrEqualTo("itemName", searchQuery + "\uf8ff") // Case-insensitive search
        }

        query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                callback(emptyList())
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                val updatedPosts = snapshot.documents.mapNotNull { it.toObject(ItemPostObject::class.java) }
                callback(updatedPosts)
            } else {
                callback(emptyList())
            }
        }
    }


    // Adds the message to the chat
    fun sendMessage(chatMessage: ChatObject) {
        // Generate a consistent chat ID based on userID and receiverID
        val chatId = getChatId(chatMessage.senderID, chatMessage.recieverID)
        val chatRef = db.collection("chats").document(chatId)

        // Save or update the chat document
        chatRef.set(chatMessage)
            .addOnSuccessListener {
                Log.d("BG", "Chat created/updated successfully")
            }
            .addOnFailureListener { e ->
            }
    }

    // returns the Chat ID between the 2 users
    fun getChatId(userID: String, receiverID: String): String {
        return if (userID < receiverID) "$userID-$receiverID" else "$receiverID-$userID"
    }

    // Gets all the related users chats
    fun listenToUserChats(userID: String, callback: (List<ChatObject>) -> Unit) {
        val chats = mutableListOf<ChatObject>()

        // Listen for changes where the user is either the sender or the receiver
        db.collection("chats")
            .whereArrayContains("participants", userID)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val updatedChats = snapshot.documents.mapNotNull { it.toObject(ChatObject::class.java) }
                    callback(updatedChats)
                } else {
                    callback(emptyList())
                }
            }
    }

    // returns a ChatObject if it exits otherwise null
    // updates whenever firebase see it has changed
    fun getUserChat(senderID: String, recieverID:String, callback: (ChatObject?) -> Unit) {
        // Listen for changes where the user is either the sender or the receiver
        db.collection("chats")
            .document(getChatId(senderID,recieverID))
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    callback(null)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val chatObject = snapshot.toObject(ChatObject::class.java)
                    callback(chatObject)
                } else {

                    callback(null)
                }
            }
    }

    fun deleteChat(userID: String, receiverID: String, callback: (Boolean) -> Unit) {
        val chatId = getChatId(userID, receiverID)
        val chatRef = db.collection("chats").document(chatId)
        chatRef.delete()
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener { e ->
                callback(false)
            }
    }

    fun getUsername(ID: String, callback: (String?) -> Unit) {
        val users = db.collection("users").document(ID)
        users.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username")
                    callback(username)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                callback(null)
            }
    }

    fun getAccountDetails(ID:String,callback: (String?, String?,String?) -> Unit){
        val users = db.collection("users").document(ID)
        users.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username")
                    val addressMap = document.get("address") as? Map<String, String>
                    val address = addressMap?.let { "${it["line1"]}, ${it["city"]}" }
                    val firstName = document.getString("firstName")
                    val lastName = document.getString("lastName")
                    callback(username,address,"$firstName $lastName")
                } else {
                    callback(null,null,null)
                }
            }
            .addOnFailureListener { exception ->
                callback(null,null,null)
            }
    }


}