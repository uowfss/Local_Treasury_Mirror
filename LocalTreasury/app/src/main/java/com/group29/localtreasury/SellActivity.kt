package com.group29.localtreasury

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SellActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sell)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.selling_info_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // References to UI elements
        val itemImageView = findViewById<ImageView>(R.id.item_imageView)
        val sellerNameTextView = findViewById<TextView>(R.id.seller_name_text)
        val sellerContactTextView = findViewById<TextView>(R.id.seller_phone_text)
        val openMapButton = findViewById<Button>(R.id.show_map_btn)
        val openChatButton = findViewById<Button>(R.id.chat_page_btn)
        val cancelSellingButton = findViewById<Button>(R.id.cancel_selling_btn)

        // Mock data (replace with actual data passed to this activity)
        // TODO: Replace with your image
        //itemImageView.setImageResource(R.drawable.sample_item)
        // TODO: Replace with seller info
        sellerNameTextView.text = "John Doe"
        sellerContactTextView.text = "123456789"

        // Open Map button
        //TODO: modify map using passed address later
        /**
        openMapButton.setOnClickListener {
            val geoUri = Uri.parse("geo:0,0?q=Your+Selling+Location")
            val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }**/

        // Open Chat button
        //TODO: link to chat page later
        /**
        openChatButton.setOnClickListener {
            val chatIntent = Intent(this, ChatActivity::class.java)
            startActivity(chatIntent)
        }**/

        // Cancel current selling page
        cancelSellingButton.setOnClickListener{
            finish()
        }
    }
}