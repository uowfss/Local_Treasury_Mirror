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
import com.google.firebase.firestore.FirebaseFirestore

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
    }


    private fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        // Check if the user profile is complete in Firestore
                        val userId = user.uid
                        val firestore = FirebaseFirestore.getInstance()

                        firestore.collection("users").document(userId)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document.exists() &&
                                    document.contains("firstName") &&
                                    document.contains("lastName")) {
                                    // Profile is complete; proceed to main activity
                                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                                    navigateToMainActivity()
                                } else {
                                    // Profile is incomplete; show UserDetailsDialogFragment
                                    Toast.makeText(
                                        this,
                                        "Please complete your profile details.",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    val dialog = UserDetailDialog()
                                    val bundle = Bundle()
                                    bundle.putString("USER_ID", userId)
                                    bundle.putBoolean("FROM_SIGNUP", false)
                                    dialog.arguments = bundle
                                    dialog.show(supportFragmentManager, "UserDetailsDialogFragment")
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this,
                                    "Failed to fetch user details. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        // Email is not verified
                        Toast.makeText(this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show()
                        auth.signOut() // Sign out the user to prevent an unverified session
                    }
                } else {
                    // Login failed
                    Toast.makeText(this, "Login failed: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}


