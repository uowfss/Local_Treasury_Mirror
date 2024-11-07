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

/**
 * A simple [Fragment] subclass.
 * Use the [SignUpFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SignUpFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    //TODO: config after firebase is set up, line 33 and 77
    private var isEmailMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)
        //auth = FirebaseAuth.getInstance()

        val emailEditText = view.findViewById<EditText>(R.id.email_signup_input)
        val phoneEditText = view.findViewById<EditText>(R.id.phone_signup_input)
        phoneEditText.visibility = View.GONE
        val passwordEditText = view.findViewById<EditText>(R.id.password_signup_input)
        val switchSignupButton = view.findViewById<Button>(R.id.switch_signup_btn)
        val submitSignUpButton = view.findViewById<Button>(R.id.signup_submit_btn)
        val cancelSignUpButton = view.findViewById<Button>(R.id.signup_cancel_btn)

        // Toggle between email and phone sign-up
        switchSignupButton.setOnClickListener {
            if (isEmailMode) {
                // Switch to phone mode
                emailEditText.visibility = View.GONE
                phoneEditText.visibility = View.VISIBLE
                passwordEditText.visibility = View.GONE
            } else {
                // Switch to email mode
                emailEditText.visibility = View.VISIBLE
                phoneEditText.visibility = View.GONE
                passwordEditText.visibility = View.VISIBLE
            }
            isEmailMode = !isEmailMode
        }

        submitSignUpButton.setOnClickListener{
            if (isEmailMode) {
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()
                //signUpWithEmail(email, password)
            } else {
                val phone = phoneEditText.text.toString()
                signUpWithPhone(phone)
            }
        }

        cancelSignUpButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
            requireActivity().findViewById<View>(R.id.login_layout).visibility = View.VISIBLE
        }

        return view
    }

    /**
    private fun signUpWithEmail(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Sign-Up successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Sign-Up failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
    **/

    private fun signUpWithPhone(phone: String) {
        //TODO: Implement phone authentication for sign-up or remove this option
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         **/
    }
}