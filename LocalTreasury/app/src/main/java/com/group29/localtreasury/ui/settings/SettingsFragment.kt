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

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var userName: TextView
    private lateinit var address: TextView
    private lateinit var fullName: TextView

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


        settingsViewModel = ViewModelProvider(requireActivity()).get(SettingsViewModel::class.java)
        settingsViewModel.profileImage.observe(requireActivity()){

        }


        userName = binding.UsernamePlaceholder
        address = binding.AddressPlaceholder
        fullName = binding.FullNamePlaceholder
        val firebase = FirebaseDatabase()

        firebase.getAccountDetails(FirebaseAuth.getInstance().getCurrentUser()!!.getUid()){usernametemp,addresstemp,nametemp ->
            address.text = addresstemp
            userName.text = usernametemp
            fullName.text = nametemp
        }




        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}