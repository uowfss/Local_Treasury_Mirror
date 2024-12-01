package com.group29.localtreasury.ui.chats

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.group29.localtreasury.R

class ChatArrayAdapter(
    private val context: Context,
    private var messageList: List<String>,
    private val userID: String
) : BaseAdapter() {

    override fun getCount(): Int {
        return messageList.size
    }

    override fun getItem(position: Int): Any {
        return messageList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val view = convertView ?: inflater.inflate(R.layout.single_message, parent, false)
        val message: TextView = view.findViewById(R.id.message)
        var parsedMessage = messageList[position].split("(:::)")
        val combinedMessage = parsedMessage.drop(1).joinToString("")
        if(userID == parsedMessage[0]){
            message.setBackgroundColor(ContextCompat.getColor(context, R.color.UserChat))

        }
        else{
            message.setBackgroundColor(ContextCompat.getColor(context, R.color.OtherChat))
        }
        message.text = combinedMessage


        return view
    }
    //Updates the list
    fun updateChat(updatedList: List<String>){
        messageList = updatedList
        notifyDataSetChanged()
    }
}