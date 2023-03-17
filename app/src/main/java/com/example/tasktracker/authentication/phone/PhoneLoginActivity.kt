package com.example.tasktracker.authentication.phone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tasktracker.MainActivity
import com.example.tasktracker.authentication.email.EmailLoginActivity
import com.example.tasktracker.databinding.ActivityPhoneLoginBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class PhoneLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhoneLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var phoneNumberEt: TextInputEditText
    private lateinit var phoneNumber: String
    private lateinit var sendOTPButton: MaterialButton
    private lateinit var useEmailTv: TextView

    private val TAG = "PhoneLoginActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        init()
        registerEvents()



    }

    private fun init(){
        sendOTPButton = binding.sendOtpBtn
        phoneNumberEt = binding.phoneNumberEt
        useEmailTv = binding.useEmailTv
        auth = FirebaseAuth.getInstance()
    }

    private fun registerEvents(){

        useEmailTv.setOnClickListener{
            startActivity(Intent(this, EmailLoginActivity::class.java))
            finish()
        }

        sendOTPButton.setOnClickListener {
            phoneNumber = binding.phoneNumberEt.text.toString().trim()
            if (phoneNumber.isNotEmpty()) {
                if (phoneNumber.length == 10) {
                    phoneNumber = "+91$phoneNumber"

                    val options = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)
                    binding.progressBar.visibility = View.VISIBLE
                } else {
                    binding.phoneNumberEt.error = "Enter a valid phone number"
                }
            } else {
                binding.phoneNumberEt.error = "Enter a valid phone number"
            }
        }

    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:$credential")
            Toast.makeText(this@PhoneLoginActivity, "Verification Completed", Toast.LENGTH_SHORT).show()
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Log.d(TAG, "onVerificationFailed: Invalid request")
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Log.d(TAG, "onVerificationFailed: SMS Quota exceeded")
            }

            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String, token: PhoneAuthProvider.ForceResendingToken
        ) {

            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent:$verificationId")

            val intent = Intent(this@PhoneLoginActivity, OTPActivity::class.java)
            intent.putExtra("OTP", verificationId)
            intent.putExtra("resendToken", token)
            intent.putExtra("phoneNumber", phoneNumber)
            binding.progressBar.visibility = View.GONE
            startActivity(intent)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = task.result?.user
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    updateUI(user)

                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Snackbar.make(binding.root, "Invalid OTP", Snackbar.LENGTH_SHORT).show()
                    }
                    // Update UI

                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
}