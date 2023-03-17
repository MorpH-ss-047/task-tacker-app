package com.example.tasktracker.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.tasktracker.AddNoteActivity
import com.example.tasktracker.adapters.NoteAdapter
import com.example.tasktracker.data.NoteData
import com.example.tasktracker.databinding.FragmentNoteBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class NoteFragment : Fragment(), NoteAdapter.OnNoteClickListenerInterface {

    private val TAG = "NoteFragment"

    private lateinit var binding: FragmentNoteBinding
    lateinit var noteAdapter: NoteAdapter
    private lateinit var noteList: ArrayList<NoteData>
    private lateinit var noteListRv: RecyclerView


    private lateinit var auth: FirebaseAuth
    private lateinit var authId: String
    private lateinit var dbReference: DatabaseReference


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentNoteBinding.bind(view)
        init()
        getNotesFromFirebase()
    }


    private fun init() {

        auth = FirebaseAuth.getInstance()
        authId = auth.currentUser!!.uid
        dbReference = FirebaseDatabase.getInstance().reference.child("users")
            .child(authId).child("notes")
        noteList = ArrayList()

        noteListRv = binding.noteListRv
        noteListRv.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        noteAdapter = NoteAdapter(noteList)
        noteAdapter.setListener(this)
        noteListRv.adapter = noteAdapter
    }

    private fun getNotesFromFirebase() {
        dbReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val progressBar = binding.progressBar
                progressBar.visibility = View.VISIBLE

                noteList.clear()
                for (taskSnapshot in snapshot.children) {
                    val note =
                        taskSnapshot.key?.let {
                            val id = it
                            val title = taskSnapshot.child("title").value ?: ""
                            val description = taskSnapshot.child("description").value.toString()
                            NoteData(id, title.toString(), description)

                        }

                    if (note != null) {
                        noteList.add(note)
                    }

                }

                noteAdapter.notifyItemRangeChanged(0, noteList.size)
                progressBar.visibility = View.GONE
                if (noteList.isEmpty()) {
                    binding.noTaskTv.visibility = View.VISIBLE
                } else {
                    binding.noTaskTv.visibility = View.GONE
                }
                Log.d(TAG, "onDataChange: $noteList")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "onCancelled: ${error.message}")
                Snackbar.make(binding.root, error.toString(), Snackbar.LENGTH_SHORT).show()

            }


        })
    }

    override fun onNoteClickListener(noteData: NoteData, position: Int) {
        val intent = Intent(requireContext(), AddNoteActivity::class.java)

        intent.putExtra("edit", true)
        intent.putExtra("noteId", noteData.id)
        intent.putExtra("noteTitle", noteData.title)
        intent.putExtra("noteDescription", noteData.description)
        intent.putExtra("position", position)
        startActivity(intent)
        noteAdapter.notifyItemChanged(position)
    }

}