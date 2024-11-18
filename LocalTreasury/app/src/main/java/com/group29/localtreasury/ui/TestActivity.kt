package com.group29.localtreasury.ui

import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.group29.localtreasury.R
import com.group29.localtreasury.database.ChatObject
import com.group29.localtreasury.database.FirebaseDatabase
import com.group29.localtreasury.databinding.ActivityTestBinding

class TestActivity : AppCompatActivity() {

    var UserID = "nvTXLr0gppdYaWHS7TmX5BoNnf83"
    val RID = "1234567"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val firebaseDatabase = FirebaseDatabase()

        firebaseDatabase.createAccount("Test1@gmail.com","123456789") { userID ->
            if(userID != FirebaseDatabase.SIGNUPFAILED && userID != null){
                val text = "Create Account userID: $userID"
                UserID = userID
                firebaseDatabase.saveUsername(UserID,"Testname","Test1@gmail.com")
                Log.d("BG",text)
            }

        }

        firebaseDatabase.signIn("Test1@gmail.com","123456789") { userID ->
            if(userID != FirebaseDatabase.LOGINFAILED && userID != null){
                val text = "Sign In userID: $userID"
                UserID = userID
                Log.d("BG",text)
            }

        }

        var chat = ChatObject()
        chat.senderID = UserID
        chat.recieverID = RID
        chat.participants.add(UserID)
        chat.participants.add(RID)
        chat.messages.add("Hi")
        firebaseDatabase.sendMessage(chat)

        firebaseDatabase.listenToUserChats(UserID){ list ->
            Log.d("BG1",list.size.toString())
        }


        chat.messages.add("Hello")
        chat.messages.add("Bye")
        firebaseDatabase.sendMessage(chat)

        firebaseDatabase.listenToUserChats(UserID){ list ->
            Log.d("BG2",list.size.toString())
        }

        firebaseDatabase.getUserChat(RID,UserID){ chatitem ->
            val messages = chatitem?.messages
            messages!!.forEachIndexed { index, message ->
                Log.d("BG-Message", "Message $index: $message")
            }
            Log.d("BG",messages.size.toString())

        }

        chat.messages.add("Hello")
        chat.messages.add("Bye")
        firebaseDatabase.sendMessage(chat)


    }

}