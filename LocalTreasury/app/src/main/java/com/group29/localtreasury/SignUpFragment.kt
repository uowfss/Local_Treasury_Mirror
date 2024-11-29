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
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.group29.localtreasury.database.FirebaseDatabase

class SignUpFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)
        auth = FirebaseAuth.getInstance()

        val emailEditText = view.findViewById<EditText>(R.id.email_signup_input)
        val passwordEditText = view.findViewById<EditText>(R.id.password_signup_input)
        val usernameEditText = view.findViewById<EditText>(R.id.username_signup_input)
        val submitSignUpButton = view.findViewById<Button>(R.id.signup_submit_btn)
        val cancelSignUpButton = view.findViewById<Button>(R.id.signup_cancel_btn)

        submitSignUpButton.setOnClickListener{
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val username = usernameEditText.text.toString()

            if (username.isEmpty()) {
                usernameEditText.error = "Username cannot be empty"
                usernameEditText.requestFocus()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                emailEditText.error = "Email cannot be empty"
                emailEditText.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordEditText.error = "Password cannot be empty"
                passwordEditText.requestFocus()
                return@setOnClickListener
            }

            signUpWithEmail(email, password, username)
        }

        cancelSignUpButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
            requireActivity().findViewById<View>(R.id.login_layout).visibility = View.VISIBLE
        }
        return view
    }

    private fun signUpWithEmail(email: String, password: String, username: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid
                    if (userId != null) {
                        // Initialize Firestore instance
                        val firestore = FirebaseFirestore.getInstance()

                        // Create a map of user data
                        val userMap = hashMapOf(
                            "email" to email,
                            "username" to username
                        )

                        // Save the user data in the Firestore "users" collection
                        firestore.collection("users").document(userId)
                            .set(userMap)
                            .addOnCompleteListener { dbTask: Task<Void> ->
                                if (dbTask.isSuccessful) {
                                    // Open the UserDetailsDialogFragment
                                    val dialog = UserDetailDialog()
                                    val bundle = Bundle()
                                    bundle.putString("USER_ID", userId)
                                    bundle.putBoolean("FROM_SIGNUP", true)
                                    dialog.arguments = bundle
                                    dialog.show(requireActivity().supportFragmentManager, "UserDetailsDialogFragment")

                                    // Send email verification
                                    user.sendEmailVerification()
                                        ?.addOnCompleteListener { emailTask: Task<Void> ->
                                            if (emailTask.isSuccessful) {
                                                Toast.makeText(
                                                    context,
                                                    "Sign-Up successful, check your email for verification.",
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                                // Sign out the user immediately after sending the verification email
                                                auth.signOut()

                                                // Navigate back to login page
                                                requireActivity().supportFragmentManager.popBackStack()
                                                requireActivity().findViewById<View>(R.id.login_layout).visibility =
                                                    View.VISIBLE
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Failed to send verification email.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Failed to save user data in Firestore.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Sign-Up failed: ${task.exception?.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}