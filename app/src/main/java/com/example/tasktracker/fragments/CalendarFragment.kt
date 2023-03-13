package com.example.tasktracker.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tasktracker.*
import com.example.tasktracker.adapters.CalendarFragmentTaskAdapter
import com.example.tasktracker.data.TaskData
import com.example.tasktracker.databinding.FragmentCalendarBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*


class CalendarFragment : Fragment(), CalendarFragmentTaskAdapter.OnItemClickListenerInterface,
    CalendarFragmentTaskAdapter.ChangeTaskStatusInterface {


    private var TAG = "CalendarFragment"

    private var currentYear: Int = 2023
    private var currentMonth: Int = 0
    private var currentDay: Int = 1
    private lateinit var calendar: Calendar
    private lateinit var currentDate: String
    private lateinit var selectedDate: String

    private lateinit var auth: FirebaseAuth
    private lateinit var authId: String
    private lateinit var dbReference: DatabaseReference

    private lateinit var taskList: ArrayList<TaskData>
    private lateinit var taskListCopy: ArrayList<TaskData>
    private lateinit var completedTaskList: ArrayList<TaskData>
    private lateinit var filteredCompletedTaskList: ArrayList<TaskData>
    private lateinit var pendingTaskList: ArrayList<TaskData>
    private lateinit var filteredPendingTaskList: ArrayList<TaskData>

    private lateinit var binding: FragmentCalendarBinding
    private lateinit var calendarFragmentTaskAdapter: CalendarFragmentTaskAdapter
    private lateinit var taskListCalendarScreenRv: RecyclerView
    private lateinit var calendarView: CalendarView

    private lateinit var utils: Utils

    private lateinit var completedButtonTv: TextView
    private lateinit var pendingButtonTv: TextView
    private lateinit var allTasksButtonTv: TextView

    private lateinit var progressBar: ProgressBar


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        getTaskFromFirebase()
        updateCompletedTaskList()
        updatePendingTaskList()
        registerEvents()
    }

    private fun init() {

        progressBar = binding.progressBar
        completedButtonTv = binding.taskStatusButtonGroup.completedButtonTv
        pendingButtonTv = binding.taskStatusButtonGroup.pendingButtonTv
        allTasksButtonTv = binding.taskStatusButtonGroup.allTasksButtonTv
        utils = Utils()
        calendarView = binding.calendarView
        taskListCalendarScreenRv = binding.taskListCalendarScreenRv

        calendar = Calendar.getInstance()
        currentYear = calendar.get(Calendar.YEAR)
        currentMonth = calendar.get(Calendar.MONTH)
        currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        currentDate = "$currentDay ${utils.monthMap[currentMonth]} $currentYear"
        selectedDate = currentDate
        auth = FirebaseAuth.getInstance()
        authId = auth.currentUser!!.uid
        dbReference =
            FirebaseDatabase.getInstance().reference.child("users").child(authId).child("tasks")


        taskList = ArrayList()
        taskListCopy = ArrayList()
        completedTaskList = ArrayList()
        pendingTaskList = ArrayList()
        filteredPendingTaskList = ArrayList()
        filteredCompletedTaskList = ArrayList()

        calendarFragmentTaskAdapter = CalendarFragmentTaskAdapter(pendingTaskList)
        taskListCalendarScreenRv.layoutManager = LinearLayoutManager(context)
        taskListCalendarScreenRv.adapter = calendarFragmentTaskAdapter
        calendarFragmentTaskAdapter.setItemOnClickListener(this)
        calendarFragmentTaskAdapter.setChangeTaskStatusListener(this)

    }

    private fun updateCompletedTaskList() {
        completedTaskList = taskListCopy.filter { it.completed } as ArrayList<TaskData>
        Log.d(TAG, "CompletedTaskList size: ${completedTaskList.size}")
    }

    private fun updateCompletedTaskList(date: String) {
        updateCompletedTaskList()
        filteredCompletedTaskList = completedTaskList.filter {
            it.endDate?.split(",")?.get(0) == date
        } as ArrayList<TaskData>
        Log.d(TAG, "filteredCompletedTaskList size: ${filteredCompletedTaskList.size}")

    }

    private fun updatePendingTaskList() {
        pendingTaskList = taskListCopy.filter { !it.completed } as ArrayList<TaskData>
        Log.d(TAG, "PendingTaskList size: ${pendingTaskList.size}")

    }

    private fun updatePendingTaskList(date: String) {
        updatePendingTaskList()
        filteredPendingTaskList =
            pendingTaskList.filter { it.endDate?.split(",")?.get(0) == date } as ArrayList<TaskData>
        Log.d(TAG, "filteredCompletedTaskList size: ${filteredCompletedTaskList.size}")

    }

    @SuppressLint("SetTextI18n")
    private fun registerEvents() {

        allTasksButtonTv.isSelected = true
        setTvSelected(allTasksButtonTv)
        updatePendingTaskList(selectedDate)
        updateCompletedTaskList(selectedDate)
        calendarFragmentTaskAdapter.updateList(taskListCopy.filter {
            it.endDate?.split(",")?.get(0) == selectedDate
        } as ArrayList<TaskData>)
        if (filteredPendingTaskList.isEmpty() && filteredCompletedTaskList.isEmpty() && !progressBar.isVisible) {
            binding.noTaskTv.visibility = View.VISIBLE
            binding.noTaskTv.text = "No tasks for $selectedDate"
        } else {
            binding.noTaskTv.visibility = View.GONE
        }


        completedButtonTv.setOnClickListener {
            completedButtonTvClickListener(currentDate)
        }

        pendingButtonTv.setOnClickListener {
            pendingButtonTvClickListener(currentDate)
        }


        allTasksButtonTv.setOnClickListener {
            setTvSelected(allTasksButtonTv)
            (taskListCopy.filter {
                it.endDate?.split(",")?.get(0) == selectedDate
            } as ArrayList<TaskData>).let {
                calendarFragmentTaskAdapter.updateList(it)
                if (it.isEmpty() && !progressBar.isVisible) {
                    binding.noTaskTv.visibility = View.VISIBLE
                    binding.noTaskTv.text = "No tasks for $selectedDate"
                } else {
                    binding.noTaskTv.visibility = View.GONE
                }
            }
        }
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            Log.d("CalendarFragment", "onViewCreated: $year, $month, $dayOfMonth")
            // date format used: 4 March 2023, Saturday
            selectedDate = "$dayOfMonth ${utils.monthMap[month]} $year"

            Log.d("CalendarFragment", "selectedDate: $selectedDate")

            if (pendingButtonTv.isSelected) {
                pendingButtonTvClickListener(selectedDate)
            } else {
                completedButtonTvClickListener(selectedDate)
            }

            completedButtonTv.setOnClickListener {
                completedButtonTvClickListener(selectedDate)
            }

            pendingButtonTv.setOnClickListener {
                pendingButtonTvClickListener(selectedDate)
            }
        }

        val swipeGesture = object : SwipeGesture(requireContext()) {

            override fun getSwipeDirs(
                recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder
            ): Int {
                if (!pendingButtonTv.isSelected) return 0
                return super.getSwipeDirs(recyclerView, viewHolder)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                if (pendingButtonTv.isSelected) {
                    when (direction) {
                        ItemTouchHelper.LEFT -> {
                            Log.d("CalendarFragment", "onSwiped: left swipe")
                            val position = viewHolder.absoluteAdapterPosition
                            changeTaskStatusToCompleted(
                                taskDataItem = pendingTaskList[position],
                                position = position,
                                undo = false
                            )

                            Snackbar.make(binding.root, "Task completed", Snackbar.LENGTH_SHORT)
//                                .setAction("Undo") {
//                                    Log.d("CalendarFragment", "onSwiped: undo")
//                                    changeTaskStatusToPending(
//                                        taskDataItem = pendingTaskList[position],
//                                        position = position,
//                                        undo = true
//                                    )
//                                }
//                                .setActionTextColor(Color.RED)
                                .show()
                        }
                    }
                } else {
                    // disable swipe gesture for completed task list
                    viewHolder.itemView.animate().translationX(0f).duration = 300
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeGesture)
        itemTouchHelper.attachToRecyclerView(taskListCalendarScreenRv)
    }

    private fun setTvSelected(tv: TextView) {
        allTasksButtonTv.isSelected = false
        pendingButtonTv.isSelected = false
        completedButtonTv.isSelected = false
        tv.isSelected = true
    }

    @SuppressLint("SetTextI18n")
    private fun completedButtonTvClickListener(date: String) {

        setTvSelected(completedButtonTv)
        updateCompletedTaskList(date)

        if (filteredCompletedTaskList.isEmpty() && !progressBar.isVisible) {
            binding.noTaskTv.visibility = View.VISIBLE
            binding.noTaskTv.text = "No tasks for $date"
        } else {
            Log.d("CalendarFragment", "completedTaskListSize: ${completedTaskList.size}")
            binding.noTaskTv.visibility = View.GONE
        }
        calendarFragmentTaskAdapter.updateList(filteredCompletedTaskList)
    }

    @SuppressLint("SetTextI18n")
    private fun pendingButtonTvClickListener(date: String) {

        setTvSelected(pendingButtonTv)

        filteredPendingTaskList = pendingTaskList.filter {
            date == it.endDate?.split(",")?.get(0)
        } as ArrayList<TaskData>

        if (filteredPendingTaskList.isEmpty() && !progressBar.isVisible) {
            binding.noTaskTv.visibility = View.VISIBLE
            binding.noTaskTv.textSize = 20f
            binding.noTaskTv.text = "No tasks for $date"
        } else {
            binding.noTaskTv.visibility = View.GONE
        }
        calendarFragmentTaskAdapter.updateList(filteredPendingTaskList)
    }

    private fun getTaskFromFirebase() {
        dbReference.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                progressBar.visibility = View.VISIBLE

                taskList.clear()
                for (taskSnapshot in snapshot.children) {
                    val task = taskSnapshot.key?.let {
                        val id = it
                        val title = taskSnapshot.child("title").value ?: ""
                        val description = taskSnapshot.child("description").value.toString()
                        val startDate = taskSnapshot.child("startDate").value.toString()
                        val endDate = taskSnapshot.child("endDate").value.toString()
                        val priority = taskSnapshot.child("priority").value.toString()
                        val completed = taskSnapshot.child("completed").value.toString()

                        TaskData(
                            id = id,
                            title = title.toString(),
                            description = description,
                            startDate = startDate,
                            endDate = endDate,
                            priority = priority,
                            completed = completed.toBoolean()
                        )
                    }
                    Log.d(TAG, "onDataChange: ${task?.id}")

                    if (task != null) {
                        taskList.add(task)
                    }

                }

                if (taskList.isNotEmpty()) {
                    taskListCopy = taskList.map { it.copy() } as ArrayList<TaskData>

                    updateCompletedTaskList(selectedDate)
                    updatePendingTaskList(selectedDate)

                    if (pendingButtonTv.isSelected) {
                        calendarFragmentTaskAdapter.updateList(filteredPendingTaskList)
                        if (filteredPendingTaskList.isEmpty()) {
                            binding.noTaskTv.visibility = View.VISIBLE
                            binding.noTaskTv.text = "No pending tasks for $selectedDate..."
                        } else {
                            binding.noTaskTv.visibility = View.GONE
                        }
                    } else if (completedButtonTv.isSelected) {
                        calendarFragmentTaskAdapter.updateList(filteredCompletedTaskList)
                        if (filteredCompletedTaskList.isEmpty()) {
                            binding.noTaskTv.visibility = View.VISIBLE
                            binding.noTaskTv.text = "No completed tasks for $selectedDate..."
                        } else {
                            binding.noTaskTv.visibility = View.GONE
                        }
                    } else {
                        taskList.filter { it.endDate?.split(",")?.get(0) == selectedDate }.let {
                                calendarFragmentTaskAdapter.updateList(it as ArrayList<TaskData>)
                                if (taskList.isEmpty()) {
                                    binding.noTaskTv.visibility = View.VISIBLE
                                    binding.noTaskTv.text = "No Tasks for $selectedDate..."
                                } else {
                                    binding.noTaskTv.visibility = View.GONE
                                }
                            }

                    }
                    progressBar.visibility = View.GONE
                } else { /* if taskList is empty */
                    binding.noTaskTv.visibility = View.VISIBLE
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "onCancelled: ${error.message}")
                Snackbar.make(binding.root, error.toString(), Snackbar.LENGTH_SHORT).show()

            }


        })
    }

    override fun onListItemClickListener(taskDataItem: TaskData, position: Int) {
        val intent = Intent(context, AddTaskActivity::class.java)

        intent.putExtra("newTask", false)
        intent.putExtra("taskTitle", taskDataItem.title)
        intent.putExtra("taskDescription", taskDataItem.description)
        intent.putExtra("taskStartDate", taskDataItem.startDate)
        intent.putExtra("taskEndDate", taskDataItem.endDate)
        intent.putExtra("taskPriority", taskDataItem.priority)
        intent.putExtra("taskCompleted", taskDataItem.completed)
        intent.putExtra("taskId", taskDataItem.id)

        startActivity(intent)

    }

    override fun changeTaskStatusToCompleted(taskDataItem: TaskData, position: Int, undo: Boolean) {
//        if (undo){
//            taskDataItem.completed = true
//            dbReference.child(taskDataItem.id).updateChildren(taskDataItem.toMap())
//            completedTaskList.removeLast()
//            pendingTaskList.add(position, taskDataItem)
//            calendarFragmentTaskAdapter.notifyItemInserted(position)
//        } else {
        Log.d(TAG, "changeTaskStatusToCompleted, : position-> $position ${taskDataItem.id} ")
        taskDataItem.completed = true
        dbReference.child(taskDataItem.id).updateChildren(taskDataItem.toMap())
        completedTaskList.add(filteredPendingTaskList[position])
        filteredPendingTaskList.removeAt(position)
        pendingTaskList.remove(taskDataItem)
        calendarFragmentTaskAdapter.notifyItemRemoved(position)
//        }
    }

}