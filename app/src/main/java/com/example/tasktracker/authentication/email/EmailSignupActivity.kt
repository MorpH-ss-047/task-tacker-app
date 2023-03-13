package com.example.tasktracker.authentication.email

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tasktracker.R
import com.example.tasktracker.authentication.CompleteRegistrationActivity
import com.example.tasktracker.databinding.ActivityEmailSignupBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class EmailSignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmailSignupBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailSignupBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        init()
        registerEvents()
    }
    private fun init() {
        auth = FirebaseAuth.getInstance()
    }
    private fun registerEvents() {
        binding.signUpButton.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            val password = binding.passwordEt.text.toString().trim()
            val confirmPassword = binding.confirmPasswordEt.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                // navigate user to complete registration activity
                                Log.d("EmailSignupActivity", "User created successfully")
                                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, CompleteRegistrationActivity::class.java)
                                intent.putExtra("fromPhone", false)
                                intent.putExtra("phoneOrEmail", email)
                                startActivity(intent)
                                finish()
                            } else {
                                Log.e("EmailSignupActivity", "Error [else block in addOnCompleteListener]: ${task.exception?.message}")
                                Snackbar.make(binding.root, getString(R.string.error_message), Snackbar.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("EmailSignupActivity", "Error [onFailure Listener]: ${exception.message}")
                            Snackbar.make(binding.root, getString(R.string.error_message), Snackbar.LENGTH_SHORT).show()
                        }
                } else {
                    Log.d("EmailSignupActivity", "Passwords do not match")
                    Snackbar.make(binding.root, "Passwords do not match", Snackbar.LENGTH_SHORT).show()
                }
            } else {
                Log.d("EmailSignupActivity", "Empty fields")
                Snackbar.make(binding.root, "Please fill out all fields", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.signInTv.setOnClickListener {
            val intent = Intent(this, EmailLoginActivity::class.java)
            startActivity(intent)
        }
    }
}