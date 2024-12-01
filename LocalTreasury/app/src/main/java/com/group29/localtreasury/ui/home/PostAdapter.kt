package com.group29.localtreasury.ui.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.group29.localtreasury.R
import com.group29.localtreasury.SellDetailActivity
import com.group29.localtreasury.database.ItemPostObject

class PostAdapter(private val posts: List<ItemPostObject>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemImage: ImageView = view.findViewById(R.id.itemImageView)
        val itemName: TextView = view.findViewById(R.id.itemNameText)
        val itemDescription: TextView = view.findViewById(R.id.itemDescriptionText)
        val itemPrice: TextView = view.findViewById(R.id.itemPriceText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        // Set the data
        holder.itemName.text = "Item: ${post.itemName}"
        holder.itemDescription.text = "Description: ${post.itemDescription}"
        holder.itemPrice.text = "Listed price: $${post.itemPrice}"


        Glide.with(holder.itemView.context)
            .load(post.ImageURL)
            .centerCrop()
            .placeholder(R.drawable.placeholder_image)
            .into(holder.itemImage)

        // Click listener to open SellDetailActivity
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, SellDetailActivity::class.java)
            intent.putExtra("itemPost", post) // Pass the ItemPostObject
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = posts.size
}
