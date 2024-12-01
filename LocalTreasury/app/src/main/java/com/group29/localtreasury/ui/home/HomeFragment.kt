package com.group29.localtreasury.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.group29.localtreasury.database.FirebaseDatabase
import com.group29.localtreasury.database.ItemPostObject
import com.group29.localtreasury.databinding.FragmentHomeBinding
import com.group29.localtreasury.ui.home.PostAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val database = FirebaseDatabase()
    private val postList = mutableListOf<ItemPostObject>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Set up RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = PostAdapter(postList)
        binding.recyclerView.adapter = adapter

        // Fetch posts from Firebase
        //val userId = "USER_ID" // Replace with actual user ID logic
        database.getUserPosts() { posts ->
            postList.clear()
            postList.addAll(posts)
            adapter.notifyDataSetChanged()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
