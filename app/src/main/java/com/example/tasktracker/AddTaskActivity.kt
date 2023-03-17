package com.example.tasktracker

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.tasktracker.data.TaskData
import com.example.tasktracker.databinding.ActivityAddTaskBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class AddTaskActivity : AppCompatActivity() {

    private val TAG = "AddTaskActivity"

    private lateinit var binding: ActivityAddTaskBinding
    private var newTask: Boolean = true

    private lateinit var taskTitle: String
    private lateinit var taskDescription: String
    private lateinit var taskStartDateString: String
    private lateinit var taskEndDateString: String
    private lateinit var taskPriority: String

    private lateinit var taskTitleEt: TextInputEditText
    private lateinit var taskDescriptionEt: TextInputEditText
    private lateinit var radioGroup: RadioGroup

    private lateinit var auth: FirebaseAuth
    private lateinit var authId: String
    private lateinit var dbReference: DatabaseReference

    private lateinit var taskData: TaskData
    private lateinit var dateFormatter: DateTimeFormatter

    private lateinit var utils: Utils
    private lateinit var currentDate: LocalDate
    private lateinit var selectedStartDate: LocalDate
    private lateinit var selectedEndDate: LocalDate


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // set toolbar in action bar
        setSupportActionBar(binding.toolbar)

        // set back button in toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        init()
        registerEvents()

    }

    private fun init() {
        taskTitleEt = binding.taskTitleEt
        taskDescriptionEt = binding.taskDescriptionEt
        radioGroup = binding.radioGroup
        dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

        utils = Utils()
        auth = FirebaseAuth.getInstance()
        authId = auth.currentUser!!.uid
            dbReference = FirebaseDatabase.getInstance().reference.child("users")
            .child(authId).child("tasks")


        newTask = intent.getBooleanExtra("newTask", true)
        currentDate = LocalDate.now()
        selectedStartDate = currentDate
        selectedEndDate = currentDate
        taskStartDateString = selectedStartDate.format(dateFormatter)
        taskEndDateString = selectedEndDate.format(dateFormatter)


        if (!newTask) {
            // set title in toolbar, hide save button and calender button
            supportActionBar?.title = getString(R.string.task)
            binding.saveTaskButton.visibility = View.INVISIBLE
            binding.saveTaskButton.isClickable = false
            binding.startCalenderButton.visibility = View.GONE
            binding.endCalenderButton.visibility = View.GONE
            /* Visibility of editButton in toolbar is set in onCreateOptionsMenu(menu:Menu) method */


            // get task data from intent
            taskTitle = intent.getStringExtra("taskTitle") ?: ""
            taskDescription = intent.getStringExtra("taskDescription") ?: ""
            taskStartDateString = intent.getStringExtra("taskStartDate") ?: ""
            selectedStartDate = LocalDate.parse(taskStartDateString, dateFormatter)
            taskEndDateString = intent.getStringExtra("taskEndDate") ?: ""
            selectedEndDate = LocalDate.parse(taskEndDateString, dateFormatter)
            taskPriority = intent.getStringExtra("taskPriority") ?: ""

            // set task data in edit text
            taskTitleEt.setText(taskTitle)
            taskDescriptionEt.setText(taskDescription)
            val taskStartDateSplitted = taskStartDateString.split("-")
            binding.startDateTv.text = buildString {
                append(taskStartDateSplitted[0])
                append(" ")
                append(utils.monthMap[taskStartDateSplitted[1].toInt()])
                append(" ")
                append(taskStartDateSplitted[2])
                append(", ")
                append(utils.dayMap[LocalDate.parse(taskStartDateString, dateFormatter).dayOfWeek.value])
            }
            val taskEndDateSplitted = taskEndDateString.split("-")
            binding.endDateTv.text = buildString {
                append(taskEndDateSplitted[0])
                append(" ")
                append(utils.monthMap[taskEndDateSplitted[1].toInt()])
                append(" ")
                append(taskEndDateSplitted[2])
                append(", ")
                append(utils.dayMap[LocalDate.parse(taskEndDateString, dateFormatter).dayOfWeek.value])
            }
            when (taskPriority) {
                "Low" -> binding.lowPriority.isChecked = true
                "Normal" -> binding.normalPriority.isChecked = true
                "Critical" -> binding.criticalPriority.isChecked = true
            }


            // set text color to black
            taskTitleEt.setTextColor(getColor(R.color.black))
            taskDescriptionEt.setTextColor(getColor(R.color.black))
            binding.startDateTv.setTextColor(getColor(R.color.black))
            binding.endDateTv.setTextColor(getColor(R.color.black))


            // disable edit text
            taskTitleEt.isEnabled = false
            taskDescriptionEt.isEnabled = false
            binding.lowPriority.isEnabled = false
            binding.normalPriority.isEnabled = false
            binding.criticalPriority.isEnabled = false


        } else /* newTask == true */ {
            // set title in toolbar
            supportActionBar?.title = getString(R.string.create_task)

            binding.startDateTv.text = buildString {
                append(currentDate.dayOfMonth)
                append(" ")
                append(utils.monthMap[currentDate.monthValue])
                append(" ")
                append(currentDate.year)
                append(", ")
                append(utils.dayMap[currentDate.dayOfWeek.value])
            }

            binding.endDateTv.text = buildString {
                append(currentDate.dayOfMonth)
                append(" ")
                append(utils.monthMap[currentDate.monthValue])
                append(" ")
                append(currentDate.year)
                append(", ")
                append(utils.dayMap[currentDate.dayOfWeek.value])
            }

            binding.normalPriority.isChecked = true

        }

    }

    private fun registerEvents() {

        binding.startCalenderButton.setOnClickListener {
            // create date picker dialog
            hideKeyboard()

            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                // set date to text view
                binding.startDateTv.text = buildString {
                    append(dayOfMonth)
                    append(" ")
                    append(utils.monthMap[month + 1])
                    append(" ")
                    append(year)
                    append(", ")
                    append(utils.dayMap[LocalDate.of(year, month + 1, dayOfMonth).dayOfWeek.value])
                }
                selectedStartDate = LocalDate.of(year, month + 1, dayOfMonth)
                taskStartDateString = selectedStartDate.format(dateFormatter)
                if (selectedStartDate.isAfter(selectedEndDate)) {
                    binding.endDateTv.setTextColor(getColor(R.color.red))
                    binding.endDateTv.error = "End date must be greater than start date"
                    Snackbar.make(binding.root,"End date must be greater than start date",Snackbar.LENGTH_SHORT).show()
                } else {
                    binding.endDateTv.setTextColor(getColor(R.color.black))
                    binding.endDateTv.error = null
                }
                binding.startDateTv.setTextColor(getColor(R.color.black))
            }, selectedStartDate.year, selectedStartDate.monthValue - 1, selectedStartDate.dayOfMonth).show()
        }
        binding.endCalenderButton.setOnClickListener {
            // create date picker
            hideKeyboard()

            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                // set date to text view
                binding.endDateTv.text = buildString {
                    append(dayOfMonth)
                    append(" ")
                    append(utils.monthMap[month + 1])
                    append(" ")
                    append(year)
                    append(", ")
                    append(utils.dayMap[LocalDate.of(year, month + 1, dayOfMonth).dayOfWeek.value])
                }

                binding.startDateTv.setTextColor(getColor(R.color.black))
                selectedEndDate = LocalDate.of(year, month + 1, dayOfMonth)
                taskEndDateString = selectedEndDate.format(dateFormatter)
                if (selectedStartDate.isAfter(selectedEndDate)
                ) {
                    binding.endDateTv.setTextColor(getColor(R.color.red))
                    binding.endDateTv.error = "End date must be greater than start date"
                    Snackbar.make(binding.root,"End date must be greater than start date",Snackbar.LENGTH_SHORT).show()
                } else {
                    binding.endDateTv.setTextColor(getColor(R.color.black))
                    binding.endDateTv.error = null
                }

            }, selectedEndDate.year, selectedEndDate.monthValue - 1, selectedEndDate.dayOfMonth).show()
        }
        binding.saveTaskButton.setOnClickListener {
            taskTitle = taskTitleEt.text.toString().trim()
            taskDescription = taskDescriptionEt.text.toString().trim()
            when (radioGroup.checkedRadioButtonId) {
                R.id.lowPriority -> taskPriority = "Low"
                R.id.normalPriority -> taskPriority = "Normal"
                R.id.criticalPriority -> taskPriority = "Critical"
            }

            Log.d(TAG, "$taskStartDateString, $taskEndDateString")
            if (taskTitle.isNotEmpty() && taskDescription.isNotEmpty() && (LocalDate.parse(
                    taskStartDateString,
                    dateFormatter
                ).isBefore(LocalDate.parse(taskEndDateString, dateFormatter)) || LocalDate.parse(
                    taskStartDateString,
                    dateFormatter
                ).isEqual(LocalDate.parse(taskEndDateString, dateFormatter)))
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Save Task")
                    .setMessage("Are you sure you want to save this task?")
                    .setPositiveButton("Yes") { _, _ ->
                        if (newTask) {
                            // save task
                            taskData = TaskData(
                                title = taskTitle,
                                description = taskDescription,
                                startDate = taskStartDateString,
                                endDate = taskEndDateString,
                                priority = taskPriority
                            )

                            saveTaskToFirebase(taskData)
                        } else {
                            // edit task
                            if (taskDescription.isNotEmpty()) {

                                taskData = TaskData(
                                    id = intent.getStringExtra("taskId")!!,
                                    title = taskTitleEt.text.toString().trim(),
                                    description = taskDescriptionEt.text.toString().trim(),
                                    startDate = taskStartDateString,
                                    endDate = taskEndDateString,
                                    priority = when (radioGroup.checkedRadioButtonId) {
                                        R.id.lowPriority -> "Low"
                                        R.id.normalPriority -> "Normal"
                                        R.id.criticalPriority -> "Critical"
                                        else -> "Normal"
                                    },
                                    completed = false
                                )

                                updateTaskInFirebase(taskData)

                            } else {
                                taskDescriptionEt.error = "Task description is required"
                                return@setPositiveButton
                            }

                        }
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()

            } else if (taskTitle.isEmpty()) {
                taskTitleEt.error = "Task title is required"
                return@setOnClickListener
            } else if (taskDescription.isEmpty()) {
                taskDescriptionEt.error = "Task description is required"
                return@setOnClickListener
            } else if (taskStartDateString.isEmpty()) {
                Snackbar.make(binding.root, "Task start date is required", Snackbar.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            } else if (taskEndDateString.isEmpty()) {
                Snackbar.make(binding.root, "Task end date is required", Snackbar.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            } else if (LocalDate.parse(taskStartDateString, dateFormatter) > LocalDate.parse(
                    taskEndDateString,
                    dateFormatter
                )
            ) {
                binding.endDateTv.setTextColor(getColor(R.color.red))
                binding.endDateTv.error = "End date must be greater than start date"
                Snackbar.make(
                    binding.root,
                    "Task end date must be greater than start date",
                    Snackbar.LENGTH_SHORT
                )
                    .show()
                return@setOnClickListener
            }


        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPressed()
                finish()
            }
        })

    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)

        menuInflater.inflate(R.menu.toolbar_nav_menu, menu)
        menu?.findItem(R.id.saveButton)?.isVisible = false
        menu?.findItem(R.id.saveButton)?.isEnabled = false

        menu?.findItem(R.id.editButton)?.isVisible = !newTask

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.editButton -> {
                // set title in toolbar, show save button and calender button
                supportActionBar?.title = getString(R.string.edit_task)
                binding.startCalenderButton.visibility = View.VISIBLE
                binding.endCalenderButton.visibility = View.VISIBLE
                binding.saveTaskButton.text = getString(R.string.edit_task)
                binding.saveTaskButton.visibility = View.VISIBLE
                binding.saveTaskButton.isClickable = true

                // enable edit text
                taskTitleEt.isEnabled = true
                taskDescriptionEt.isEnabled = true
                binding.lowPriority.isEnabled = true
                binding.normalPriority.isEnabled = true
                binding.criticalPriority.isEnabled = true

                // set focus on title edit text
                taskTitleEt.requestFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(taskTitleEt, InputMethodManager.SHOW_IMPLICIT)
                // set cursor to end of text else it will be at the start(if text is null)
                taskTitleEt.setSelection(taskTitleEt.text?.length ?: 0)

                // hide edit button
                item.isVisible = false


            }

            android.R.id.home -> {
                // Confirm if user wants to exit
                handleBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateTaskInFirebase(taskData: TaskData) {
        // update task in firebase
        dbReference.child(taskData.id).updateChildren(taskData.toMap()).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("AddTaskActivity", "Task updated")
                Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Log.d("AddTaskActivity", "Error while updating task", it.exception)
                Toast.makeText(this, "Error while updating task...", Toast.LENGTH_SHORT).show()
            }
        }
            .addOnFailureListener {
                Log.d("AddTaskActivity", "Error while updating note", it)
                Toast.makeText(
                    this, "Error while updating task...", Toast.LENGTH_SHORT
                ).show()
            }

    }

    private fun saveTaskToFirebase(taskData: TaskData) {
        // save task in firebase
        Snackbar.make(binding.root, "Saving task...", Snackbar.LENGTH_SHORT).show()
        dbReference.push().setValue(taskData.toMap())
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Snackbar.make(binding.root, "Task saved", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Log.d("AddTaskActivity", "Error while saving task", it.exception)
                    Snackbar.make(binding.root, "Error while saving task...", Snackbar.LENGTH_SHORT)
                        .show()
                }
            }

    }

    private fun handleBackPressed() {
        if (!newTask) {

            if (taskTitleEt.text.toString() != taskTitle || taskDescriptionEt.text.toString() != taskDescription || binding.startDateTv.text.toString() != taskStartDateString || binding.endDateTv.text.toString() != taskEndDateString || when (radioGroup.checkedRadioButtonId) {
                    R.id.lowPriority -> "Low"
                    R.id.normalPriority -> "Normal"
                    R.id.criticalPriority -> "Critical"
                    else -> "Normal"
                } != taskPriority
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Are you sure you want to exit?")
                    .setMessage("Changes will not be saved")
                    .setPositiveButton("Yes") { _, _ ->
                        Toast.makeText(this, "Changes not saved", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            } else {
                finish()
            }
        } else /* new task == true */ {
            if (taskTitleEt.text.toString().isNotEmpty() || taskDescriptionEt.text.toString()
                    .isNotEmpty()
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Exit")
                    .setMessage("Are you sure you want to exit without saving?")
                    .setPositiveButton("Yes") { _, _ ->
                        Toast.makeText(this, "Task not saved", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            } else /* no changes made by the user */ {
                finish()
            }
        }
    }

}

