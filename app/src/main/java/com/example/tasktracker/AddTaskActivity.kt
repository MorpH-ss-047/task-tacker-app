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
import java.util.*

class AddTaskActivity : AppCompatActivity() {

    private val calendar: Calendar = Calendar.getInstance()
    private lateinit var binding: ActivityAddTaskBinding
    private var newTask: Boolean = true

    private lateinit var taskTitle: String
    private lateinit var taskDescription: String
    private lateinit var taskStartDate: String
    private lateinit var taskEndDate: String
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
        taskStartDate = LocalDate.now().format(dateFormatter)
        taskEndDate = LocalDate.now().format(dateFormatter)


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
            taskStartDate = intent.getStringExtra("taskStartDate") ?: ""
            taskEndDate = intent.getStringExtra("taskEndDate") ?: ""
            taskPriority = intent.getStringExtra("taskPriority") ?: ""

            // set task data in edit text
            taskTitleEt.setText(taskTitle)
            taskDescriptionEt.setText(taskDescription)
            binding.startDateTv.text = taskStartDate
            binding.endDateTv.text = taskEndDate
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
                append(calendar.get(Calendar.DAY_OF_MONTH).toString())
                append(" ")
                append(utils.monthMap[calendar.get(Calendar.MONTH)])
                append(" ")
                append(calendar.get(Calendar.YEAR))
                append(", ")
                append(utils.dayMap[calendar.get(Calendar.DAY_OF_WEEK)])
            }

            binding.endDateTv.text = buildString {
                append(calendar.get(Calendar.DAY_OF_MONTH).toString())
                append(" ")
                append(utils.monthMap[calendar.get(Calendar.MONTH)])
                append(" ")
                append(calendar.get(Calendar.YEAR))
                append(", ")
                append(utils.dayMap[calendar.get(Calendar.DAY_OF_WEEK)])
            }

            binding.normalPriority.isChecked = true

        }

    }

    private fun registerEvents() {

        binding.startCalenderButton.setOnClickListener {
            // create date picker

            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

            hideKeyboard()

            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                // set date to text view
                calendar.set(year, month, dayOfMonth)
                binding.startDateTv.text = buildString {
                    append(dayOfMonth)
                    append(" ")
                    append(utils.monthMap[month])
                    append(" ")
                    append(year)
                    append(", ")
                    append(utils.dayMap[calendar.get(Calendar.DAY_OF_WEEK)])
                }
                taskStartDate = LocalDate.of(year, month + 1, dayOfMonth).format(dateFormatter)
                binding.startDateTv.setTextColor(getColor(R.color.black))
            }, currentYear, currentMonth, currentDay).show()
        }
        binding.endCalenderButton.setOnClickListener {
            // create date picker
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

            hideKeyboard()

            DatePickerDialog(this, { _, year, month, dayOfMonth ->

                calendar.set(year, month, dayOfMonth)

                // set date to text view
                binding.endDateTv.text = buildString {
                    append(dayOfMonth)
                    append(" ")
                    append(utils.monthMap[month])
                    append(" ")
                    append(year)
                    append(", ")
                    append(utils.dayMap[calendar.get(Calendar.DAY_OF_WEEK)])
                }

                binding.startDateTv.setTextColor(getColor(R.color.black))
                taskEndDate = LocalDate.of(year, month + 1, dayOfMonth).format(dateFormatter)
            }, currentYear, currentMonth, currentDay).show()
        }
        binding.saveTaskButton.setOnClickListener {
            taskTitle = taskTitleEt.text.toString()
            taskDescription = taskDescriptionEt.text.toString()
            when (radioGroup.checkedRadioButtonId) {
                R.id.lowPriority -> taskPriority = "Low"
                R.id.normalPriority -> taskPriority = "Normal"
                R.id.criticalPriority -> taskPriority = "Critical"
            }


            if (taskTitle.isNotEmpty() && taskDescription.isNotEmpty() && taskStartDate.isNotEmpty() && taskEndDate.isNotEmpty()) {
                AlertDialog.Builder(this)
                    .setTitle("Save Task")
                    .setMessage("Are you sure you want to save this task?")
                    .setPositiveButton("Yes") { _, _ ->
                        if (newTask) {
                            // save task
                            taskData = TaskData(
                                title = taskTitle,
                                description = taskDescription,
                                startDate = taskStartDate,
                                endDate = taskEndDate,
                                priority = taskPriority
                            )

                            saveTaskToFirebase(taskData)
                        } else {
                            // edit task
                            if (taskDescription.isNotEmpty()) {

                                taskData = TaskData(
                                    id = intent.getStringExtra("taskId")!!,
                                    title = taskTitleEt.text.toString(),
                                    description = taskDescriptionEt.text.toString(),
                                    startDate = binding.startDateTv.text.toString(),
                                    endDate = binding.endDateTv.text.toString(),
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
            } else if (taskStartDate.isEmpty()) {
                Snackbar.make(binding.root, "Task start date is required", Snackbar.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            } else if (taskEndDate.isEmpty()) {
                Snackbar.make(binding.root, "Task end date is required", Snackbar.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }


        }
        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
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

    private fun handleBackPressed(){
        if (!newTask) {

            if (taskTitleEt.text.toString() != taskTitle || taskDescriptionEt.text.toString() != taskDescription || binding.startDateTv.text.toString() != taskStartDate || binding.endDateTv.text.toString() != taskEndDate || when (radioGroup.checkedRadioButtonId) {
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
        }
        else /* new task == true */ {
            if (taskTitleEt.text.toString().isNotEmpty() || taskDescriptionEt.text.toString().isNotEmpty()) {                      AlertDialog.Builder(this)
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

