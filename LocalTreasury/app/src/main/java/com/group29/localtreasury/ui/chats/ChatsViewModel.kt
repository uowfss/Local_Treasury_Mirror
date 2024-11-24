package com.group29.localtreasury.ui.chats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.group29.localtreasury.database.ChatObject

class ChatsViewModel(application: Application) {
    var allChats = MutableLiveData<List<ChatObject>?>()
    var singleChat: MutableLiveData<ChatObject?> = MutableLiveData(ChatObject())

    fun updateChatList(chats: List<ChatObject>){
        allChats.value = chats
    }

    fun updateChat(chat: ChatObject){
        singleChat.value = chat
    }

    fun getChat(pos: Int): ChatObject{
        return allChats.value!!.get(pos)
    }
    companion object {
        @Volatile
        private var INSTANCE: ChatsViewModel? = null
        fun getInstance(application: Application): ChatsViewModel {
            return INSTANCE ?: synchronized(this) {
                var instance = ChatsViewModel.INSTANCE
                if (instance == null){
                    instance = ChatsViewModel(application)
                    ChatsViewModel.INSTANCE = instance
                }
                return instance
            }
        }
    }
}