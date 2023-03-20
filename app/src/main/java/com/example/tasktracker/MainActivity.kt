package com.example.tasktracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.example.tasktracker.authentication.phone.PhoneLoginActivity
import com.example.tasktracker.databinding.ActivityMainBinding
import com.example.tasktracker.databinding.ProfileAtTopBinding
import com.example.tasktracker.fragments.HomeFragment
import com.example.tasktracker.fragments.NoteFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding
    private lateinit var profileAtTopBinding: ProfileAtTopBinding
    private lateinit var fab: FloatingActionButton
    private lateinit var addNoteFab: FloatingActionButton
    private lateinit var addTaskFab: FloatingActionButton
    private lateinit var addTaskToolTip: MaterialTextView
    private lateinit var addNoteToolTip: MaterialTextView
    private lateinit var displayNameTvText: String
    private lateinit var authTvText: String
    private var profilePicUrl: Uri? = null
    private lateinit var profilePic: RequestCreator


    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.rotate_open_animation)
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.rotate_close_animation)
    }
    private val fromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.from_bottom_animation)
    }
    private val toBottom: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.to_bottom_animation)
    }


    private var clicked = false
    private lateinit var auth: FirebaseAuth
    private lateinit var dbReference: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        profileAtTopBinding = ProfileAtTopBinding.bind(binding.root)
        fab = binding.fab
        addNoteFab = binding.addNoteFab
        addTaskFab = binding.addTaskFab
        addNoteToolTip = binding.addNoteToolTip
        addTaskToolTip = binding.addTaskToolTip

        val homeFragment = HomeFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, homeFragment)
        transaction.commit()

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser


        if (user != null) /* Set details in profileAtTopBinding if user != null */ {
            dbReference = FirebaseDatabase.getInstance().reference.child("users").child(user.uid)
                .child("userDetails")
            getUserDetailsFromFirebase(dbReference, user, profileAtTopBinding)

        } else /* user == null, redirect to PhoneLogInActivity */ {
            Toast.makeText(this, getString(R.string.auth_error_message), Toast.LENGTH_SHORT).show()
            val intent = Intent(this, PhoneLoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        profileAtTopBinding.avatarIv.setOnClickListener /* go to settings page */ {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("auth", profileAtTopBinding.authTv.text.toString())
            startActivity(intent)
        }


        profileAtTopBinding.dropdown.setOnClickListener {
            // create a popup menu
            val popup = PopupMenu(this, profileAtTopBinding.dropdown)
            // add items to the popup menu
            popup.menuInflater.inflate(R.menu.dropdown_menu, popup.menu)
            // set item click listener
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.settings -> {
                        val intent = Intent(this, SettingsActivity::class.java)
                        intent.putExtra("auth", profileAtTopBinding.authTv.text.toString())
                        startActivity(intent)
                        true
                    }
                    R.id.logout -> {
                        auth.signOut()
                        val intent = Intent(this, PhoneLoginActivity::class.java)
                        startActivity(intent)
                        finish()
                        true
                    }
                    else -> false
                }
            }
            // show the popup menu
            popup.show()
            // set popup menu background transparent


        }

        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    val fragment = HomeFragment()
                    replaceFragment(fragment)
                    true
                }
//                R.id.navigation_calendar -> {
//                    val fragment = CalendarFragment()
//                    replaceFragment(fragment)
//                    true
//                }
                R.id.navigation_notes -> {
                    val fragment = NoteFragment()
                    replaceFragment(fragment)
                    true
                }
                else -> false
            }
        }

        fab.setOnClickListener {
            onAddButtonClicked()
        }
        addNoteFab.setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java)
            startActivity(intent)
        }
        addTaskFab.setOnClickListener {
            // onAddTaskClicked()
            val intent = Intent(this, AddTaskActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onResume() {
        val user = auth.currentUser

        if (user != null) {
            getUserDetailsFromFirebase(dbReference, user, profileAtTopBinding)
            profileAtTopBinding.authTv.visibility = View.VISIBLE
        } else {
            Toast.makeText(this, getString(R.string.auth_error_message), Toast.LENGTH_SHORT).show()
            val intent = Intent(this, PhoneLoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        if (clicked) {
            addNoteFab.isClickable = false
            addNoteFab.startAnimation(toBottom)
            addNoteToolTip.startAnimation(toBottom)

            addTaskFab.isClickable = false
            addTaskFab.startAnimation(toBottom)
            addTaskToolTip.startAnimation(toBottom)
        }

        clicked = false

        when (binding.bottomNavigationView.selectedItemId) {
            R.id.navigation_home -> {
                val fragment = HomeFragment()
                replaceFragment(fragment)
            }
//            R.id.navigation_calendar -> {
//                val fragment = CalendarFragment()
//                replaceFragment(fragment)
//            }
            R.id.navigation_notes -> {
                val fragment = NoteFragment()
                replaceFragment(fragment)
            }
        }
        super.onResume()

    }

    private fun onAddButtonClicked() {
        clicked = !clicked
        setFabVisibility()
        setFabAnimation(clicked)
        setFabClickable(clicked)
    }

    private fun setFabClickable(clicked: Boolean) {
        if (!clicked) {
            addNoteFab.isClickable = false
            addTaskFab.isClickable = false

            addTaskToolTip.isClickable = false
            addNoteToolTip.isClickable = false

        } else {
            addNoteFab.isClickable = true
            addTaskFab.isClickable = true

            addTaskToolTip.isClickable = true
            addNoteToolTip.isClickable = true
        }
    }

    private fun setFabAnimation(clicked: Boolean) {
        if (!clicked) {
            addNoteFab.startAnimation(toBottom)
            addTaskFab.startAnimation(toBottom)

            addTaskToolTip.startAnimation(toBottom)
            addNoteToolTip.startAnimation(toBottom)

        } else {
            addNoteFab.startAnimation(fromBottom)
            addTaskFab.startAnimation(fromBottom)

            addTaskToolTip.startAnimation(fromBottom)
            addNoteToolTip.startAnimation(fromBottom)

        }
    }

    private fun setFabVisibility(/* clicked: Boolean */) {
//        if (!clicked) {
//            addNoteFab.hide()
//            addTaskFab.hide()
//        } else {
        addNoteFab.show()
        addTaskFab.show()
//        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.commit()
    }

    private fun getUserDetailsFromFirebase(
        dbReference: DatabaseReference,
        user: FirebaseUser,
        profileAtTopBinding: ProfileAtTopBinding
    ) {

        displayNameTvText = user.displayName.toString()
        profileAtTopBinding.name.text = displayNameTvText.split(" ")[0]


        dbReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                profilePicUrl = user.photoUrl
                profilePicUrl?.let {
                    Log.d(TAG, "profilePicUrl: $profilePicUrl")
                    profilePic = Picasso.get().load(profilePicUrl)
                    profilePic.into(profileAtTopBinding.avatarIv)
                }?: run {
                    profileAtTopBinding.avatarIv.setImageResource(R.drawable.avatar)
                }
                for (userSnapshot in snapshot.children) {
                    Log.d(TAG, "userSnapshot: ${userSnapshot.key}")
                    val method = snapshot.key?.let {
                        Log.d(TAG, "auth type: ${userSnapshot.child("method").value}")
                        userSnapshot.child("method").value.toString()
                    }
                    authTvText = if (method == "phone") {
                        userSnapshot.child("phone").value.toString()
                    } else {
                        user.email ?: userSnapshot.child("email").value.toString()
                    }
                    profileAtTopBinding.authTv.text = authTvText

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "onCancelled: ${error.message}")
                Snackbar.make(binding.root, error.toString(), Snackbar.LENGTH_LONG  ).show()

            }
        })
    }
}