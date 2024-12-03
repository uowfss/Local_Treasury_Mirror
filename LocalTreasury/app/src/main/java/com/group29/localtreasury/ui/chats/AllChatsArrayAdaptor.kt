package com.group29.localtreasury.ui.chats

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.group29.localtreasury.R
import com.group29.localtreasury.database.ChatObject
import com.group29.localtreasury.database.FirebaseDatabase

class AllChatsArrayAdaptor (
    private val context: Context,
    private var chatList: List<ChatObject>,
    private val userID: String

    ) : BaseAdapter()
{
    override fun getCount(): Int {
        return chatList.size
    }
    override fun getItem(position: Int): Any {
        return chatList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val view = convertView ?: inflater.inflate(R.layout.chat_active, parent, false)
        val usernameTextView: TextView = view.findViewById(R.id.username)
        val lastmessage: TextView = view.findViewById(R.id.last_message)
        val size = chatList[position].messages.size
        var lastText = ""

        val firebase = FirebaseDatabase()

        var chatInstance = chatList[position]
        if(chatInstance.senderID != userID){
            firebase.getUsername(chatInstance.senderID){ userName ->
                usernameTextView.text = userName
            }
        }
        else{
            firebase.getUsername(chatInstance.recieverID){ userName ->
                usernameTextView.text = userName
            }
        }

        if(size > 0){
            chatInstance.messages.let { messageList ->
                for (item in messageList) {
                    Log.d("BG", item)
                }
            }




            var parsedMessage = chatInstance.messages[size-1].split("(:::)")

            if(parsedMessage[0] == userID){
                lastText = "Sent: " + parsedMessage[1]
            }
            else{
                lastText = "Recieved: " + parsedMessage[1]
            }
        }
        lastmessage.text = lastText
        return view
    }
    //Updates the list
    fun updateChat(updatedList: List<ChatObject>){
        chatList = updatedList
        notifyDataSetChanged()
    }
}