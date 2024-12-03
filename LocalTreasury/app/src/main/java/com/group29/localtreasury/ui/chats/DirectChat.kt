package com.group29.localtreasury.ui.chats

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.group29.localtreasury.R
import com.group29.localtreasury.database.ChatObject
import com.group29.localtreasury.database.FirebaseDatabase

class DirectChat : AppCompatActivity() {

    private lateinit var chatsViewModel: ChatsViewModel
    var userID = ""
    var recieverID= ""
    private lateinit var sendLine : EditText
    private lateinit var sendButton: Button
    private lateinit var usernameText : TextView
    private lateinit var deletebutton: Button

    private lateinit var chatActivityAdapter : ChatArrayAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_direct_chat)

        var chatListView = findViewById<ListView>(R.id.chat_messages)

        sendLine = findViewById(R.id.send_line)
        sendButton = findViewById(R.id.send_button)
        usernameText = findViewById(R.id.Reciever_Username)
        deletebutton = findViewById(R.id.delete_chat_button)

        chatsViewModel = ChatsViewModel.getInstance(application)

        userID = FirebaseAuth.getInstance().getCurrentUser()!!.getUid()

        if(savedInstanceState != null){
            recieverID = savedInstanceState.getString("RECIEVERID")!!
        }
        else{
            recieverID = intent.getStringExtra("RECIEVERID")!!
        }

        val firebase = FirebaseDatabase()

        firebase.getUsername(recieverID){ username ->
            if(username != null){
                usernameText.text = username
            }

        }
        deletebutton.setOnClickListener(){
            firebase.deleteChat(userID,recieverID){ isDeleted ->
                if(isDeleted){
                    var tempChat = chatsViewModel.singleChat?.value
                    tempChat!!.messages = mutableListOf()
                    chatsViewModel.singleChat.value = tempChat
                    finish()
                }
            }


        }


        sendButton.setOnClickListener(){
            var messageText = sendLine.text.toString().trim()
            messageText = userID + "(:::)" + messageText
            if(messageText != userID + "(:::)"){
                var tempChat = chatsViewModel.singleChat?.value


                if(tempChat!!.senderID != ""){
                    tempChat.messages.add(messageText)
                    firebase.sendMessage(tempChat)
                    sendLine.setText("")
                }else{
                    var chat = ChatObject()
                    chat.senderID = userID
                    chat.recieverID = recieverID
                    chat.participants.add(userID)
                    chat.participants.add(recieverID)
                    chat.messages.add(messageText)
                    firebase.sendMessage(chat)
                    sendLine.setText("")

                }

            }
        }

        chatsViewModel.singleChat.observe(this){
            chatActivityAdapter.updateChat(it!!.messages)
        }

        firebase.getUserChat(userID,recieverID){chat ->
            if(chat != null){
                chatsViewModel.updateChat(chat)
            }else{
                var tempChat = chatsViewModel.singleChat?.value
                tempChat!!.messages = mutableListOf()
                chatsViewModel.singleChat.value = tempChat
            }
        }

        val emptyList = listOf<String>()
        chatActivityAdapter = ChatArrayAdapter(this,emptyList,userID)
        chatListView.adapter = chatActivityAdapter
        chatListView.post{
            chatListView.setSelection(chatActivityAdapter.count)
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("RECIEVERID",recieverID)
        super.onSaveInstanceState(outState)
    }
}
