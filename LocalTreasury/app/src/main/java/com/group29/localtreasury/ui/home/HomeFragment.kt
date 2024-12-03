package com.group29.localtreasury.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.group29.localtreasury.database.FirebaseDatabase
import com.group29.localtreasury.database.ItemPostObject
import com.group29.localtreasury.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val database = FirebaseDatabase()
    private val postList = mutableListOf<ItemPostObject>()
    private lateinit var adapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Set up RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = PostAdapter(postList)
        binding.recyclerView.adapter = adapter

        // Fetch all posts initially
        fetchPosts(null)

        // Handle search functionality
        binding.searchLogo.setOnClickListener {
            val searchText = binding.textView14.text.toString().trim()
            if (searchText.isNotEmpty()) {
                fetchPosts(searchText.lowercase()) // Perform case-insensitive search
            } else {
                Toast.makeText(context, "Enter a search term", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle back button press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            binding.textView14.setText("Search...")
            fetchPosts(null) // Reload all posts
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Function to fetch posts from Firebase
    private fun fetchPosts(searchQuery: String?) {
        database.getUserPosts(searchQuery) { posts ->
            postList.clear()
            postList.addAll(posts)
            adapter.notifyDataSetChanged()
        }
    }
}
