package com.group29.localtreasury.ui.chats

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.group29.localtreasury.database.ChatObject
import com.group29.localtreasury.database.FirebaseDatabase
import com.group29.localtreasury.databinding.FragmentChatsBinding

class ChatsFragment : Fragment() {
    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!

    var userID = ""

    private lateinit var chatsViewModel: ChatsViewModel
    private lateinit var chatActivityAdapter : AllChatsArrayAdaptor


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        chatsViewModel = ChatsViewModel.getInstance(requireActivity().application)

        userID = FirebaseAuth.getInstance().getCurrentUser()!!.getUid()
        val firebase = FirebaseDatabase()

        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val listView: ListView = binding.ChatsList

        val emptyList = listOf<ChatObject>()
        chatActivityAdapter = AllChatsArrayAdaptor(requireActivity(),emptyList,userID)
        listView.adapter = chatActivityAdapter
        listView.setOnItemClickListener(){ parent, view, position, id ->
            val allChats = chatsViewModel.allChats.value
            var recieverID = ""
            if(allChats?.get(position)!!.senderID != userID){
                recieverID = allChats[position].senderID
            }
            else{
                recieverID = allChats[position].recieverID
            }
            val intent = Intent(activity,DirectChat::class.java)
            intent.putExtra("RECIEVERID", recieverID)
            startActivity(intent)

        }

        chatsViewModel.allChats.observe(requireActivity()){
            chatActivityAdapter.updateChat(it!!)
        }

        firebase.listenToUserChats(userID){chats ->
            chatsViewModel.updateChatList(chats)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}