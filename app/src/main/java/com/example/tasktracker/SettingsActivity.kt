package com.example.tasktracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tasktracker.authentication.CompleteRegistrationActivity
import com.example.tasktracker.authentication.phone.PhoneLoginActivity
import com.example.tasktracker.databinding.ActivitySettingsBinding
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var auth: FirebaseAuth
    private val TAG = "SettingsActivity"
    private val FACEBOOK_URL = "https://www.facebook.com/apptimates"
    private val TWITTER_URL = "https://twitter.com/Apptimates"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        auth = FirebaseAuth.getInstance()
        binding.emailOrPhoneTv.text = intent.getStringExtra("auth")

        registerEvents()

    }


    private fun registerEvents(){
        binding.signOutButton.setOnClickListener{
            auth.signOut()
            Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, PhoneLoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }

        binding.manageAccountButton.setOnClickListener{
            val intent = Intent(this, CompleteRegistrationActivity::class.java)
            intent.putExtra("edit", true)
            startActivity(intent)
        }

        binding.followFacebookTv.setOnClickListener{
            Log.d(TAG, "facebook clicked")
            gotoURL(FACEBOOK_URL)
        }
        binding.followTwitterTv.setOnClickListener{
            Log.d(TAG, "twitter clicked")
            gotoURL(TWITTER_URL)
        }

    }

    private fun gotoURL(url: String){
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}