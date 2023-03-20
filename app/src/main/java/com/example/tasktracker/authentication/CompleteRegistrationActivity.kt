package com.example.tasktracker.authentication

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputType
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tasktracker.MainActivity
import com.example.tasktracker.R
import com.example.tasktracker.authentication.email.EmailSignupActivity
import com.example.tasktracker.authentication.phone.PhoneLoginActivity
import com.example.tasktracker.data.UserData
import com.example.tasktracker.databinding.ActivityCompleteRegistrationBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException


class CompleteRegistrationActivity : AppCompatActivity() {

    private val TAG = "CompleteRegistrationActivity"
    private val REQUEST_IMAGE_PICK = 1
    private val REQUEST_READ_EXTERNAL_STORAGE = 2
    private var imageBitmap: Bitmap? = null

    private lateinit var binding: ActivityCompleteRegistrationBinding

    private lateinit var fullNameEt: TextInputEditText
    private lateinit var ageEt: TextInputEditText
    private lateinit var maleButton: MaterialButton
    private lateinit var femaleButton: MaterialButton
    private lateinit var otherButton: MaterialButton
    private lateinit var phoneOrEmail: TextInputEditText
    private lateinit var saveButton: MaterialButton
    private lateinit var avatarIv: ShapeableImageView
    private lateinit var uploadIv: ImageView


    private lateinit var auth: FirebaseAuth
    private lateinit var dbReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private var downloadUri: Uri? = null



    private var fromPhone: Boolean = true
    private lateinit var phoneOrEmailIntent: String


    private lateinit var fullName: String
    private lateinit var age: String
    private lateinit var gender: String
    private lateinit var email: String
    private lateinit var phone: String

    private var genderChecked = false
    private var edit: Boolean = false

    private var currentUser: FirebaseUser? = null
    private lateinit var authId: String
    private var nodeId: String? = null
    private var method: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompleteRegistrationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        fromPhone = intent.getBooleanExtra("fromPhone", false)
        phoneOrEmailIntent = intent.getStringExtra("phoneOrEmail").toString()
        edit = intent.getBooleanExtra("edit", false)

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
        authId = auth.currentUser!!.uid
        dbReference = FirebaseDatabase.getInstance().reference.child("users")
            .child(authId).child("userDetails")
        storageReference = FirebaseStorage.getInstance().reference.child("profile_pictures/$authId")

        currentUser = auth.currentUser

        fullNameEt = binding.fullNameEt
        ageEt = binding.ageEt
        maleButton = binding.maleBtn
        femaleButton = binding.femaleBtn
        otherButton = binding.otherBtn
        phoneOrEmail = binding.phoneOrEmailEt
        saveButton = binding.saveButton
        avatarIv = binding.avatarIv
        uploadIv = binding.uploadIv
        currentUser?.let {
            it.photoUrl?.let {uri ->
                Picasso.get().load(uri).into(avatarIv)
            }?: run {
                Picasso.get().load(R.drawable.avatar).into(avatarIv)
            }
        }


        if (edit) /* fetches and sets data in the ui */ {

            dbReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (userSnapshot in snapshot.children) {
                        Log.d(TAG, "userSnapshot: ${userSnapshot.key}")
                        method = userSnapshot.key?.let {
                            nodeId = it
                            Log.d(TAG, "auth type: ${userSnapshot.child("method").value}")
                            binding.fullNameEt.text =
                                userSnapshot.child("fullName").value.toString().toEditable()
                            Log.d(
                                TAG,
                                "full name: ${userSnapshot.child("fullName").value.toString()}"
                            )
                            binding.ageEt.text =
                                userSnapshot.child("age").value.toString().toEditable()

                            when (userSnapshot.child("gender").value.toString()) {
                                "male" -> {
                                    gender = "male"
                                    maleButton.isSelected = true
                                    femaleButton.isSelected = false
                                    otherButton.isSelected = false
                                    genderChecked = true
                                }
                                "female" -> {
                                    gender = "female"
                                    femaleButton.isSelected = true
                                    maleButton.isSelected = false
                                    otherButton.isSelected = false
                                    genderChecked = true
                                }
                                "other" -> {
                                    gender = "other"
                                    otherButton.isSelected = true
                                    maleButton.isSelected = false
                                    femaleButton.isSelected = false
                                    genderChecked = true
                                }
                            }
                            phone = userSnapshot.child("phone").value.toString()
                            email = userSnapshot.child("email").value.toString()
                            Log.d(TAG, "email: $email")
                            Log.d(TAG, "phone: $phone")
                            userSnapshot.child("method").value.toString()

                        }
                        binding.phoneOrEmailEt.text = if (method == "phone") {
                            binding.phoneOrEmailEt.hint = "Email"
                            binding.phoneOrEmailEt.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                            email.toEditable()
                        } else {
                            binding.phoneOrEmailEt.hint = "Phone"
                            binding.phoneOrEmailEt.inputType = InputType.TYPE_CLASS_PHONE
                            phone.toEditable()
                        }


                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "onCancelled: ${error.message}")
                    Snackbar.make(binding.root, error.toString(), Snackbar.LENGTH_LONG).show()

                }
            })


        }
    }

    private fun registerEvents() {
        saveButton.setOnClickListener {
            fullName = fullNameEt.text.toString()
            age = ageEt.text.toString()

            if(edit){
                if (method == "phone") {
                    email = phoneOrEmail.text.toString()
                } else if (method == "email") {
                    phone = phoneOrEmail.text.toString()
                }
            } else {
                if (fromPhone) {
                    email = phoneOrEmail.text.toString()
                    phone = phoneOrEmailIntent
                    method = "phone"
                } else {
                    phone = phoneOrEmail.text.toString()
                    email = phoneOrEmailIntent
                    method = "email"
                }
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
                    if (edit) {

                        Log.d(TAG, "registerEvents: updating DisplayName")
                        imageBitmap?.let {
                            uploadImageToFirebase(it)
                        }
                        currentUser.updateProfile(
                            UserProfileChangeRequest.Builder().setDisplayName(fullName).build()
                        )
                        Log.d(TAG, "registerEvents: updated DisplayName")
                        Log.d(TAG, "registerEvents: updating Email")
                        currentUser.updateEmail(email)
                        Log.d(TAG, "registerEvents: updated DisplayName")

                        val userData = UserData(fullName, age, gender, phone, email, method)
                        Log.d(TAG, "registerEvents: updating user data $nodeId")
                        dbReference.child(nodeId!!).updateChildren(userData.toMap())
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    Log.d(TAG, "registerEvents: user data saved successfully")
                                    Toast.makeText(
                                        this, "User data saved", Toast.LENGTH_SHORT
                                    ).show()
                                    val intent = Intent(this, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Log.d(
                                        "CompleteUserRegistration",
                                        "registerEvents: ${it.exception}"
                                    )
                                    Toast.makeText(
                                        this, "User data not saved", Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }.addOnFailureListener {
                                Log.d(
                                    "CompleteUserRegistration",
                                    "registerEvents: ${it.message}"
                                )
                            }
                    }
                    else {
                        imageBitmap?.let {
                            uploadImageToFirebase(it)
                        }
                        currentUser.updateProfile(
                            UserProfileChangeRequest.Builder()
                                .setDisplayName(fullName)
//                                .setPhotoUri(Uri.parse())
                                .build()
                        )
                        currentUser.updateEmail(email)
                        val userData = UserData(fullName, age, gender, phone, email, method)
                        dbReference.push().setValue(userData.toMap())
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
                                    Log.d(
                                        "CompleteUserRegistration",
                                        "registerEvents: ${it.exception}"
                                    )
                                    Toast.makeText(
                                        this, "User data not saved", Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }.addOnFailureListener {
                                Snackbar.make(
                                    binding.root,
                                    getString(R.string.error_message),
                                    Snackbar.LENGTH_SHORT
                                ).show()
                                Log.d("CompleteUserRegistration", "registerEvents: ${it.message}")
                            }
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

        binding.uploadIv.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "registerEvents: Permission granted")
                pickImageFromGallery()
            } else {
                Log.d(TAG, "registerEvents: Permission not granted, requesting permission")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_READ_EXTERNAL_STORAGE
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun pickImageFromGallery() {
        Log.d(TAG, "pickImageFromGallery: opening gallery")
        val pickImageIntent = Intent(Intent.ACTION_PICK)
        pickImageIntent.type = "image/*"
        startActivityForResult(pickImageIntent, REQUEST_IMAGE_PICK)
    }

    // Handles the result of the gallery intent
    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_PICK -> {
                    try {
                        val imageUri = data?.data
                        Log.d(TAG, "onActivityResult: converting image to bitmap")
                        imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                        Log.d(TAG, "onActivityResult: converted image to bitmap")

                        Log.d(TAG, "onActivityResult: setting image to image view")
                        avatarIv.setImageBitmap(imageBitmap)
                        Log.d(TAG, "onActivityResult: set image to image view")
                        Log.d(TAG, "onActivityResult: uploading image to firebase")


                    } catch (e: IOException) {
                        Log.d(TAG, "onActivityResult: IOException: ${e.message}")
                        e.printStackTrace()
                        Toast.makeText(this, "Image selection failed", Toast.LENGTH_SHORT).show()
                    } catch (e: NullPointerException) {
                        Log.d(TAG, "onActivityResult: NullPointerException: ${e.message}")
                        e.printStackTrace()
                        Toast.makeText(this, "Image selection failed", Toast.LENGTH_SHORT).show()
                    } catch (e: FileNotFoundException) {
                        Log.d(TAG, "onActivityResult: FileNotFoundException: ${e.message}")
                        e.printStackTrace()
                        Toast.makeText(this, "File does not exist", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Uploads the image to Firebase Storage and updates the user's profile picture URL in Firebase Realtime Database
    private fun uploadImageToFirebase(imageBitmap: Bitmap) {
        val currentUser = auth.currentUser
        val uid = currentUser?.uid
        if (uid != null) {
            Log.d(TAG, "uploadImageToFirebase: uploading image to firebase storage")
            Snackbar.make(binding.root, "Uploading image...", Snackbar.LENGTH_SHORT).show()
            val uploadTask = storageReference.putBytes(bitmapToByteArray(imageBitmap))
            Log.d(TAG, "uploadImageToFirebase: upload task started")

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        Log.d(TAG, "uploadImageToFirebase#continueWithTask: exception: ${it.message}")
                        throw it
                    }
                }
                storageReference.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "uploadImageToFirebase: image uploaded to firebase storage successfully")
                    Snackbar.make(binding.root, "Image uploaded successfully", Snackbar.LENGTH_SHORT).show()

                    downloadUri = task.result
                    saveProfilePictureUrl(downloadUri.toString())
                }
            }
                .addOnFailureListener{
                    Log.d(TAG, "uploadImageToFirebase: onFailure listener ${it.message}")
                }
        }
    }

    // Converts a Bitmap to a byte array for uploading to Firebase Storage
    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return outputStream.toByteArray()
    }

    // Updates the user's profile picture URL in Firebase Realtime Database
    private fun saveProfilePictureUrl(url: String) {
        val currentUser = auth.currentUser
        val uid = currentUser?.uid
        if (uid != null) {
            Log.d(TAG, "saveProfilePictureUrl: Saving image URL at $nodeId")
            if (nodeId != null) {
                dbReference.child(nodeId!!).child("profile_picture_url").setValue(url)
                    .addOnSuccessListener {
                        currentUser.updateProfile(
                            UserProfileChangeRequest.Builder()
                                .setPhotoUri(Uri.parse(url))
                                .build()
                        )
                        Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show()

                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to update profile picture", Toast.LENGTH_SHORT)
                            .show()
                    }
            } else {
                currentUser.updateProfile(
                    UserProfileChangeRequest.Builder()
                        .setPhotoUri(Uri.parse(url))
                        .build()
                )
            }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, PhoneLoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}




private fun String.toEditable(): Editable? {
    return Editable.Factory.getInstance().newEditable(this)
}
