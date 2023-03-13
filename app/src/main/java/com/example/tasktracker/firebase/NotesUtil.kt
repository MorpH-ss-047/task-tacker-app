package com.example.tasktracker.firebase

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.tasktracker.adapters.NoteAdapter
import com.example.tasktracker.data.NoteData
import com.example.tasktracker.databinding.ActivityAddNoteBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener


class NotesUtil(
    private val dbRef: DatabaseReference,
    private val binding: ActivityAddNoteBinding,
) : ViewModel() {
    var notes = ArrayList<NoteData>()
    val TAG = "NotesUtil"


    fun saveNote(noteData: NoteData) {
        // save note in firebase
        dbRef.push().setValue(noteData)
            .addOnCompleteListener{
                if (it.isSuccessful) {
                    Snackbar.make(binding.root, "Note saved", Toast.LENGTH_SHORT).show()

                } else {
                    Log.d("AddNoteActivity", "Error while saving note", it.exception)
                    Snackbar.make(binding.root, "Error while saving note...", Snackbar.LENGTH_SHORT).show()
                }
            }
    }

    fun updateNote(noteData: NoteData) {
        dbRef.child(noteData.id).updateChildren(noteData.toMap())
            .addOnCompleteListener{
                if (it.isSuccessful) {
                    Log.d("AddNoteActivity", "Note updated")
                    Snackbar.make(binding.root, "Note updated", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d("AddNoteActivity", "Error while updating note", it.exception)
                    Snackbar.make(binding.root, "Error while updating note...", Snackbar.LENGTH_SHORT).show()
                }
            }
    }

    fun deleteNote(noteData: NoteData, position: Int) {
        dbRef.child(noteData.id).removeValue()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "Note deleted")
                    Snackbar.make(binding.root, "Note deleted", Snackbar.LENGTH_SHORT).show()
                    notes.removeAt(position)
                } else {
                    Log.d(TAG, "Error while deleting note", it.exception)
                    Snackbar.make(binding.root, "Error while deleting note...", Snackbar.LENGTH_SHORT).show()
                }
            }
    }

    fun fetchNotes() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    notes.clear()
                    for (noteSnapshot in snapshot.children) {
                        val note = noteSnapshot.getValue(NoteData::class.java)
                        notes.add(note!!)
                    }
//                    binding.notesRv.adapter = NoteAdapter(notes)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "Error while fetching notes", error.toException())
                Snackbar.make(binding.root, "Error while fetching notes...", Snackbar.LENGTH_SHORT).show()
            }
        })
    }

}