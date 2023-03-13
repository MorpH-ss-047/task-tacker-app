package com.example.tasktracker.authentication.phone

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.tasktracker.MainActivity
import com.example.tasktracker.R
import com.example.tasktracker.authentication.CompleteRegistrationActivity
import com.example.tasktracker.databinding.ActivityOtpBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit


class OTPActivity : AppCompatActivity() {

    val TAG = "OTPActivity"

    private lateinit var binding: ActivityOtpBinding
    private lateinit var verifyOTPButton: MaterialButton
    private lateinit var didntReceiveOTPTv: TextView
    private lateinit var resendOTPTv: TextView
    private lateinit var wrongOTPTv: TextView
    private lateinit var otpEt1: TextInputEditText
    private lateinit var otpEt2: TextInputEditText
    private lateinit var otpEt3: TextInputEditText
    private lateinit var otpEt4: TextInputEditText
    private lateinit var otpEt5: TextInputEditText
    private lateinit var otpEt6: TextInputEditText

    private lateinit var auth: FirebaseAuth
    private lateinit var otp: String
    private lateinit var phoneNumber: String
    private lateinit var resendingToken: PhoneAuthProvider.ForceResendingToken

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        otp = intent.getStringExtra("OTP").toString()
        if (Build.VERSION.SDK_INT >= 33) { // API 33 = Android 12 = S = Tiramisu
            resendingToken = intent.getParcelableExtra("resendToken", PhoneAuthProvider.ForceResendingToken::class.java)!!
        }else{
            @Suppress("DEPRECATION")
            resendingToken = intent.getParcelableExtra("resendToken")!!
        }
        phoneNumber = intent.getStringExtra("phoneNumber").toString()

        init()
        addTextChangedListener()
        resendOTPTvVisibility()

        verifyOTPButton.setOnClickListener {
            val typedOTP =
                (otpEt1.text.toString() + otpEt2.text.toString() + otpEt3.text.toString() + otpEt4.text.toString() + otpEt5.text.toString() + otpEt6.text.toString())
            if (typedOTP.isNotEmpty() && typedOTP.length == 6) {
                val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(otp, typedOTP)
                signInWithPhoneAuthCredential(credential)
            }
        }

        resendOTPTv.setOnClickListener {
            resendVerificationCode()
            resendOTPTvVisibility()
        }

    }

    private fun init() {
        auth = FirebaseAuth.getInstance()
        verifyOTPButton = binding.verifyOTPButton
        didntReceiveOTPTv = binding.didntRecieveOtpTv
        resendOTPTv = binding.resendOTPTv
        wrongOTPTv = binding.wrongOTPTv

        otpEt1 = binding.otpEt1
        otpEt2 = binding.otpEt2
        otpEt3 = binding.otpEt3
        otpEt4 = binding.otpEt4
        otpEt5 = binding.otpEt5
        otpEt6 = binding.otpEt6
    }

    private fun resendOTPTvVisibility() {
        otpEt1.setText("")
        otpEt2.setText("")
        otpEt3.setText("")
        otpEt4.setText("")
        otpEt5.setText("")
        otpEt6.setText("")
        didntReceiveOTPTv.visibility = View.INVISIBLE
        resendOTPTv.visibility = View.INVISIBLE
        resendOTPTv.isEnabled = false

        Handler(Looper.myLooper()!!).postDelayed({
            didntReceiveOTPTv.visibility = View.VISIBLE
            resendOTPTv.visibility = View.VISIBLE
            resendOTPTv.isEnabled = true
        }, 60000)
    }

    private fun resendVerificationCode() {
        val options = PhoneAuthOptions.newBuilder(auth).setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS).setActivity(this).setCallbacks(callbacks)
            .setForceResendingToken(resendingToken).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
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

            otp = verificationId
            resendingToken = token


        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "signInWithCredential:success")
                val user = task.result?.user
                Snackbar.make(binding.root, "Login Successful", Toast.LENGTH_SHORT).show()
                updateUI(user)

            } else {
                // Sign in failed, display a message and update the UI
                Log.w(TAG, "signInWithCredential:failure", task.exception)
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    // The verification code entered was invalid
                    Log.w(TAG, "signInWithCredential:failure: Invalid OTP")
                    wrongOTPTv.visibility = View.VISIBLE
                }
                // Update UI

            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {

            if (user.displayName != null) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, CompleteRegistrationActivity::class.java)
                intent.putExtra("phoneOrEmail", phoneNumber)
                intent.putExtra("fromPhone", true)
                startActivity(intent)
                finish()
            }

        }
    }

    private fun addTextChangedListener() {
        otpEt1.addTextChangedListener(EditTextWatcher(otpEt1))
        otpEt2.addTextChangedListener(EditTextWatcher(otpEt2))
        otpEt3.addTextChangedListener(EditTextWatcher(otpEt3))
        otpEt4.addTextChangedListener(EditTextWatcher(otpEt4))
        otpEt5.addTextChangedListener(EditTextWatcher(otpEt5))
        otpEt6.addTextChangedListener(EditTextWatcher(otpEt6))
    }

    /**
     * This class helps to jump the cursor to the next EditText when the user enters a digit in the current EditText
     */
    inner class EditTextWatcher(private val view: View) : TextWatcher {

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {



        }

        override fun afterTextChanged(s: Editable?) {
            // Get the text from the current EditText
            val text = s.toString()
            // Move the cursor to the next EditText
            when (view.id) {
                R.id.otpEt1 -> {
                    if (text.isNotEmpty()) otpEt2.requestFocus()
                }
                R.id.otpEt2 -> {
                    if (text.isEmpty()) otpEt1.requestFocus() else otpEt3.requestFocus()
                }
                R.id.otpEt3 -> {
                    if (text.isEmpty()) otpEt2.requestFocus() else otpEt4.requestFocus()
                }
                R.id.otpEt4 -> {
                    if (text.isEmpty()) otpEt3.requestFocus() else otpEt5.requestFocus()
                }
                R.id.otpEt5 -> {
                    if (text.isEmpty()) otpEt4.requestFocus() else otpEt6.requestFocus()
                }
                R.id.otpEt6 -> {
                    if (text.isEmpty()) otpEt5.requestFocus()
                }
                else -> {
                    otpEt1.requestFocus()
                }

            }
        }

    }
}

