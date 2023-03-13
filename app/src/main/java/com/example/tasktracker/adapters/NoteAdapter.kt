package com.example.tasktracker.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tasktracker.data.NoteData
import com.example.tasktracker.databinding.NoteListItemBinding

class NoteAdapter(private val noteList: ArrayList<NoteData>) :
    RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private var listener: OnNoteClickListenerInterface? = null

    fun setListener(listener: OnNoteClickListenerInterface) {
        this.listener = listener
    }

    inner class NoteViewHolder(val binding: NoteListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(
            NoteListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        with(holder) {
            with(noteList[position]) {
                binding.noteTitleTv.text = noteList[position].title
                binding.noteDescriptionTv.text = noteList[position].description

                holder.binding.noteCard.setOnClickListener {
                    Log.d("NoteAdapter", "onBindViewHolder: $this")
                    listener?.onNoteClickListener(this, position)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return noteList.size

    }

    interface OnNoteClickListenerInterface {
        fun onNoteClickListener(noteData: NoteData, position: Int)
    }
}