package com.example.tasktracker

import android.content.Intent
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding
    private lateinit var fab: FloatingActionButton
    private lateinit var addNoteFab: FloatingActionButton
    private lateinit var addTaskFab: FloatingActionButton

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

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val profileAtTopBinding = ProfileAtTopBinding.bind(binding.root)


        if (user != null) /* Set details in profileAtTopBinding if user != null */ {
            dbReference = FirebaseDatabase.getInstance().reference.child("users").child(user.uid)
                .child("userDetails")

            // setting text in profileAtTop
            getUserDetailsFromFirebase(dbReference, user, profileAtTopBinding)
//            profileAtTopBinding.authTv.visibility = View.VISIBLE

//            profileAtTopBinding.emailTv.text = user.email

            if (user.photoUrl != null) {
                profileAtTopBinding.avatar.setImageURI(user.photoUrl)
            }
        } else /* user == null, redirect to PhoneLogInActivity */ {
            Toast.makeText(this, getString(R.string.auth_error_message), Toast.LENGTH_SHORT).show()
            val intent = Intent(this, PhoneLoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        profileAtTopBinding.avatar.setOnClickListener /* go to settings page */ {
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

        val homeFragment = HomeFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, homeFragment)
        transaction.commit()



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

        fab = binding.fab
        addNoteFab = binding.addNoteFab
        addTaskFab = binding.addTaskFab

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
            val profileAtTopBinding = ProfileAtTopBinding.bind(binding.root)
            profileAtTopBinding.name.text = user.displayName!!.split(" ")[0]
            getUserDetailsFromFirebase(dbReference, user, profileAtTopBinding)
            profileAtTopBinding.authTv.visibility = View.VISIBLE

            if (user.photoUrl != null) {
                profileAtTopBinding.avatar.setImageURI(user.photoUrl)
            }
        } else {
            Toast.makeText(this, getString(R.string.auth_error_message), Toast.LENGTH_SHORT).show()
            val intent = Intent(this, PhoneLoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        if (clicked) {
            addNoteFab.isClickable = false
            addNoteFab.startAnimation(toBottom)

            addTaskFab.isClickable = false
            addTaskFab.startAnimation(toBottom)
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
        setFabVisibility(clicked)
        setFabAnimation(clicked)
        setFabClickable(clicked)
    }

    private fun setFabClickable(clicked: Boolean) {
        if (!clicked) {
            addNoteFab.isClickable = false
            addTaskFab.isClickable = false
        } else {
            addNoteFab.isClickable = true
            addTaskFab.isClickable = true
        }
    }

    private fun setFabAnimation(clicked: Boolean) {
        if (!clicked) {
            addNoteFab.startAnimation(toBottom)
            addTaskFab.startAnimation(toBottom)
        } else {
            addNoteFab.startAnimation(fromBottom)
            addTaskFab.startAnimation(fromBottom)
        }
    }

    private fun setFabVisibility(clicked: Boolean) {
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
        dbReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    Log.d(TAG, "userSnapshot: ${userSnapshot.key}")
                    val method = snapshot.key?.let {
                        Log.d(TAG, "auth type: ${userSnapshot.child("method").value}")
                        profileAtTopBinding.name.text =
                            userSnapshot.child("fullName").value.toString().split(" ")[0]
                        userSnapshot.child("method").value.toString()
                    }
                    profileAtTopBinding.authTv.text = if (method == "phone") {
                        userSnapshot.child("phone").value.toString()
                    } else {
                        user.email ?: userSnapshot.child("email").value.toString()
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "onCancelled: ${error.message}")
                Snackbar.make(binding.root, error.toString(), Snackbar.LENGTH_LONG  ).show()

            }
        })
    }
}