package com.group29.localtreasury

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.firestore.FirebaseFirestore

class UserDetailDialog : DialogFragment() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String
    private var fromSignUp: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()

        // Retrieve the userId passed to this fragment
        arguments?.let {
            userId = it.getString("USER_ID", "")
            fromSignUp = it.getBoolean("FROM_SIGNUP", false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_detail_dialog, container, false)

        val firstNameEditText = view.findViewById<EditText>(R.id.first_name_input)
        val lastNameEditText = view.findViewById<EditText>(R.id.last_name_input)
        val addressLine1EditText = view.findViewById<EditText>(R.id.address_input)
        val cityEditText = view.findViewById<EditText>(R.id.city_input)
        val submitButton = view.findViewById<Button>(R.id.submit_details_btn)

        submitButton.setOnClickListener {
            val firstName = firstNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()
            val addressLine1 = addressLine1EditText.text.toString().trim()
            val city = cityEditText.text.toString().trim()

            // Validate inputs
            when {
                firstName.isEmpty() -> {
                    firstNameEditText.error = "First Name is required"
                    firstNameEditText.requestFocus()
                }
                lastName.isEmpty() -> {
                    lastNameEditText.error = "Last Name is required"
                    lastNameEditText.requestFocus()
                }
                addressLine1.isEmpty() -> {
                    addressLine1EditText.error = "Address Line 1 is required"
                    addressLine1EditText.requestFocus()
                }
                city.isEmpty() -> {
                    cityEditText.error = "City is required"
                    cityEditText.requestFocus()
                }
                else -> {
                    // Inputs are valid, save to Firestore
                    val additionalData = hashMapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "address" to mapOf(
                            "line1" to addressLine1,
                            "city" to city
                        )
                    )

                    firestore.collection("users").document(userId)
                        .update(additionalData)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Details saved successfully!", Toast.LENGTH_SHORT).show()
                                if(!fromSignUp){
                                    // Navigate to the main activity
                                    val intent = Intent(requireContext(), MainActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                }
                                dismiss()
                            } else {
                                Toast.makeText(context, "Failed to save details.", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }
        return view
    }
}