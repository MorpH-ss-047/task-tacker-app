package com.example.tasktracker.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tasktracker.AddTaskActivity
import com.example.tasktracker.SwipeGesture
import com.example.tasktracker.Utils
import com.example.tasktracker.adapters.HomeFragmentTaskAdapter
import com.example.tasktracker.data.TaskData
import com.example.tasktracker.databinding.FragmentHomeBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment(), HomeFragmentTaskAdapter.OnItemClickListenerInterface {


    private var TAG = "HomeFragment"
    private lateinit var currentDate: LocalDate
    private lateinit var selectedDate: LocalDate
    private lateinit var utils: Utils

    private lateinit var binding: FragmentHomeBinding
    private lateinit var homeFragmentTaskAdapter: HomeFragmentTaskAdapter
    private lateinit var taskList: ArrayList<TaskData>
    private lateinit var taskListCopy: ArrayList<TaskData>
    private lateinit var completedTaskList: ArrayList<TaskData>
    private lateinit var pendingTaskList: ArrayList<TaskData>
    private lateinit var filteredCompletedTaskList: ArrayList<TaskData>
    private lateinit var filteredPendingTaskList: ArrayList<TaskData>

    private lateinit var taskListHomeScreenRv: RecyclerView

    private lateinit var auth: FirebaseAuth
    private lateinit var authId: String
    private lateinit var dbReference: DatabaseReference

    private lateinit var calendar: Calendar
    private lateinit var onlyDateFormatter: DateFormat
    private lateinit var dateFormatter: DateTimeFormatter


    private lateinit var minusOneDayButton: MaterialButton
    private lateinit var minusTwoDaysButton: MaterialButton
    private lateinit var minusThreeDaysButton: MaterialButton
    private lateinit var todayButton: MaterialButton
    private lateinit var plusOneDayButton: MaterialButton
    private lateinit var plusTwoDaysButton: MaterialButton
    private lateinit var plusThreeDaysButton: MaterialButton
    private lateinit var showCalendarButton: ImageView
    private lateinit var completedButtonTv: Button
    private lateinit var pendingButtonTv: Button
    private lateinit var allTasksButtonTv: Button
    private lateinit var selectedButton: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var calendarView: CalendarView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        init()
        getTaskFromFirebase()
        registerEvents()

        todayButton.isSelected = true
        setTvSelected(allTasksButtonTv)

    }

    private fun init() {

//        val date0 = "01 June 2021"
//        val date1 = "01 June 2023"
//        val date2 = "01 June 2023"
//
//        val localDate0 = LocalDate.parse(date0, DateTimeFormatter.ofPattern("dd MMMM yyyy"))
//        val localDate1 = LocalDate.parse(date1, DateTimeFormatter.ofPattern("dd MMMM yyyy"))
//        val localDate2 = LocalDate.parse(date2, DateTimeFormatter.ofPattern("dd MMMM yyyy"))
//
//        if ((localDate1.isAfter(localDate0) && localDate1.isBefore(localDate2)) || localDate1.isEqual(localDate0) || localDate1.isEqual(localDate2)) {
//            Log.d(TAG, "Date is in range")
//            Log.d(TAG, localDate1.toString())
//        } else {
//            Log.d(TAG, "Date not in range")
//        }
//
//        val lDate = LocalDate.now().minusDays(6).format(DateTimeFormatter.ofPattern("dd"))
//        Log.d(TAG, localDate0.format(DateTimeFormatter.ofPattern("dd MM yyyy")).toString() + ", " + localDate0.dayOfWeek.toString())
//        Log.d(TAG, lDate.value.toString())


        utils = Utils()
        taskListHomeScreenRv = binding.taskListHomeScreenRv
        minusOneDayButton = binding.minusOneDay
        minusTwoDaysButton = binding.minusTwoDays
        minusThreeDaysButton = binding.minusThreeDays
        todayButton = binding.todayButton
        plusOneDayButton = binding.plusOneDay
        plusTwoDaysButton = binding.plusTwoDays
        plusThreeDaysButton = binding.plusThreeDays

        completedButtonTv = binding.taskStatusButtonGroup.completedButtonTv
        pendingButtonTv = binding.taskStatusButtonGroup.pendingButtonTv
        allTasksButtonTv = binding.taskStatusButtonGroup.allTasksButtonTv
        progressBar = binding.progressBar
        showCalendarButton = binding.showCalendarButton
        calendarView = binding.calendarView


        auth = FirebaseAuth.getInstance()
        authId = auth.currentUser!!.uid
        dbReference = FirebaseDatabase.getInstance().reference.child("users")
            .child(authId).child("tasks")

        calendar = Calendar.getInstance()
        dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        currentDate = LocalDate.now()
        selectedDate = currentDate
        onlyDateFormatter = SimpleDateFormat.getPatternInstance("dd")


        setDates()

        taskList = ArrayList()
        taskListCopy = ArrayList()
        completedTaskList = ArrayList()
        pendingTaskList = ArrayList()
        filteredCompletedTaskList = ArrayList()
        filteredPendingTaskList = ArrayList()

        selectedButton = todayButton

        homeFragmentTaskAdapter = HomeFragmentTaskAdapter(taskList)
        taskListHomeScreenRv.adapter = homeFragmentTaskAdapter
        taskListHomeScreenRv.layoutManager = LinearLayoutManager(context)

        homeFragmentTaskAdapter.setItemOnClickListener(this)


    }

    private fun updateCompletedTaskList() {
        completedTaskList = taskListCopy.filter { it.completed } as ArrayList<TaskData>
        Log.d(TAG, "CompletedTaskList size: ${completedTaskList.size}")
    }

    private fun updateCompletedTaskList(selectedDate: LocalDate) {
        updateCompletedTaskList()
        filteredCompletedTaskList = completedTaskList.filter {
            filterTaskItem(it, selectedDate)
        } as ArrayList<TaskData>
        Log.d(TAG, "filteredCompletedTaskList size: ${filteredCompletedTaskList.size}")

    }

    private fun updatePendingTaskList() {
        pendingTaskList = taskListCopy.filter { !it.completed } as ArrayList<TaskData>
        Log.d(TAG, "PendingTaskList size: ${pendingTaskList.size}")

    }

    private fun updatePendingTaskList(selectedDate: LocalDate) {
        updatePendingTaskList()
        filteredPendingTaskList = pendingTaskList.filter {
            filterTaskItem(it, selectedDate)
        } as ArrayList<TaskData>

        Log.d(TAG, "filteredPendingTaskList size: ${filteredPendingTaskList.size}")

    }

    @SuppressLint("SetTextI18n")
    private fun registerEvents() {


       calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)

            when(selectedDate.compareTo(currentDate)){
                0 -> {
                    setButtonSelected(todayButton)
                }
                -1 -> {
                    setButtonSelected(minusOneDayButton)
                }
                -2 -> {
                    setButtonSelected(minusTwoDaysButton)
                }
                -3 -> {
                    setButtonSelected(minusThreeDaysButton)
                }
                1 -> {
                    setButtonSelected(plusOneDayButton)
                }
                2 -> {
                    setButtonSelected(plusTwoDaysButton)
                }
                3 -> {
                    setButtonSelected(plusThreeDaysButton)
                }

                else -> {
                    removeButtonSelected()
                }
            }

            Log.d(TAG, "Selected date: $selectedDate")
            updateCompletedTaskList(selectedDate)
            updatePendingTaskList(selectedDate)
            if (allTasksButtonTv.isSelected) {
                (taskListCopy.filter {
                    filterTaskItem(it, selectedDate)
                } as ArrayList<TaskData>).let {
                    if(it.isEmpty() && !progressBar.isVisible) {
                        binding.noTaskTv.visibility = View.VISIBLE
                        binding.noTaskTv.text = "No tasks for $selectedDate"
                    }
                    else binding.noTaskTv.visibility = View.GONE
                    homeFragmentTaskAdapter.updateTaskList(it)
                }
            } else if (completedButtonTv.isSelected) {
                homeFragmentTaskAdapter.updateTaskList(filteredCompletedTaskList)
            } else if (pendingButtonTv.isSelected) {
                homeFragmentTaskAdapter.updateTaskList(filteredPendingTaskList)
            }
        }

        minusOneDayButton.setOnClickListener {
            dateButtonClickListener(it as MaterialButton)
        }
        minusTwoDaysButton.setOnClickListener {
            dateButtonClickListener(it as MaterialButton)
        }
        minusThreeDaysButton.setOnClickListener {
            dateButtonClickListener(it as MaterialButton)
        }
        todayButton.setOnClickListener {
            dateButtonClickListener(it as MaterialButton)
        }
        plusOneDayButton.setOnClickListener {
            dateButtonClickListener(it as MaterialButton)
        }
        plusTwoDaysButton.setOnClickListener {
            dateButtonClickListener(it as MaterialButton)
        }
        plusThreeDaysButton.setOnClickListener {
            dateButtonClickListener(it as MaterialButton)
        }

        completedButtonTv.setOnClickListener {
            setTvSelected(it as Button)
            Log.d(TAG, "Completed button clicked: $selectedDate")
            updateCompletedTaskList(selectedDate)
            if (filteredCompletedTaskList.isEmpty() && !progressBar.isVisible) {
                binding.noTaskTv.visibility = View.VISIBLE
                binding.noTaskTv.text = "No completed tasks for $selectedDate"
            } else {
                binding.noTaskTv.visibility = View.GONE
            }

            homeFragmentTaskAdapter.updateTaskList(filteredCompletedTaskList)
        }
        pendingButtonTv.setOnClickListener {
            Log.d(TAG, "Completed button clicked: $selectedDate")
            setTvSelected(it as Button)
            updatePendingTaskList(selectedDate)

            if (filteredPendingTaskList.isEmpty() && !progressBar.isVisible) {
                binding.noTaskTv.visibility = View.VISIBLE
                binding.noTaskTv.text = "No pending tasks for $selectedDate"
            } else {
                binding.noTaskTv.visibility = View.GONE
            }

            homeFragmentTaskAdapter.updateTaskList(filteredPendingTaskList)
        }
        allTasksButtonTv.setOnClickListener { btn ->
            Log.d(TAG, "Completed button clicked: $selectedDate")
            setTvSelected(btn as Button)

            (taskListCopy.filter { taskItem ->
                filterTaskItem(taskItem, selectedDate)
            } as ArrayList<TaskData>).let {
                if(it.isEmpty() && !progressBar.isVisible) {
                    binding.noTaskTv.visibility = View.VISIBLE
                    binding.noTaskTv.text = "No tasks for $selectedDate"
                }
                else binding.noTaskTv.visibility = View.GONE
                homeFragmentTaskAdapter.updateTaskList(it)
            }
        }

        val swipeGesture = object : SwipeGesture(requireContext()) {

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
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
        itemTouchHelper.attachToRecyclerView(taskListHomeScreenRv)


    }

    @SuppressLint("SetTextI18n")
    private fun dateButtonClickListener(button: MaterialButton) {
        setButtonSelected(button)
        selectedDate = LocalDate.now().minusDays(selectedButton.tag.toString().toLong())
        Log.d(TAG, "dateButtonClickListener: selectedDate = $selectedDate")
        updatePendingTaskList(selectedDate)
        updateCompletedTaskList(selectedDate)

        if (allTasksButtonTv.isSelected) {
            (taskListCopy.filter {
                filterTaskItem(it, selectedDate)

            } as ArrayList<TaskData>).let {
                if(it.isEmpty() && !progressBar.isVisible) {
                    binding.noTaskTv.visibility = View.VISIBLE
                    binding.noTaskTv.text = "No tasks for $selectedDate"
                }
                else binding.noTaskTv.visibility = View.GONE
                homeFragmentTaskAdapter.updateTaskList(it)
            }

        } else if (pendingButtonTv.isSelected) {
            if(filteredPendingTaskList.isEmpty() && !progressBar.isVisible) {
                binding.noTaskTv.visibility = View.VISIBLE
                binding.noTaskTv.text = "No pending tasks for $selectedDate"
            }
            else binding.noTaskTv.visibility = View.GONE
            homeFragmentTaskAdapter.updateTaskList(filteredPendingTaskList)

        } else if (completedButtonTv.isSelected) {
            if(filteredCompletedTaskList.isEmpty() && !progressBar.isVisible){
                binding.noTaskTv.visibility = View.VISIBLE
                binding.noTaskTv.text = "No completed tasks for $selectedDate"
            }
            else binding.noTaskTv.visibility = View.GONE
            homeFragmentTaskAdapter.updateTaskList(filteredCompletedTaskList)
        }
    }

    private fun filterTaskItem(taskData: TaskData, selectedDate: LocalDate): Boolean {
        val startDate = LocalDate.parse(taskData.startDate?.split(",")?.get(0), dateFormatter)
        val endDate = LocalDate.parse(taskData.endDate?.split(",")?.get(0), dateFormatter)
        return (startDate.isBefore(selectedDate) && endDate.isAfter(selectedDate)) || startDate.isEqual(selectedDate)  || endDate.isEqual(selectedDate)
    }

    private fun setTvSelected(tv: Button) {
        allTasksButtonTv.isSelected = false
        pendingButtonTv.isSelected = false
        completedButtonTv.isSelected = false
        tv.isSelected = true
    }

    private fun removeButtonSelected() {
        minusOneDayButton.isSelected = false
        minusTwoDaysButton.isSelected = false
        minusThreeDaysButton.isSelected = false
        todayButton.isSelected = false
        plusOneDayButton.isSelected = false
        plusTwoDaysButton.isSelected = false
        plusThreeDaysButton.isSelected = false
    }

    private fun setButtonSelected(button: MaterialButton) {
        minusOneDayButton.isSelected = false
        minusTwoDaysButton.isSelected = false
        minusThreeDaysButton.isSelected = false
        todayButton.isSelected = false
        plusOneDayButton.isSelected = false
        plusTwoDaysButton.isSelected = false
        plusThreeDaysButton.isSelected = false

        button.isSelected = true
        selectedButton = button
    }

    private fun setDates() {

        val minusOneDay = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -1)
        }.time

        val minusTwoDays = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -2)
        }.time

        val minusThreeDays = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -3)
        }.time

        val plusOneDay = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }.time

        val plusTwoDays = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 2)
        }.time

        val plusThreeDays = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 3)
        }.time

        minusOneDayButton.text = onlyDateFormatter.format(minusOneDay)
        minusOneDayButton.tag = -1
        minusTwoDaysButton.text = onlyDateFormatter.format(minusTwoDays)
        minusTwoDaysButton.tag = -2
        minusThreeDaysButton.text = onlyDateFormatter.format(minusThreeDays)
        minusThreeDaysButton.tag = -3
        plusOneDayButton.text = onlyDateFormatter.format(plusOneDay)
        plusOneDayButton.tag = 1
        plusTwoDaysButton.text = onlyDateFormatter.format(plusTwoDays)
        plusTwoDaysButton.tag = 2
        plusThreeDaysButton.text = onlyDateFormatter.format(plusThreeDays)
        plusThreeDaysButton.tag = 3
        todayButton.tag = 0


    }

    private fun getTaskFromFirebase() {
        dbReference.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                progressBar.visibility = View.VISIBLE

                taskList.clear()
                for (taskSnapshot in snapshot.children) {
                    val task =
                        taskSnapshot.key?.let {
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

                    if (task != null) {
                        taskList.add(task)
                    }

                }

                homeFragmentTaskAdapter.notifyItemRangeChanged(0, taskList.size)
                progressBar.visibility = View.GONE
                Log.d(TAG, "onDataChange: progress bar gone")
                if (taskList.isEmpty()) {
                    binding.noTaskTv.visibility = View.VISIBLE
                } else {
                    binding.noTaskTv.visibility = View.GONE
                    minusOneDayButton.visibility = View.VISIBLE
                    minusTwoDaysButton.visibility = View.VISIBLE
                    minusThreeDaysButton.visibility = View.VISIBLE
                    todayButton.visibility = View.VISIBLE
                    plusOneDayButton.visibility = View.VISIBLE
                    plusTwoDaysButton.visibility = View.VISIBLE
                    plusThreeDaysButton.visibility = View.VISIBLE
                    Log.d(TAG, "onDataChange: selected date =${selectedDate}")
                    taskListCopy = taskList.map { it.copy() } as ArrayList<TaskData>
                    updateCompletedTaskList(selectedDate)
                    updatePendingTaskList(selectedDate)

                    if (allTasksButtonTv.isSelected) {
                        (taskListCopy.filter {
                            filterTaskItem(it, selectedDate)
                        } as ArrayList<TaskData>).let {
                            if(it.isEmpty() && !progressBar.isVisible) {
                                binding.noTaskTv.visibility = View.VISIBLE
                                binding.noTaskTv.text = "No tasks for $selectedDate"
                            }
                            else binding.noTaskTv.visibility = View.GONE
                            homeFragmentTaskAdapter.updateTaskList(it)

                        }
                    } else if (pendingButtonTv.isSelected) {
                        homeFragmentTaskAdapter.updateTaskList(filteredPendingTaskList)
                    } else if (completedButtonTv.isSelected) {
                        homeFragmentTaskAdapter.updateTaskList(filteredCompletedTaskList)
                    }
                }
                Log.d(TAG, "onDataChange: ${taskList.size}")


            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "onCancelled: ${error.message}")
            }

        })
    }

    override fun onItemButtonClickListener(taskDataItem: TaskData, position: Int) {
        val intent = Intent(context, AddTaskActivity::class.java)

        intent.putExtra("newTask", false)
        intent.putExtra("taskId", taskDataItem.id)
        intent.putExtra("taskTitle", taskDataItem.title)
        intent.putExtra("taskDescription", taskDataItem.description)
        intent.putExtra("taskStartDate", taskDataItem.startDate)
        intent.putExtra("taskEndDate", taskDataItem.endDate)
        intent.putExtra("taskPriority", taskDataItem.priority)
        intent.putExtra("taskCompleted", taskDataItem.completed)

        startActivity(intent)

    }

    fun changeTaskStatusToCompleted(taskDataItem: TaskData, position: Int, undo: Boolean) {
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
        homeFragmentTaskAdapter.notifyItemRemoved(position)
//        }
    }





}

