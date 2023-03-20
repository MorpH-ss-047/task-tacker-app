package com.example.tasktracker.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.example.tasktracker.R
import com.example.tasktracker.data.TaskData
import com.example.tasktracker.databinding.CalendarTaskListItemBinding
import com.example.tasktracker.utils.CalendarUtils

class HomeFragmentTaskAdapter(private val taskList: ArrayList<TaskData>) :
    RecyclerView.Adapter<HomeFragmentTaskAdapter.TaskViewHolder>() {

    private val TAG = "HomeFragmentTaskAdapter"
    private var calendarUtils = CalendarUtils()

    private var listener: OnItemClickListenerInterface? = null

    fun setItemOnClickListener(listener: OnItemClickListenerInterface) {
        this.listener = listener
    }

    interface OnItemClickListenerInterface {
        fun onItemButtonClickListener(taskDataItem: TaskData, position: Int)
    }

    inner class TaskViewHolder(val binding: CalendarTaskListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder(
            CalendarTaskListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {

        holder.binding.apply {
            taskTitleTv.text = taskList[position].title
            taskDescriptionTv.text = taskList[position].description
            priorityTv.text = taskList[position].priority


            priorityTv.background = AppCompatResources.getDrawable(
                priorityTv.context,
                when (taskList[position].priority) {
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

            relativeLayout.background = AppCompatResources.getDrawable(
                relativeLayout.context,
                if(taskList[position].completed) {
                    R.drawable.task_background_completed
                } else {
                    R.drawable.task_background_not_completed
                }
            )

            val date = taskList[position].endDate?.split("-")

//            Log.d(TAG, "$position  ${taskList[position].endDate?: "null"}: ${date?: "null"}")
            taskEndDateTv.text = buildString {
                append(calendarUtils.monthMap[date?.get(1)?.toInt()]?.slice(0..2))
                append(" ")
                append(date?.get(0))
            }

            taskCard.setOnClickListener {
                listener?.onItemButtonClickListener(taskList[position], position)
            }

        }
    }

    override fun getItemCount(): Int {
        return taskList.size

    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateTaskList(newTaskList: ArrayList<TaskData>) {
        this.taskList.clear()
        this.taskList.addAll(newTaskList)
        notifyDataSetChanged()
    }
}