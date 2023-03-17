package com.example.tasktracker

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.tasktracker.data.NoteData
import com.example.tasktracker.databinding.ActivityAddNoteBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNoteBinding

    private lateinit var noteData: NoteData

    private var noteId: String? = null
    private var noteTitle: String? = null
    private var noteDescription: String? = null


    private lateinit var noteTitleEt: TextInputEditText
    private lateinit var noteDescriptionEt: TextInputEditText


    private lateinit var auth: FirebaseAuth
    private lateinit var authId: String
    private lateinit var dbReference: DatabaseReference

    private var edit: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


//        binding.deleteButton.visibility = View.GONE
//        binding.deleteButton.isEnabled = false
//        binding.deleteButton.isClickable = false

        init()
        registerEvents()

    }

    private fun init() {
        // initialize firebase auth

        auth = FirebaseAuth.getInstance()
        authId = auth.currentUser!!.uid
        dbReference =
            FirebaseDatabase.getInstance().reference.child("users").child(authId).child("notes")
        noteTitleEt = binding.noteTitleEt
        noteDescriptionEt = binding.noteDescriptionEt

        edit = intent.getBooleanExtra("edit", false)
        if (edit) {
            // set title in toolbar
            binding.toolbarTitle.text = getString(R.string.edit_note)

            // get note data from intent
            noteId = intent.getStringExtra("noteId")
            noteTitle = intent.getStringExtra("noteTitle")
            noteDescription = intent.getStringExtra("noteDescription")

            // set note data in edit text
            noteTitleEt.setText(noteTitle)
            noteDescriptionEt.setText(noteDescription)
        } else { /* new note == true */
            // set title in toolbar
            binding.toolbarTitle.text = getString(R.string.add_note)
            binding.deleteButton.visibility = View.GONE
            binding.deleteButton.isEnabled = false
            binding.deleteButton.isClickable = false
        }
    }


    private fun registerEvents() {

        binding.saveButton.setOnClickListener { saveButtonOnClickListener() }

        binding.backButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Discard Changes")
                .setMessage("Are you sure you want to exit? Changes will not be saved")
                .setPositiveButton("Yes") { _, _ ->
                    Toast.makeText(this, "Note not saved", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        binding.deleteButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Yes") { _, _ ->
                    deleteButtonOnClickListener()
                    Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

    }

    private fun deleteButtonOnClickListener() {
        Log.d("AddNoteActivity", "delete button clicked")
        noteData = NoteData(
            id = intent.getStringExtra("noteId")!!,
            title = noteTitleEt.text.toString(),
            description = noteDescriptionEt.text.toString()
        )
        deleteNoteFromFirebase(noteData)
    }


    private fun saveButtonOnClickListener() {
        // save note
        if (edit) {
            // update note

            // get note data from edit text and make noteData object
            noteData = NoteData(
                id = intent.getStringExtra("noteId")!!,
                title = noteTitleEt.text.toString(),
                description = noteDescriptionEt.text.toString()
            )

            if (noteData.title.isNotEmpty() && noteData.description.isNotEmpty()) {
                updateNoteInFirebase(noteData) /* update note in firebase */
                Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }


        } else { /* new note == true */
            // add note
            val noteTitle = noteTitleEt.text.toString()
            val noteDescription = noteDescriptionEt.text.toString()
            if (noteDescription.isNotEmpty()) {
                noteData = NoteData(
                    title = noteTitle, description = noteDescription
                )
                saveNoteInFirebase(noteData)
                finish()
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }


    /* Firebase operations */

    private fun saveNoteInFirebase(noteData: NoteData) {
        Snackbar.make(binding.root, "Saving note...", Snackbar.LENGTH_SHORT).show()
        dbReference.push().setValue(noteData.toMap()).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("AddNoteActivity", "Note saved")
                Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("AddNoteActivity", "Error while saving note", it.exception)
                Toast.makeText(this, "Error while saving note...", Toast.LENGTH_SHORT)
                    .show()
            }

        }
            .addOnFailureListener {
                Log.d("AddNoteActivity", "Error while saving note", it)
                Toast.makeText(this, "Error while saving note...", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun updateNoteInFirebase(noteData: NoteData) {
        // update note in firebase
        dbReference.child(noteData.id).updateChildren(noteData.toMap()).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("AddNoteActivity", "Note updated, note id = ${noteData.id}")
                Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("AddNoteActivity", "Error while updating note [note id ${noteData.id}]", it.exception)
                Toast.makeText(
                    this, "Error while updating note...", Toast.LENGTH_SHORT
                ).show()
            }
        }
            .addOnFailureListener {
                Log.d("AddNoteActivity", "Error while updating note [note id ${noteData.id}]", it)
                Toast.makeText(
                    this, "Error while updating note...", Toast.LENGTH_SHORT
                ).show()
            }
    }



    private fun deleteNoteFromFirebase(noteData: NoteData) {
        Log.d("AddNoteActivity", "deleting note from firebase: ${noteData.id}")
        dbReference.child(noteData.id).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("AddNoteActivity", "Note deleted [note id ${noteData.id}]")
                Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("AddNoteActivity", "Error while deleting note [note id ${noteData.id}]", it.exception)
                Toast.makeText(
                    this, "Error while deleting note...", Toast.LENGTH_SHORT
                ).show()
            }
        }
            .addOnFailureListener {
                Log.d("AddNoteActivity", "Error while deleting note [note id ${noteData.id}]", it)
                Toast.makeText(
                    this, "Error while deleting note...", Toast.LENGTH_SHORT
                ).show()
            }
    }

}
