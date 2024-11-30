package com.group29.localtreasury.ui.settings

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.logging.type.LogSeverityProto
import com.group29.localtreasury.R
import com.group29.localtreasury.Util
import com.group29.localtreasury.database.ChatObject
import com.group29.localtreasury.database.FirebaseDatabase
import com.group29.localtreasury.databinding.FragmentSettingsBinding
import com.group29.localtreasury.ui.chats.DirectChat

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    private lateinit var galleryResult: ActivityResultLauncher<Intent>
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var imageView: ImageView
    private lateinit var userName: TextView

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        imageView = binding.profileImage
        imageView.setImageResource(R.drawable.profiledefault)

        galleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedImageUri = result.data?.data
                if (selectedImageUri != null) {
                    val bitmap = Util.getBitmap(requireActivity(), selectedImageUri,false)
                    settingsViewModel.profileImage.value = bitmap
                }
            }
        }

        settingsViewModel = ViewModelProvider(requireActivity()).get(SettingsViewModel::class.java)
        settingsViewModel.profileImage.observe(requireActivity()){
                it: Bitmap ->
            imageView.setImageBitmap(it)
            //save the image to the database
        }

        imageView.setOnClickListener(){
            val intent = Intent(activity,DirectChat::class.java)
            intent.putExtra("RECIEVERID", "o95kzdINL6buVAut0Lcy0KcHRkN2")
            startActivity(intent)
            //galleryApp()
        }

        userName = binding.UsernamePlaceholder
        val firebase = FirebaseDatabase()

        firebase.getUsername(FirebaseAuth.getInstance().getCurrentUser()!!.getUid()){ username ->
            if(username != null){
                userName.text = username
            }

        }




        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun galleryApp(){
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryResult.launch(galleryIntent)
    }
}