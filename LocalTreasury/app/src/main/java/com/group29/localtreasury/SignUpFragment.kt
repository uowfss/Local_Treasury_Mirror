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
import com.google.firebase.auth.FirebaseAuth

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
        val submitSignUpButton = view.findViewById<Button>(R.id.signup_submit_btn)
        val cancelSignUpButton = view.findViewById<Button>(R.id.signup_cancel_btn)

        submitSignUpButton.setOnClickListener{
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            signUpWithEmail(email, password)
        }

        cancelSignUpButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
            requireActivity().findViewById<View>(R.id.login_layout).visibility = View.VISIBLE
        }
        return view
    }

    private fun signUpWithEmail(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.sendEmailVerification()?.addOnCompleteListener { emailTask ->
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
                    Toast.makeText(context, "Sign-Up failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}