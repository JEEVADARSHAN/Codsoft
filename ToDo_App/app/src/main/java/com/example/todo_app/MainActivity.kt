package com.example.todo_app

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.todo_app.adapters.TaskRVVBListAdapter
import com.example.todo_app.databinding.ActivityMainBinding
import com.example.todo_app.models.Task
import com.example.todo_app.utils.Status
import com.example.todo_app.utils.StatusResult
import com.example.todo_app.utils.StatusResult.Added
import com.example.todo_app.utils.StatusResult.Deleted
import com.example.todo_app.utils.StatusResult.Updated
import com.example.todo_app.utils.clearEditText
import com.example.todo_app.utils.hideKeyBoard
import com.example.todo_app.utils.longToastShow
import com.example.todo_app.utils.setupDialog
import com.example.todo_app.utils.validateEditText
import com.example.todo_app.viewmodels.TaskViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.UUID


class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var viewPager: ViewPager
    private lateinit var dotsLayout: LinearLayout
    private lateinit var dots: Array<View>
    private lateinit var btnNext: Button
    private var currentPage = 0

    private val layouts = intArrayOf(
        R.layout.tutorial1,
        R.layout.tutorial2
    )

    lateinit var deadlineCalendar: Calendar
    lateinit var deadlineButton:Button
    private var selectedDeadline: Date? = null

    private val mainBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val addTaskDialog: Dialog by lazy {
        Dialog(this, R.style.DialogCustomTheme).apply {
            setupDialog(R.layout.add_task)
        }
    }

    private val updateTaskDialog: Dialog by lazy {
        Dialog(this, R.style.DialogCustomTheme).apply {
            setupDialog(R.layout.update_task)
        }
    }

    private val loadingDialog: Dialog by lazy {
        Dialog(this, R.style.DialogCustomTheme).apply {
            setupDialog(R.layout.loading_dialog)
        }
    }

    private val taskViewModel: TaskViewModel by lazy {
        ViewModelProvider(this)[TaskViewModel::class.java]
    }

    private val isListMutableLiveData = MutableLiveData<Boolean>().apply {
        postValue(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        setTheme(R.style.Theme_TodoApp)

        val status = getString(R.string.statusP)

        sharedPreferences = getSharedPreferences("tutorial_prefs", Context.MODE_PRIVATE)
        val hasSeenTutorial = sharedPreferences.getBoolean("hasSeenTutorial", false)
        if (!hasSeenTutorial) {
            setContentView(R.layout.tutorial1)
            viewPager = findViewById(R.id.viewPager)
            dotsLayout = findViewById(R.id.layoutDots)
            btnNext = findViewById(R.id.btnNext)

            addBottomDots(0)

            val adapter = TutorialPagerAdapter()
            viewPager.adapter = adapter
            viewPager.addOnPageChangeListener(viewPagerPageChangeListener)

            btnNext.setOnClickListener {
                val next = currentPage + 1
                if (next < layouts.size) {
                    viewPager.currentItem = next
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }//tutorial end
            with(sharedPreferences.edit()) {
                putBoolean("hasSeenTutorial", true)
                apply()
            }
        }else {
            setContentView(mainBinding.root)

            // Add task start
            val addCloseImg = addTaskDialog.findViewById<ImageView>(R.id.close)
            addCloseImg.setOnClickListener { addTaskDialog.dismiss() }

            val addETTitle = addTaskDialog.findViewById<TextInputEditText>(R.id.edTaskTitle)
            val addETTitleL = addTaskDialog.findViewById<TextInputLayout>(R.id.edTaskTitleL)

            addETTitle.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(s: Editable) {
                    validateEditText(addETTitle, addETTitleL)
                }

            })

            val addETDesc = addTaskDialog.findViewById<TextInputEditText>(R.id.edTaskDesc)
            val addETDescL = addTaskDialog.findViewById<TextInputLayout>(R.id.edTaskTDescL)

            addETDesc.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(s: Editable) {
                    validateEditText(addETDesc, addETDescL)
                }
            })

            deadlineButton = addTaskDialog.findViewById<Button>(R.id.deadlineButton)
            deadlineCalendar = Calendar.getInstance()
            deadlineButton.setOnClickListener {
                showDatePicker()
            }


            mainBinding.addTaskFABtn.setOnClickListener {
                clearEditText(addETTitle, addETTitleL)
                clearEditText(addETDesc, addETDescL)
                addTaskDialog.show()
            }

            val saveTaskBtn = addTaskDialog.findViewById<Button>(R.id.saveTaskBtn)
            saveTaskBtn.setOnClickListener {
                if (validateEditText(addETTitle, addETTitleL)
                    && validateEditText(addETDesc, addETDescL)
                    && selectedDeadline!=null
                ) {

                    val newTask = Task(
                        UUID.randomUUID().toString(),
                        addETTitle.text.toString().trim(),
                        addETDesc.text.toString().trim(),
                        Date(),
                        selectedDeadline!!,
                        status.toString(),
                    )
                    hideKeyBoard(it)
                    addTaskDialog.dismiss()
                    taskViewModel.insertTask(newTask)
                    setAlarmForTask(newTask)
                }
                else {
                    Toast.makeText(this, "Please select a deadline", Toast.LENGTH_SHORT).show()
                }
            }
            // Add task end

            // Update Task Start
            val updateETTitle = updateTaskDialog.findViewById<TextInputEditText>(R.id.edTaskTitle)
            val updateETTitleL = updateTaskDialog.findViewById<TextInputLayout>(R.id.edTaskTitleL)
            val deadlineBtn = updateTaskDialog.findViewById<Button>(R.id.deadlineButton)
            val statusBtn = updateTaskDialog.findViewById<Button>(R.id.status)

            deadlineBtn.text = updateDeadlineUI()
            deadlineBtn.setOnClickListener {
                showDatePicker()
                deadlineBtn.text = updateDeadlineUI()
            }
            statusBtn.text = updateStatusUI()
            statusBtn.setOnClickListener {
                statusBtn.text = updateStatusUI(statusBtn.text.toString())
            }

            updateETTitle.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(s: Editable) {
                    validateEditText(updateETTitle, updateETTitleL)
                }

            })

            val updateETDesc = updateTaskDialog.findViewById<TextInputEditText>(R.id.edTaskDesc)
            val updateETDescL = updateTaskDialog.findViewById<TextInputLayout>(R.id.edTaskTDescL)

            updateETDesc.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(s: Editable) {
                    validateEditText(updateETDesc, updateETDescL)
                }
            })

            val updateCloseImg = updateTaskDialog.findViewById<ImageView>(R.id.close)
            updateCloseImg.setOnClickListener { updateTaskDialog.dismiss() }

            val updateTaskBtn = updateTaskDialog.findViewById<Button>(R.id.updateTaskBtn)

            // Update Task End

            isListMutableLiveData.observe(this){
                if (it){
                    mainBinding.taskRV.layoutManager = LinearLayoutManager(
                        this,LinearLayoutManager.VERTICAL,false
                    )
                    mainBinding.listOrGridImg.setImageResource(R.drawable.grid)
                }else{
                    mainBinding.taskRV.layoutManager = StaggeredGridLayoutManager(
                        2,LinearLayoutManager.VERTICAL
                    )
                    mainBinding.listOrGridImg.setImageResource(R.drawable.list)
                }
            }

            mainBinding.listOrGridImg.setOnClickListener {
                isListMutableLiveData.postValue(!isListMutableLiveData.value!!)
            }

            val taskRVVBListAdapter = TaskRVVBListAdapter(isListMutableLiveData ) { type, position, task ->
                if (type == "delete") {
                    taskViewModel
                        // Deleted Task
                        .deleteTaskUsingId(task.id)

                    // Restore Deleted task
                    restoreDeletedTask(task)
                } else if (type == "update") {
                    updateETTitle.setText(task.title)
                    updateETDesc.setText(task.description)
                    updateTaskBtn.setOnClickListener {
                        if (validateEditText(updateETTitle, updateETTitleL)
                            && validateEditText(updateETDesc, updateETDescL)
                        ) {
                            val updateTask = Task(
                                task.id,
                                updateETTitle.text.toString().trim(),
                                updateETDesc.text.toString().trim(),
                                Date(),
                                selectedDeadline ?: Date(),
                                statusBtn.text.toString(),
                            )
                            hideKeyBoard(it)
                            updateTaskDialog.dismiss()
                            setAlarmForTask(updateTask)
                            taskViewModel
                                .updateTask(updateTask)
                        }
                    }
                    updateTaskDialog.show()
                }
            }
            mainBinding.taskRV.adapter = taskRVVBListAdapter
            ViewCompat.setNestedScrollingEnabled(mainBinding.taskRV,false)
            taskRVVBListAdapter.registerAdapterDataObserver(object :
                RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    super.onItemRangeInserted(positionStart, itemCount)
                    mainBinding.nestedScrollView.smoothScrollTo(0,positionStart)
                }
            })
            callGetTaskList(taskRVVBListAdapter)
            callSortByLiveData()
            statusCallback()

            callSearch()
        }

    }

    private fun restoreDeletedTask(deletedTask : Task){
        val snackBar = Snackbar.make(
            mainBinding.root, "Deleted '${deletedTask.title}'",
            Snackbar.LENGTH_LONG
        )
        snackBar.setAction("Undo"){
            taskViewModel.insertTask(deletedTask)
        }
        snackBar.show()
    }

    private fun callSearch() {
        mainBinding.edSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(query: Editable) {
                if (query.toString().isNotEmpty()){
                    taskViewModel.searchTaskList(query.toString())
                }else{
                    callSortByLiveData()
                }
            }
        })

        mainBinding.edSearch.setOnEditorActionListener{ v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH){
                hideKeyBoard(v)
                return@setOnEditorActionListener true
            }
            false
        }

        callSortByDialog()
    }
    private fun callSortByLiveData(){
        taskViewModel.sortByLiveData.observe(this){
            taskViewModel.getTaskList(it.second,it.first)
        }
    }

    private fun callSortByDialog() {
        var checkedItem = 0   // 2 is default item set
        val items = arrayOf("Title Ascending", "Title Descending","Date Ascending","Date Descending")

        mainBinding.sortImg.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Sort By")
                .setPositiveButton("Ok") { _, _ ->
                    when (checkedItem) {
                        0 -> {
                            taskViewModel.setSortBy(Pair("title",true))
                        }
                        1 -> {
                            taskViewModel.setSortBy(Pair("title",false))
                        }
                        2 -> {
                            taskViewModel.setSortBy(Pair("date",true))
                        }
                        else -> {
                            taskViewModel.setSortBy(Pair("date",false))
                        }
                    }
                }
                .setSingleChoiceItems(items, checkedItem) { _, selectedItemIndex ->
                    checkedItem = selectedItemIndex
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun statusCallback() {
        taskViewModel
            .statusLiveData
            .observe(this) {
                when (it.status) {
                    Status.LOADING -> {
                        loadingDialog.show()
                    }

                    Status.SUCCESS -> {
                        loadingDialog.dismiss()
                        when (it.data as StatusResult) {
                            Added -> {
                                Log.d("StatusResult", "Added")
                            }

                            Deleted -> {
                                Log.d("StatusResult", "Deleted")

                            }

                            Updated -> {
                                Log.d("StatusResult", "Updated")

                            }
                        }
                        it.message?.let { it1 -> longToastShow(it1) }
                    }

                    Status.ERROR -> {
                        loadingDialog.dismiss()
                        it.message?.let { it1 -> longToastShow(it1) }
                    }
                }
            }
    }

    private fun callGetTaskList(taskRecyclerViewAdapter: TaskRVVBListAdapter) {

        CoroutineScope(Dispatchers.Main).launch {
            taskViewModel
                .taskStateFlow
                .collectLatest {
                    Log.d("status", it.status.toString())

                    when (it.status) {
                        Status.LOADING -> {
                            loadingDialog.show()
                        }

                        Status.SUCCESS -> {
                            loadingDialog.dismiss()
                            it.data?.collect { taskList ->
                                taskRecyclerViewAdapter.submitList(taskList)
                            }
                        }

                        Status.ERROR -> {
                            loadingDialog.dismiss()
                            it.message?.let { it1 -> longToastShow(it1) }
                        }
                    }

                }
        }
    }

    private fun showDatePicker() {
        val currentDate = Calendar.getInstance()
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH)
        val day = currentDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, selectedYear)
                    set(Calendar.MONTH, selectedMonth)
                    set(Calendar.DAY_OF_MONTH, selectedDay)
                }
                showTimePickerDialog(calendar.time)
            },
            year,
            month,
            day
        )

        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    // Function to show time picker dialog
    private fun showTimePickerDialog(selectedDate: Date) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val updatedCalendar = Calendar.getInstance().apply {
                    time = selectedDate
                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                }
                selectedDeadline = updatedCalendar.time // Assign the selected deadline here
            },
            hour,
            minute,
            false
        )
        timePickerDialog.show()
    }

    private fun updateDeadlineUI(): String {
        val deadlineText = "Deadline: " + formatDate(deadlineCalendar) + " " + formatTime(deadlineCalendar)
        return deadlineText
    }
    private fun updateStatusUI(currentStatus: String = ""): String {
        if(currentStatus == "Status: In Progress"){
            return "Status: Completed"
        }else if(currentStatus == "Status: Completed"){
            return "Status: In Progress"
        }
        return "Status: In Progress"
    }

    private fun formatDate(calendar: Calendar): String {
        val dateFormat = android.text.format.DateFormat.getDateFormat(this)
        return dateFormat.format(calendar.time)
    }

    private fun formatTime(calendar: Calendar): String {
        val timeFormat = android.text.format.DateFormat.getTimeFormat(this)
        return timeFormat.format(calendar.time)
    }

    companion object {
        private const val CHANNEL_ID = "task_notifications"
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Task Notifications"
            val descriptionText = "Channel for task notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setAlarmForTask(task: Task) {
        // Calculate time difference between task's deadline and current time
        val currentTime = System.currentTimeMillis()
        val taskTime = task.deadline.time
        val timeDifference = taskTime - currentTime

        // Check if timeDifference is positive
        if (timeDifference > 0) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmIntent = Intent(this, AlarmReceiver::class.java)
            alarmIntent.putExtra("taskId", task.id) // Pass task ID to BroadcastReceiver
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                alarmIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeDifference, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeDifference, pendingIntent)
            }
        } else {
            // Task deadline has already passed
            Log.e("MainActivity", "Task deadline has already passed for task ID: ${task.id}")
        }
    }

    private fun addBottomDots(currentPage: Int) {
        dots = Array(layouts.size) { View(this) }
        dotsLayout.removeAllViews()
        val dotSize = resources.getDimensionPixelSize(R.dimen.dot_size)
        val params = LinearLayout.LayoutParams(dotSize, dotSize)
        params.setMargins(10, 0, 10, 0)

        for (i in dots.indices) {
            dots[i] = View(this)
            dots[i].setBackgroundResource(if (i == currentPage) R.drawable.dots_active else R.drawable.dots_inactive)
            dotsLayout.addView(dots[i], params)
        }
    }

    private val viewPagerPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            addBottomDots(position)
            currentPage = position
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}

        override fun onPageScrollStateChanged(arg0: Int) {}
    }

    inner class TutorialPagerAdapter : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(layouts[position], container, false)
            container.addView(view)
            return view
        }

        override fun getCount(): Int = layouts.size

        override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }
    }

}
