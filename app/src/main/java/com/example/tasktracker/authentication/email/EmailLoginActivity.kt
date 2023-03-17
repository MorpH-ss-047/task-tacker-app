package com.example.tasktracker.authentication.email

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tasktracker.MainActivity
import com.example.tasktracker.R
import com.example.tasktracker.databinding.ActivityEmailLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class EmailLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmailLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEmailLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        init()
        registerEvents()
    }

    private fun init() {
        auth = FirebaseAuth.getInstance()
    }

    private fun registerEvents() {

        binding.forgotPasswordTv.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }


        binding.signInButton.setOnClickListener {
            val email: String = binding.emailEt.text.toString().trim()
            val password: String = binding.passwordEt.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)

                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            // get user's name from firebase
                            val name = auth.currentUser?.displayName

                            Snackbar.make(binding.root, "Welcome back $name!", Snackbar.LENGTH_SHORT).show()

                            // navigate to main activity
                            val intent = Intent(this, MainActivity::class.java)
                            // pass name and email to main activity
                            intent.putExtra("name", name)
                            intent.putExtra("email", email)

                            startActivity(intent)

                        } else {
                            Log.e("EmailLoginActivity", "Error: ${task.exception?.message}")
                            Snackbar.make(binding.root, getString(R.string.error_message), Snackbar.LENGTH_LONG).show()
                        }
                    }

                    .addOnFailureListener { exception ->
                        Log.e("EmailLoginActivity", "Error: ${exception.message}")
                    }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.signUpTv.setOnClickListener {
            val intent = Intent(this, EmailSignupActivity::class.java)
            startActivity(intent)
        }
    }
}