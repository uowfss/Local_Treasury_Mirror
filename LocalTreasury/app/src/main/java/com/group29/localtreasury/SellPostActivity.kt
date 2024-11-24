package com.group29.localtreasury

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.Manifest
import android.graphics.Bitmap
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

class SellPostActivity : AppCompatActivity() {

    private lateinit var itemImageView: ImageView
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sell_post)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sell_post_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        itemImageView = findViewById(R.id.post_item_imageView)
        val selectImageButton = findViewById<Button>(R.id.select_pic_btn)
        val postItemButton = findViewById<Button>(R.id.submit_post_btn)
        val cancelPostButton = findViewById<Button>(R.id.cancel_post_btn)

        val sellerNameEditText = findViewById<EditText>(R.id.post_owner_textBox)
        val sellerAddressEditText = findViewById<EditText>(R.id.post_address_textBox)
        val sellerPhoneEditText = findViewById<EditText>(R.id.owner_phone_textBox)

        // Initialize launchers
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as Bitmap
                itemImageView.setImageBitmap(imageBitmap)
            }
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri: Uri? = result.data?.data
                itemImageView.setImageURI(imageUri)
            }
        }

        selectImageButton.setOnClickListener {
            val options = arrayOf("Take Photo", "Choose from Gallery")
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Select Image")
            builder.setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> openGallery()
                }
            }
            builder.show()
        }

        postItemButton.setOnClickListener {
            val name = sellerNameEditText.text.toString()
            val address = sellerAddressEditText.text.toString()
            val phone = sellerPhoneEditText.text.toString()

            if (name.isNotEmpty() && address.isNotEmpty() && phone.isNotEmpty()) {
                // TODO: Post item logic (save to database or backend)
                Toast.makeText(this, "Item posted successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            }
        }

        cancelPostButton.setOnClickListener {
            finish() // Go back to the previous activity
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 100)
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission is required to take a photo", Toast.LENGTH_SHORT).show()
        }
    }
}