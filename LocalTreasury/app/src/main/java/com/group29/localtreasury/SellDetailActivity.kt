package com.group29.localtreasury

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.group29.localtreasury.ui.chats.DirectChat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
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
    private lateinit var openMapButton : Button
    private lateinit var deleteItemButton: Button
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
        openMapButton = findViewById(R.id.show_map_btn)
        deleteItemButton = findViewById(R.id.delete_item_btn)

        // Get the ItemPostObject passed from HomeFragment
        val itemPost = intent.getSerializableExtra("itemPost") as? ItemPostObject

        // Display item details
        itemPost?.let {
            itemNameTextView.text = it.itemName
            itemDescriptionTextView.text = it.itemDescription
            itemPriceTextView.text = it.itemPrice
            sellerID = it.sellerID
            // Load image using Glide
            Glide.with(this)
                .load(it.ImageURL) // Placeholder image for now
                .placeholder(R.drawable.placeholder_image)
                .into(itemImageView)

            // Fetch seller details from Firestore
            fetchSellerDetails(it.sellerID)
        }

        // Open Map button
        openMapButton.setOnClickListener {
            if (sellerAddressTextView.text.isNotEmpty()) {
                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra("ITEM_ADDRESS", sellerAddressTextView.text.toString())
                startActivity(intent)
            } else {
                Toast.makeText(this, "Seller address not available", Toast.LENGTH_SHORT).show()
            }
        }

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

        // Delete item button
        deleteItemButton.setOnClickListener {
            itemPost?.let { post ->
                showDeleteConfirmationDialog(post.PostID, post.sellerID)
            }
        }
    }

    private fun fetchSellerDetails(sellerID: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(sellerID).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    // Set seller details
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

                    // Fetch and display the profile image
                    val profileImageUrl = document.getString("profileImageUrl")
                    if (profileImageUrl != null && profileImageUrl.isNotEmpty()) {
                        Glide.with(this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.profiledefault) // Default image if loading fails
                            .centerCrop()
                            .into(findViewById(R.id.profile_imageView))
                    } else {
                        // If no profile image is available, set the default image
                        findViewById<ImageView>(R.id.profile_imageView).setImageResource(R.drawable.profiledefault)
                    }
                }
            }
            .addOnFailureListener {
                // Handle failure
                Toast.makeText(this, "Failed to fetch seller details", Toast.LENGTH_SHORT).show()
                findViewById<ImageView>(R.id.profile_imageView).setImageResource(R.drawable.profiledefault)
            }
    }

    private fun showDeleteConfirmationDialog(postID: String, sellerID: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_post, null)
        val emailEditText = dialogView.findViewById<EditText>(R.id.delete_email_text)
        val passwordEditText = dialogView.findViewById<EditText>(R.id.delete_password_text)

        AlertDialog.Builder(this)
            .setTitle("Confirm Deletion")
            .setView(dialogView)
            .setPositiveButton("Confirm") { dialog, _ ->
                val email = emailEditText.text.toString().trim()
                val password = passwordEditText.text.toString().trim()

                if (email.isNotEmpty() && password.isNotEmpty()) {
                    verifyCredentialsWithFirebase(email, password, sellerID) { isVerified ->
                        if (isVerified) {
                            deleteItem(postID) // Proceed with deletion
                        } else {
                            Toast.makeText(this, "Invalid email, password, or permission denied", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun verifyCredentialsWithFirebase(
        email: String,
        password: String,
        sellerID: String,
        callback: (Boolean) -> Unit
    ) {
        val auth = FirebaseAuth.getInstance()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.uid == sellerID) {
                        callback(true) // Verified successfully
                    } else {
                        callback(false) // Seller ID mismatch
                    }
                } else {
                    callback(false) // Authentication failed
                }
            }
    }

    private fun deleteItem(itemId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("posts").document(itemId).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT).show()
                finish() // Navigate back to the previous screen
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show()
            }
    }

}