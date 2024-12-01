package com.group29.localtreasury

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.group29.localtreasury.ui.chats.DirectChat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.group29.localtreasury.database.ItemPostObject

class SellDetailActivity : AppCompatActivity() {

    private lateinit var itemImageView: ImageView
    private lateinit var sellerFirstNameTextView: TextView
    private lateinit var sellerLastNameTextView: TextView
    private lateinit var sellerAddressTextView: TextView
    private lateinit var itemNameTextView: TextView
    private lateinit var itemDescriptionTextView: TextView
    private lateinit var itemPriceTextView: TextView
    private lateinit var chatButton : Button
    private var sellerID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sell_detail)

        // References to UI elements
        itemImageView = findViewById(R.id.item_imageView)
        sellerFirstNameTextView = findViewById(R.id.seller_first_name_text)
        sellerLastNameTextView = findViewById(R.id.seller_last_name_text)
        sellerAddressTextView = findViewById(R.id.seller_phone_text)
        itemNameTextView = findViewById(R.id.textView9)
        itemDescriptionTextView = findViewById(R.id.textView11)
        itemPriceTextView = findViewById(R.id.textView13)
        val cancelSellingButton = findViewById<Button>(R.id.cancel_selling_btn)
        chatButton = findViewById(R.id.chat_page_btn)

        // Get the ItemPostObject passed from HomeFragment
        val itemPost = intent.getSerializableExtra("itemPost") as? ItemPostObject

        // Display item details
        itemPost?.let {
            itemNameTextView.text = it.itemName
            itemDescriptionTextView.text = it.itemDescription
            itemPriceTextView.text = it.itemPrice
            sellerID = it.sellerID
            // Load image using Glide
            Log.d("BGGlide", it.ImageURL)
            Glide.with(this)
                .load(it.ImageURL) // Placeholder image for now
                .placeholder(R.drawable.placeholder_image)
                .into(itemImageView)

            // Fetch seller details from Firestore
            fetchSellerDetails(it.sellerID)
        }

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

        chatButton.setOnClickListener {
            if(sellerID != ""){
                val intent = Intent(this,DirectChat::class.java)
                intent.putExtra("RECIEVERID", sellerID)
                startActivity(intent)
            }
        }

        // Cancel current selling page
        cancelSellingButton.setOnClickListener{
            finish()
        }
    }

    private fun fetchSellerDetails(sellerID: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(sellerID).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    sellerFirstNameTextView.text = document.getString("firstName")
                    sellerLastNameTextView.text = document.getString("lastName")
                    // Fetch and format address
                    val addressMap = document.get("address") as? Map<*, *>
                    if (addressMap != null) {
                        val line1 = addressMap["line1"] as? String ?: "Unknown"
                        val city = addressMap["city"] as? String ?: "Unknown"
                        sellerAddressTextView.text = "$line1, $city"
                    } else {
                        sellerAddressTextView.text = "Address not available"
                    }
                }
            }
    }
}