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
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.group29.localtreasury.database.FirebaseDatabase
import com.group29.localtreasury.database.ItemPostObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class SellPostActivity : AppCompatActivity() {

    private lateinit var itemImageView: ImageView
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private var selectedImageUri: Uri? = null

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

        val item_name = findViewById<EditText>(R.id.item_name_textBox)
        val item_description = findViewById<EditText>(R.id.item_description_textBox)
        val item_price = findViewById<EditText>(R.id.item_price_textBox)
        val item_pickup_addr = findViewById<EditText>(R.id.pickup_addr_textBox)

        // Initialize the camera launcher
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Get the captured image as a Bitmap
                val imageBitmap = result.data?.extras?.get("data") as Bitmap

                // Save the bitmap to a temporary file and get its URI
                val tempFile = saveBitmapToFile(imageBitmap)
                selectedImageUri = Uri.fromFile(tempFile)

                // Resize the saved image to fit the ImageView dimensions
                val resizedBitmap = resizeImageToImageView(selectedImageUri!!)
                if (resizedBitmap != null) {
                    itemImageView.setImageBitmap(resizedBitmap) // Display the resized image
                } else {
                    Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
            }
        }


        // Initialize the gallery launcher
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                if (selectedImageUri != null) {
                    // Resize the selected image to match the ImageView dimensions
                    val resizedBitmap = resizeImageToImageView(selectedImageUri!!)
                    itemImageView.setImageBitmap(resizedBitmap) // Set the resized image
                } else {
                    Toast.makeText(this, "Failed to select image", Toast.LENGTH_SHORT).show()
                }
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
            val item_Name = item_name.text.toString()
            val item_Descrip = item_description.text.toString()
            val item_Price = item_price.text.toString()
            val addr = item_pickup_addr.text.toString()

            if (item_Name.isNotEmpty() && item_Descrip.isNotEmpty() && item_Price.isNotEmpty() && addr.isNotEmpty()) {
                val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
                val localFile = copyUriToLocalFile(this, selectedImageUri!!)
                if (currentUserID != null) {
                    val itemPost = ItemPostObject().apply {
                        sellerID = currentUserID
                        itemName = item_Name
                        itemPrice = item_Price
                        itemDescription = item_Descrip
                        address = addr
                        // latLng is left empty for now
                    }
                    FirebaseDatabase().createPost(itemPost, Uri.fromFile(localFile)) // Upload the post
                    //localFile?.delete()
                    Toast.makeText(this, "Item posted successfully", Toast.LENGTH_SHORT).show()
                    finish() // Close the activity
                } else {
                    Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            }
        }

        cancelPostButton.setOnClickListener {
            finish() // Go back to the previous activity
        }
    }

    // Helper function to resize the image to fit within the ImageView dimensions
    private fun resizeImageToImageView(imageUri: Uri): Bitmap? {
        val inputStream = contentResolver.openInputStream(imageUri) ?: return null
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        // Get the target dimensions from the ImageView
        val targetWidth = resources.getDimensionPixelSize(R.dimen.image_view_width) // Convert 150dp to pixels
        val targetHeight = resources.getDimensionPixelSize(R.dimen.image_view_height) // Convert 150dp to pixels

        // Scale the bitmap to fit the ImageView dimensions
        return Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
    }

    // Helper function to copy URI to app cache
    private fun copyUriToLocalFile(context: Context, uri: Uri): File? {
        try {
            val contentResolver: ContentResolver = context.contentResolver
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("temp_image", ".jpg", context.cacheDir)
            val outputStream = FileOutputStream(tempFile)

            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            return tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap): File {
        val tempFile = File.createTempFile("camera_image", ".jpg", cacheDir)
        val outputStream = FileOutputStream(tempFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return tempFile
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