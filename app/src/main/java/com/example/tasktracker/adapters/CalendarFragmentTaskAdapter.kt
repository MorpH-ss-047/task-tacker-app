package com.example.tasktracker.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.recyclerview.widget.RecyclerView
import com.example.tasktracker.R
import com.example.tasktracker.data.TaskData
import com.example.tasktracker.databinding.CalendarTaskListItemBinding

class CalendarFragmentTaskAdapter(private val taskList: ArrayList<TaskData>) :
    RecyclerView.Adapter<CalendarFragmentTaskAdapter.TaskViewHolder>() {

    private val TAG = "CalendarFragmentTaskAdapter"
    private var onItemClickListener: OnItemClickListenerInterface? = null
    private var changeTaskStatusListener: ChangeTaskStatusInterface? = null

    fun setItemOnClickListener(listener: OnItemClickListenerInterface) {
        this.onItemClickListener = listener
    }

    fun setChangeTaskStatusListener(listener: ChangeTaskStatusInterface) {
        this.changeTaskStatusListener = listener
    }

    interface OnItemClickListenerInterface {
        fun onListItemClickListener(taskDataItem: TaskData, position: Int)
    }

    interface ChangeTaskStatusInterface {
        fun changeTaskStatusToCompleted(taskDataItem: TaskData, position: Int, undo: Boolean = false)
    }


    inner class TaskViewHolder(val binding: CalendarTaskListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder(
            CalendarTaskListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.binding.apply {
            taskTitleTv.text = taskList[position].title
            taskDescriptionTv.text = taskList[position].description
            priorityTv.text = taskList[position].priority

            priorityTv.background = getDrawable(
                priorityTv.context, when (taskList[position].priority) {
                    "Critical" -> {
                        R.drawable.priority_background_critical
                    }
                    "Normal" -> {
                        R.drawable.priority_background_normal
                    }
                    "Low" -> {
                        R.drawable.priority_background_low
                    }
                    else -> {
                        R.drawable.priority_background_normal
                    }
                }
            )


            val date = taskList[position].endDate?.split(" ")
            // Log.d(TAG, "onBindViewHolder CalendarAdapter: ${date?.get(1)?.slice(0..2)}")
            taskEndDateTv.text = buildString {
                append(date?.get(1)?.slice(0..2))
                append(" ")
                append(date?.get(0) ?: "No date")
            }

            taskCard.setOnClickListener {
                onItemClickListener?.onListItemClickListener(taskList[position], position)
            }
        } // end of holder.binding.apply
    } // end of onBindViewHolder

    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount: ${taskList.size}")
        return taskList.size

    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(filteredTaskList: ArrayList<TaskData>) {
        taskList.clear()
        taskList.addAll(filteredTaskList)

        notifyDataSetChanged()

    }

}