package com.group29.localtreasury

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var isEmailMode = true // Flag to track current mode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //TODO: config after firebase is set up, line 32, 84, 93, and 113
        //auth = FirebaseAuth.getInstance()

        val loginLayout = findViewById<View>(R.id.login_layout)
        val emailEditText = findViewById<EditText>(R.id.email_login_input)
        val phoneEditText = findViewById<EditText>(R.id.phone_login_input)
        phoneEditText.visibility = View.GONE
        val passwordEditText = findViewById<EditText>(R.id.password_login_input)
        val switchLoginButton = findViewById<Button>(R.id.switch_login_btn)
        val signupButton = findViewById<Button>(R.id.signup_btn)
        val loginButton = findViewById<Button>(R.id.login_btn)

        // Toggle between email and phone login
        switchLoginButton.setOnClickListener {
            if (isEmailMode) {
                // Switch to phone mode after clicking switch button
                emailEditText.visibility = View.GONE
                phoneEditText.visibility = View.VISIBLE
                passwordEditText.visibility = View.GONE
            } else {
                // Switch to email mode after clicking switch button
                emailEditText.visibility = View.VISIBLE
                phoneEditText.visibility = View.GONE
                passwordEditText.visibility = View.VISIBLE
            }
            isEmailMode = !isEmailMode
        }

        loginButton.setOnClickListener {
            if (isEmailMode) {
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()
                //loginWithEmail(email, password)
            } else {
                val phone = phoneEditText.text.toString()
                loginWithPhone(phone)
            }
        }

        signupButton.setOnClickListener {
            // Hide the login layout
            loginLayout.visibility = View.GONE

            val fragment = SignUpFragment()
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            transaction.replace(android.R.id.content, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        //val currentUser = auth.currentUser
        //if (currentUser != null) {
            // If the user is already signed in, navigate to the main activity
        //    navigateToMainActivity()
        }
    }

    /**
    private fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //login successful, will go to main activity
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                } else {
                    //login failed, stay in login activity
                    Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
    **/

    private fun loginWithPhone(phone: String) {
        //TODO: Implement phone authentication logic here, or remove the phone option
    }

    /**
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close the LoginActivity so the user can't go back to it
    }
    **/

