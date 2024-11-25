package com.group29.localtreasury

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()

        val loginLayout = findViewById<View>(R.id.login_layout)
        val emailEditText = findViewById<EditText>(R.id.email_login_input)
        val passwordEditText = findViewById<EditText>(R.id.password_login_input)
        val signupButton = findViewById<Button>(R.id.signup_btn)
        val loginButton = findViewById<Button>(R.id.login_btn)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            loginWithEmail(email, password)
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
//        val currentUser = auth.currentUser
//        if (currentUser != null) {
//            // If the user is already signed in, navigate to the main activity
//            Log.d("BG",currentUser.getUid())
//            navigateToMainActivity()
//        }
    }

    private fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //login successful, will go to main activity
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        // Email is verified
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                        navigateToMainActivity()
                    } else {
                        // Email is not verified
                        Toast.makeText(this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show()
                        auth.signOut() // Sign out the user to prevent an unverified session
                    }
                } else {
                    //login failed, stay in login activity
                    Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}


