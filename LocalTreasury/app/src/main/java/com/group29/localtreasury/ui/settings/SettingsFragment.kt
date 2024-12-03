package com.group29.localtreasury.ui.settings

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.group29.localtreasury.R
import com.group29.localtreasury.databinding.FragmentSettingsBinding

@UnstableApi
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private lateinit var profileImageView: ImageView
    private lateinit var uploadButton: Button
    private lateinit var userNameTextView: TextView
    private lateinit var fullNameTextView: TextView
    private lateinit var addressTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var settingsViewModel: SettingsViewModel
    private var selectedImageUri: Uri? = null

    private val binding get() = _binding!!

    // Launcher to pick an image
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            if (selectedImageUri != null) {
                Log.d("SettingsFragment", "Image URI selected: $selectedImageUri")
                Glide.with(this).load(selectedImageUri).centerCrop().into(profileImageView)
            } else {
                Log.e("SettingsFragment", "Image URI is null")
                Toast.makeText(requireContext(), "Failed to select image", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d("SettingsFragment", "Image selection canceled")
            Toast.makeText(requireContext(), "Image selection canceled", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        profileImageView = binding.profileImage
        uploadButton = binding.uploadProfileButton
        userNameTextView = binding.UsernamePlaceholder
        fullNameTextView = binding.FullNamePlaceholder
        addressTextView = binding.AddressPlaceholder
        emailTextView = binding.EmailPlaceholder

        settingsViewModel = ViewModelProvider(requireActivity()).get(SettingsViewModel::class.java)

        // Load user details and profile image
        loadUserProfile()

        uploadButton.setOnClickListener {
            if (selectedImageUri != null) {
                uploadImageToFirebase()
            } else {
                openImagePicker()
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openImagePicker() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 101)
        } else {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }
    }

    @OptIn(UnstableApi::class)
    private fun uploadImageToFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User is not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri == null) {
            Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
            return
        }

        val storageRef = FirebaseStorage.getInstance().getReference("profile_pictures/$userId.jpg")
        Log.d("SettingsFragment", "Uploading image to path: profile_pictures/$userId.jpg")

        // Start the upload process
        storageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    Log.d("SettingsFragment", "Upload successful. Download URL: $downloadUri")
                    saveProfileImageUriToFirestore(downloadUri.toString())
                    Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e ->
                    Log.e("SettingsFragment", "Failed to get download URL", e)
                    Toast.makeText(requireContext(), "Failed to get download URL: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("SettingsFragment", "Image upload failed", e)
                Toast.makeText(requireContext(), "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun saveProfileImageUriToFirestore(imageUrl: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val additionalData = mapOf("profileImageUrl" to imageUrl)
        db.collection("users").document(userId)
            .update(additionalData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile image updated successfully", Toast.LENGTH_SHORT).show()
                loadUserProfile() // Reload profile to ensure the image is persistent
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update profile image", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    // Load user details
                    userNameTextView.text = document.getString("username") ?: "Unknown"

                    val first_name = document.getString("firstName") ?: "Unknown"
                    val last_name = document.getString("lastName") ?: "Unknown"

                    fullNameTextView.text = "$first_name $last_name"

                    val addressMap = document.get("address") as? Map<*, *>
                    val line1 = addressMap?.get("line1") as? String ?: "Unknown"
                    val city = addressMap?.get("city") as? String ?: "Unknown"
                    addressTextView.text = "$line1, $city"

                    emailTextView.text = document.getString("email") ?: "Unknown"

                    // Load profile image
                    val profileImageUrl = document.getString("profileImageUrl")
                    if (profileImageUrl != null) {
                        Glide.with(this).load(profileImageUrl).centerCrop().into(profileImageView)
                    } else {
                        profileImageView.setImageResource(R.drawable.profiledefault) // Default image
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load user profile", Toast.LENGTH_SHORT).show()
            }
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openImagePicker()
        } else {
            Toast.makeText(requireContext(), "Permission denied to access gallery", Toast.LENGTH_SHORT).show()
        }
    }

}
