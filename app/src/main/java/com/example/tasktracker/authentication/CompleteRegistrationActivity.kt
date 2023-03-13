package com.example.tasktracker.authentication

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tasktracker.MainActivity
import com.example.tasktracker.R
import com.example.tasktracker.authentication.email.EmailSignupActivity
import com.example.tasktracker.authentication.phone.PhoneLoginActivity
import com.example.tasktracker.data.UserData
import com.example.tasktracker.databinding.ActivityCompleteRegistrationBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class CompleteRegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompleteRegistrationBinding

    private lateinit var fullNameEt: TextInputEditText
    private lateinit var ageEt: TextInputEditText
    private lateinit var maleButton: MaterialButton
    private lateinit var femaleButton: MaterialButton
    private lateinit var otherButton: MaterialButton
    private lateinit var phoneOrEmail: TextInputEditText
    private lateinit var saveButton: MaterialButton

    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    private var fromPhone: Boolean = true
    private lateinit var phoneOrEmailIntent: String


    private lateinit var fullName: String
    private lateinit var age: String
    private lateinit var gender: String
    private lateinit var email: String
    private lateinit var phone: String

    private var genderChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompleteRegistrationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        fromPhone = intent.getBooleanExtra("fromPhone", false)
        phoneOrEmailIntent = intent.getStringExtra("phoneOrEmail").toString()

        if (fromPhone) {
            binding.phoneOrEmailEt.hint = "Email"
            binding.phoneOrEmailEt.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

        } else {
            binding.phoneOrEmailEt.hint = "Phone"
            binding.phoneOrEmailEt.inputType = InputType.TYPE_CLASS_PHONE
        }


        init()
        registerEvents()

    }


    private fun init() {
        auth = FirebaseAuth.getInstance()
        val authId = auth.currentUser!!.uid
        dbRef = FirebaseDatabase.getInstance().reference.child("users").child(authId)
            .child("userDetails")



        fullNameEt = binding.fullNameEt
        ageEt = binding.ageEt
        maleButton = binding.maleBtn
        femaleButton = binding.femaleBtn
        otherButton = binding.otherBtn
        phoneOrEmail = binding.phoneOrEmailEt
        saveButton = binding.saveButton


    }


    private fun registerEvents() {
        saveButton.setOnClickListener {
            fullName = fullNameEt.text.toString()
            age = ageEt.text.toString()
            if (fromPhone) {
                email = phoneOrEmail.text.toString()
                phone = phoneOrEmailIntent
            } else {
                phone = phoneOrEmail.text.toString()
                email = phoneOrEmailIntent
            }

            if (fullName.isNotEmpty() && age.isNotEmpty() && phoneOrEmail.text.toString()
                    .isNotEmpty() && genderChecked
            ) {

                val currentUser = auth.currentUser
                if (currentUser == null) {
                    Snackbar.make(binding.root, "Please login again", Snackbar.LENGTH_SHORT).show()
                    if (fromPhone) {
                        val intent = Intent(this, PhoneLoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val intent = Intent(this, EmailSignupActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else { /* if user is not null */

                    currentUser.updateProfile(
                        UserProfileChangeRequest.Builder().setDisplayName(fullName).build()
                    )
                    currentUser.updateEmail(email)

                    val userData = UserData(fullName, age, gender, phone, email)

                    dbRef.push().setValue(userData.toMap())
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(
                                    this, "User data saved", Toast.LENGTH_SHORT
                                ).show()
                                val intent = Intent(this, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            } else {
                                Log.d("CompleteUserRegistration", "registerEvents: ${it.exception}")
                                Toast.makeText(
                                    this, "User data not saved", Toast.LENGTH_SHORT
                                ).show()
                            }
                        }.addOnFailureListener {
                            Snackbar.make(binding.root, getString(R.string.error_message), Snackbar.LENGTH_SHORT).show()
                            Log.d("CompleteUserRegistration", "registerEvents: ${it.message}")
                        }
                }

            } else if (!genderChecked) {
                Snackbar.make(binding.root, "Please choose gender", Snackbar.LENGTH_SHORT).show()
            } else if (fullName.isEmpty()) {
                binding.fullNameTil.error = "Please enter full name"
            } else if (age.isEmpty()) {
                binding.ageTil.error = "Please enter age"
            } else if (phoneOrEmail.text.toString().isEmpty()) {
                if (binding.phoneOrEmailTil.hint == "Email") {
                    binding.phoneOrEmailTil.error = "Please enter email"
                } else {
                    binding.phoneOrEmailTil.error = "Please enter phone"
                }
            }

        }
        maleButton.setOnClickListener {
            maleButton.isSelected = !maleButton.isSelected
            femaleButton.isSelected = false
            otherButton.isSelected = false
            genderChecked = true
            gender = "male"
        }
        femaleButton.setOnClickListener {
            femaleButton.isSelected = !femaleButton.isSelected
            maleButton.isSelected = false
            otherButton.isSelected = false
            genderChecked = true
            gender = "female"

        }
        otherButton.setOnClickListener {
            otherButton.isSelected = !otherButton.isSelected
            maleButton.isSelected = false
            femaleButton.isSelected = false
            genderChecked = true
            gender = "other"

        }
    }

}