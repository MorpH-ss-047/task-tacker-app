package com.example.tasktracker

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.tasktracker.crypto.CryptoManager
import com.example.tasktracker.data.NoteData
import com.example.tasktracker.databinding.ActivityAddNoteBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNoteBinding


    private lateinit var cryptoManager: CryptoManager
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

        init()
        registerEvents()

    }

    private fun init() {

        cryptoManager = CryptoManager()

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

            // set focus on description edit text at end of text
          noteTitleEt.requestFocus()
                showKeyboard()
                // set cursor to end of text else it will be at the start(if text is null)
                noteTitleEt.setSelection(noteTitleEt.text?.length ?: 0)
        } else { /* new note == true */
            // set title in toolbar
            binding.toolbarTitle.text = getString(R.string.add_note)
            binding.deleteButton.visibility = View.GONE
            binding.deleteButton.isEnabled = false
            binding.deleteButton.isClickable = false
            noteTitleEt.requestFocus()
        }
    }

    private fun registerEvents() {

        binding.saveButton.setOnClickListener { saveButtonOnClickListener() }

        binding.backButton.setOnClickListener {
            AlertDialog.Builder(this).setTitle("Discard Changes")
                .setMessage("Are you sure you want to exit? Changes will not be saved")
                .setPositiveButton("Yes") { _, _ ->
                    Toast.makeText(this, "Note not saved", Toast.LENGTH_SHORT).show()
                    finish()
                }.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }.show()
        }

        binding.deleteButton.setOnClickListener {
            AlertDialog.Builder(this).setTitle("Delete note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Yes") { _, _ ->
                    deleteButtonOnClickListener()
                    Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
                }.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }.show()
        }

    }

    private fun deleteButtonOnClickListener() {
        Log.d("AddNoteActivity", "delete button clicked")

        val noteId = intent.getStringExtra("noteId")!!
        deleteNoteFromFirebase(noteId)
    }


    private fun saveButtonOnClickListener() {
        Log.d("AddNoteActivity", "save button clicked")
        hideKeyboard()
        Snackbar.make(binding.root, "Saving note...", Snackbar.LENGTH_SHORT).show()
        val noteTitle = noteTitleEt.text.toString()
        val noteDescription = noteDescriptionEt.text.toString()

        // get note data from edit text and make noteData object
        noteData = NoteData(
            id = intent.getStringExtra("noteId")?:"",
            title = noteTitle,
            description = noteDescription,
        )



        if (noteTitle.isNotEmpty() || noteDescription.isNotEmpty()) {
            if (edit) {
                updateNoteInFirebase(noteData)
                finish()
            } else /* new note == true */ {
                saveNoteInFirebase(noteData)
                finish()
            }
        } else {
            Snackbar.make(binding.root, "Nothing to save", Snackbar.LENGTH_SHORT).show()
        }

    }

    private fun showKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(noteTitleEt, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }


    /* Firebase operations */
    private fun saveNoteInFirebase(noteData: NoteData) {
        try {
            // hide keyboard if open
            if (currentFocus != null) {
                hideKeyboard()
            }
            Snackbar.make(binding.root, "Saving note...", Snackbar.LENGTH_SHORT).show()
            dbReference.push().setValue(noteData.toMap()).addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("AddNoteActivity", "Note saved")
                    Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d("AddNoteActivity", "Error while saving note", it.exception)
                    Toast.makeText(this, "Error while saving note...", Toast.LENGTH_SHORT).show()
                }

            }.addOnFailureListener {
                Log.d("AddNoteActivity", "Error while saving note", it)
                Toast.makeText(this, "Error while saving note...", Toast.LENGTH_SHORT).show()
            }
        } catch (DatabaseException: Exception) {
            Log.d("AddNoteActivity", "Error while saving note", DatabaseException)
            Toast.makeText(this, "Error while saving note...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateNoteInFirebase(noteData: NoteData) {
        // update note in firebase
        try {
            if (currentFocus != null) {
                hideKeyboard()
            }
            dbReference.child(noteData.id).updateChildren(noteData.toMap()).addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("AddNoteActivity", "Note updated, note id = ${noteData.id}")
                    Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d(
                        "AddNoteActivity",
                        "Error while updating note [note id ${noteData.id}]",
                        it.exception
                    )
                    Toast.makeText(
                        this, "Error while updating note", Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener {
                Log.d("AddNoteActivity", "Error while updating note [note id ${noteData.id}]", it)
                Toast.makeText(
                    this, "Error while updating note", Toast.LENGTH_SHORT
                ).show()
            }
        } catch (DatabaseException: Exception) {
            Log.d("AddNoteActivity", "Error while updating note [note id ${noteData.id}]", DatabaseException)
            Toast.makeText(
                this, "Error while updating note", Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Log.d("AddNoteActivity", "Error while updating note [note id ${noteData.id}]", e)
            Toast.makeText(
                this, "Error while updating note", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun deleteNoteFromFirebase(noteId: String) {
        Log.d("AddNoteActivity", "deleting note from firebase: $noteId")
        try {
            dbReference.child(noteId).removeValue().addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("AddNoteActivity", "Note deleted [note id ${noteId}]")
                    Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
                    hideKeyboard()
                    finish()
                } else {
                    Log.d(
                        "AddNoteActivity",
                        "Error while deleting note [note id ${noteId}]",
                        it.exception
                    )
                    Toast.makeText(
                        this, "Error while deleting note", Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener {
                Log.d("AddNoteActivity", "Error while deleting note [note id ${noteId}]", it)
                Toast.makeText(
                    this, "Error while deleting note", Toast.LENGTH_SHORT
                ).show()
            }
        } catch (DatabaseException: Exception) {
            Log.d("AddNoteActivity", "Exception while deleting note [note id ${noteId}]", DatabaseException)
            Toast.makeText(
                this, "Error while deleting note", Toast.LENGTH_SHORT
            ).show()
        }
    }

}
