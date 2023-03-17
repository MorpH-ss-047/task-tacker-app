package com.example.tasktracker.authentication.email

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.tasktracker.databinding.ActivityForgotPasswordBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.sendButton.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            if (email.isNotEmpty()) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(binding.emailEt.text.toString())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("EmailLoginActivity", "Email sent.")
                            Snackbar.make(binding.root, "Email sent.", Snackbar.LENGTH_SHORT).show()
                        }
                    }
            }
        }

    }
}