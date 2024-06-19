@file:Suppress("DEPRECATION")

package com.example.clock.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.clock.R
import com.example.clock.alarm.AlarmReceiver
import com.example.clock.alarm.SharedPreferencesHelper
import com.example.clock.database.Alarm
import com.example.clock.database.AlarmDatabase
import com.example.clock.database.AlarmRepository
import com.example.clock.database.AlarmViewModel
import com.example.clock.database.StringTypeConverter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar

class AlarmFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var alarmViewModel: AlarmViewModel
    private lateinit var alarmAdapter: AlarmAdapter
    private lateinit var daysListString: String
    private var selectHour: Int = 0
    private var selectMinute: Int = 0
    private var days = mutableListOf<String>()

    private lateinit var alarmReceiver: AlarmReceiver

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
        private const val RINGTONE_REQUEST_CODE = 1002
        const val ACTION_ALARM_TRIGGERED = "com.example.clock.ALARM_TRIGGERED"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alarmReceiver = AlarmReceiver()

        val filter = IntentFilter(Intent.ACTION_BOOT_COMPLETED)
        val permission = Manifest.permission.RECEIVE_BOOT_COMPLETED  // Specify the correct permission here

        // Check if the permission is granted before registering the receiver
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            requireContext().registerReceiver(alarmReceiver, filter, permission, null)
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.alrm_scrn, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        alarmAdapter = AlarmAdapter()
        recyclerView = view.findViewById(R.id.recycler_view2)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = alarmAdapter
        }

        val alarmDao = AlarmDatabase.getInstance(requireContext()).alarmDao()
        val repository = AlarmRepository(alarmDao)
        val viewModelFactory = AlarmViewModel.AlarmViewModelFactory(repository)

        alarmViewModel = ViewModelProvider(this, viewModelFactory)[AlarmViewModel::class.java]
        alarmViewModel.allAlarms.observe(viewLifecycleOwner) { alarms ->
            alarms?.let { alarmAdapter.setAlarms(it) }
        }
        alarmViewModel.allAlarms.observe(viewLifecycleOwner){alarms ->
            alarms?.let {alarmAdapter.updateAlarms(it)}
        }

        val addBtn = view.findViewById<FloatingActionButton>(R.id.add)
        addBtn.setOnClickListener {
            showAlarmSettingsDialog()
        }
    }

    private fun showAlarmSettingsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.alarm_dialog, null)
        val editTextAlarmName = dialogView.findViewById<EditText>(R.id.editTextAlarmName)
        editTextAlarmName.setText("Alarm")

        val checkBoxMon = dialogView.findViewById<CheckBox>(R.id.checkBoxMon)
        val checkBoxTue = dialogView.findViewById<CheckBox>(R.id.checkBoxTue)
        val checkBoxWed = dialogView.findViewById<CheckBox>(R.id.checkBoxWed)
        val checkBoxThu = dialogView.findViewById<CheckBox>(R.id.checkBoxThu)
        val checkBoxFri = dialogView.findViewById<CheckBox>(R.id.checkBoxFri)
        val checkBoxSat = dialogView.findViewById<CheckBox>(R.id.checkBoxSat)
        val checkBoxSun = dialogView.findViewById<CheckBox>(R.id.checkBoxSun)

        val onceBtn = dialogView.findViewById<RadioButton>(R.id.once)
        val dailyBtn = dialogView.findViewById<RadioButton>(R.id.daily)
        val customBtn = dialogView.findViewById<RadioButton>(R.id.custom)

        val list1 = dialogView.findViewById<LinearLayout>(R.id.dayList1)
        val list2 = dialogView.findViewById<LinearLayout>(R.id.daysList2)

        onceBtn.isEnabled = true
        list1.visibility = View.GONE
        list2.visibility = View.GONE

        onceBtn.setOnClickListener{
            list1.visibility = View.GONE
            list2.visibility = View.GONE
        }

        dailyBtn.setOnClickListener{
            list1.visibility = View.GONE
            list2.visibility = View.GONE
        }

        customBtn.setOnClickListener{
            list1.visibility = View.VISIBLE
            list2.visibility = View.VISIBLE
        }

        val buttonSetRingtone = dialogView.findViewById<Button>(R.id.setRingtoneBtn)

        buttonSetRingtone.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                showRingtonePicker()
            }
        }

        val alertDialog = AlertDialog.Builder(requireContext(), R.style.DialogCustomTheme)
            .setTitle("Set Alarm")
            .setView(dialogView)
            .setPositiveButton("Set") { dialog, _ ->
                days = mutableListOf()
                if(dailyBtn.isChecked){
                    val alldays = listOf<String>("Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday")
                    days.addAll(alldays)
                }
                if(onceBtn.isChecked){
                    val calendar = Calendar.getInstance()
                    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

                    val day = when (dayOfWeek) {
                        Calendar.SUNDAY -> "Sunday"
                        Calendar.MONDAY -> "Monday"
                        Calendar.TUESDAY -> "Tuesday"
                        Calendar.WEDNESDAY -> "Wednesday"
                        Calendar.THURSDAY -> "Thursday"
                        Calendar.FRIDAY -> "Friday"
                        Calendar.SATURDAY -> "Saturday"
                        else -> throw IllegalArgumentException("Unexpected day of the week: $dayOfWeek")
                    }
                    days.add(day)
                }
                if (checkBoxMon.isChecked) days.add("Monday")
                if (checkBoxTue.isChecked) days.add("Tuesday")
                if (checkBoxWed.isChecked) days.add("Wednesday")
                if (checkBoxThu.isChecked) days.add("Thursday")
                if (checkBoxFri.isChecked) days.add("Friday")
                if (checkBoxSat.isChecked) days.add("Saturday")
                if (checkBoxSun.isChecked) days.add("Sunday")

                val alarmName = editTextAlarmName.text.toString()
                daysListString = StringTypeConverter.fromStringList(days)

                saveAlarm(alarmName, daysListString)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        val setTimeBtn = dialogView.findViewById<Button>(R.id.buttonSetTime)
        setTimeBtn.setOnClickListener {
            val alarmName = editTextAlarmName.text.toString()
            val selectedDays = days.toList() // Convert to list if needed
            showTimePickerDialog(alarmName, selectedDays)
        }

        alertDialog.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showAlarmEditDialog(alarm: Alarm) {
        val dialogView = layoutInflater.inflate(R.layout.alarm_dialog, null)
        val editAlarmName = dialogView.findViewById<EditText>(R.id.editTextAlarmName)
        val checkBoxMon = dialogView.findViewById<CheckBox>(R.id.checkBoxMon)
        val checkBoxTue = dialogView.findViewById<CheckBox>(R.id.checkBoxTue)
        val checkBoxWed = dialogView.findViewById<CheckBox>(R.id.checkBoxWed)
        val checkBoxThu = dialogView.findViewById<CheckBox>(R.id.checkBoxThu)
        val checkBoxFri = dialogView.findViewById<CheckBox>(R.id.checkBoxFri)
        val checkBoxSat = dialogView.findViewById<CheckBox>(R.id.checkBoxSat)
        val checkBoxSun = dialogView.findViewById<CheckBox>(R.id.checkBoxSun)

        editAlarmName.setText(alarm.name)
        val daysList = StringTypeConverter.toStringList(alarm.days)
        checkBoxMon.isChecked = daysList.contains("Monday")
        checkBoxTue.isChecked = daysList.contains("Tuesday")
        checkBoxWed.isChecked = daysList.contains("Wednesday")
        checkBoxThu.isChecked = daysList.contains("Thursday")
        checkBoxFri.isChecked = daysList.contains("Friday")
        checkBoxSat.isChecked = daysList.contains("Saturday")
        checkBoxSun.isChecked = daysList.contains("Sunday")

        val onceBtn = dialogView.findViewById<RadioButton>(R.id.once)
        val dailyBtn = dialogView.findViewById<RadioButton>(R.id.daily)
        val customBtn = dialogView.findViewById<RadioButton>(R.id.custom)

        val list1 = dialogView.findViewById<LinearLayout>(R.id.dayList1)
        val list2 = dialogView.findViewById<LinearLayout>(R.id.daysList2)

        list1.visibility = View.GONE
        list2.visibility = View.GONE

        onceBtn.setOnClickListener{
            list1.visibility = View.GONE
            list2.visibility = View.GONE
        }

        dailyBtn.setOnClickListener{
            list1.visibility = View.GONE
            list2.visibility = View.GONE
        }

        customBtn.setOnClickListener{
            list1.visibility = View.VISIBLE
            list2.visibility = View.VISIBLE
        }

        val buttonSetRingtone = dialogView.findViewById<Button>(R.id.setRingtoneBtn)

        buttonSetRingtone.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                showRingtonePicker()
            }
        }

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Edit Alarm")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                days = mutableListOf()

                if(dailyBtn.isChecked){
                    val alldays = listOf<String>("Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday")
                    days.addAll(alldays)
                }
                if(onceBtn.isChecked){
                    val calendar = Calendar.getInstance()
                    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

                    val day = when (dayOfWeek) {
                        Calendar.SUNDAY -> "Sunday"
                        Calendar.MONDAY -> "Monday"
                        Calendar.TUESDAY -> "Tuesday"
                        Calendar.WEDNESDAY -> "Wednesday"
                        Calendar.THURSDAY -> "Thursday"
                        Calendar.FRIDAY -> "Friday"
                        Calendar.SATURDAY -> "Saturday"
                        else -> throw IllegalArgumentException("Unexpected day of the week: $dayOfWeek")
                    }
                    days.add(day)
                }

                if (checkBoxMon.isChecked) days.add("Monday")
                if (checkBoxTue.isChecked) days.add("Tuesday")
                if (checkBoxWed.isChecked) days.add("Wednesday")
                if (checkBoxThu.isChecked) days.add("Thursday")
                if (checkBoxFri.isChecked) days.add("Friday")
                if (checkBoxSat.isChecked) days.add("Saturday")
                if (checkBoxSun.isChecked) days.add("Sunday")

                val alarmName = editAlarmName.text.toString()
                daysListString = StringTypeConverter.fromStringList(days)

                updateAlarm(alarm.id, alarmName, daysListString)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        val setTimeBtn = dialogView.findViewById<Button>(R.id.buttonSetTime)
        setTimeBtn.setOnClickListener {
            val alarmName = editAlarmName.text.toString()
            val selectedDays = days.toList()
            showTimePickerDialog(alarmName, selectedDays)
        }

        alertDialog.show()
    }

    private fun saveAlarm(alarmName: String, daysListString: String) {
        val alarm = Alarm(
            name = alarmName,
            days = daysListString,
            hour = selectHour,
            minute = selectMinute,
            isEnabled = true
        )
        scheduleAlarm(alarm.id, selectHour, selectMinute, alarmName, daysListString, true)
        alarmViewModel.insert(alarm)
    }

    private fun updateAlarm(id: Long, alarmName: String, daysListString: String) {
        val alarm = Alarm(
            id = id,
            name = alarmName,
            days = daysListString,
            hour = selectHour,
            minute = selectMinute,
            isEnabled = true
        )
        scheduleAlarm(alarm.id, selectHour, selectMinute, alarmName, daysListString, true)
        alarmViewModel.update(alarm)
    }

    private fun showTimePickerDialog(alarmName: String, days: List<String>) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                daysListString = StringTypeConverter.fromStringList(days)
                selectHour = selectedHour
                selectMinute = selectedMinute
            },
            hour,
            minute,
            false
        )
        timePickerDialog.show()
    }

    private fun showRingtonePicker() {

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        } else {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Ringtone")
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, getSelectedRingtoneUri())

        startActivityForResult(intent, RINGTONE_REQUEST_CODE)
        }
    }

    private fun getSelectedRingtoneUri(): Uri? {
        val ringtoneUriString = SharedPreferencesHelper.getRingtoneUri(requireContext())
        return if (!ringtoneUriString.isNullOrEmpty()) Uri.parse(ringtoneUriString) else null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RINGTONE_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            val ringtoneUri: Uri? = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            ringtoneUri?.let { uri ->

                SharedPreferencesHelper.setRingtoneUri(requireContext(), uri.toString())

                Log.d("TAG", "Selected Ringtone URI: $uri")
            }
        }
    }

    private fun scheduleAlarm(id: Long, hour: Int, minute: Int, alarmName: String, daysListString: String, isEnabled: Boolean = true) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        intent.action = ACTION_ALARM_TRIGGERED
        intent.putExtra("alarm_name", alarmName)
        intent.putExtra("days_list", daysListString)

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            id.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)

        if (!isEnabled) {
            alarmManager.cancel(pendingIntent)
            return
        }

        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val daysList = StringTypeConverter.toStringList(daysListString)
        if (!daysList.contains(getDayOfWeek(today))) {
            return
        }

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1)
        }

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    private fun getDayOfWeek(calendarDay: Int): String {
        return when (calendarDay) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> throw IllegalArgumentException("Invalid day of the week")
        }
    }

    private fun cancelAlarm(alarm: Alarm) {
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager =
            requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    inner class AlarmAdapter : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

        private var alarms: List<Alarm> = emptyList()

        inner class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            @SuppressLint("UseSwitchCompatOrMaterialCode", "NotifyDataSetChanged")
            fun bind(alarm: Alarm) {

                val daysEnabled = StringTypeConverter.toStringList(alarm.days)
                itemView.findViewById<TextView>(R.id.Mon).setTextColor(if ("Monday" in daysEnabled) Color.BLACK else Color.GRAY)
                itemView.findViewById<TextView>(R.id.Tue).setTextColor(if ("Tuesday" in daysEnabled) Color.BLACK else Color.GRAY)
                itemView.findViewById<TextView>(R.id.Wed).setTextColor(if ("Wednesday" in daysEnabled) Color.BLACK else Color.GRAY)
                itemView.findViewById<TextView>(R.id.Thu).setTextColor(if ("Thursday" in daysEnabled) Color.BLACK else Color.GRAY)
                itemView.findViewById<TextView>(R.id.Fri).setTextColor(if ("Friday" in daysEnabled) Color.BLACK else Color.GRAY)
                itemView.findViewById<TextView>(R.id.Sat).setTextColor(if ("Saturday" in daysEnabled) Color.BLACK else Color.GRAY)
                itemView.findViewById<TextView>(R.id.Sun).setTextColor(if ("Sunday" in daysEnabled) Color.BLACK else Color.GRAY)

                itemView.findViewById<TextView>(R.id.deadlineDate).text = "${alarm.hour}:${alarm.minute}"
                itemView.findViewById<TextView>(R.id.titleTxt).text = alarm.name

                val toggleBtn = itemView.findViewById<Switch>(R.id.toggle)
                toggleBtn.isChecked = alarm.isEnabled
                toggleBtn.setOnCheckedChangeListener { _, isChecked ->
                    alarm.isEnabled = isChecked
                    if (!isChecked) {
                        cancelAlarm(alarm)
                    }
                    alarmViewModel.update(alarm)
                }

                val deleteBtn = itemView.findViewById<ImageView>(R.id.delete)
                deleteBtn.setOnClickListener {
                    cancelAlarm(alarm)
                    alarmViewModel.delete(alarm.name,alarm.hour,alarm.minute)
                    notifyDataSetChanged()
                }

                val editBtn = itemView.findViewById<ImageView>(R.id.editing)
                editBtn.setOnClickListener{
                    showAlarmEditDialog(alarm)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.view_alarm, parent, false)
            return AlarmViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
            holder.bind(alarms[position])
        }

        override fun getItemCount(): Int {
            return alarms.size
        }

        @SuppressLint("NotifyDataSetChanged")
        fun setAlarms(alarms: List<Alarm>) {
            this.alarms = alarms
            notifyDataSetChanged()
        }
        @SuppressLint("NotifyDataSetChanged")
        fun updateAlarms(alarms: List<Alarm>) {
            this.alarms = alarms
            notifyDataSetChanged()
        }
    }

}
