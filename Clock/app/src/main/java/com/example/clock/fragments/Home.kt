package com.example.clock.fragments
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.clock.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var time: TextView
    private lateinit var date: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.home_scrn, container, false)

        // Initialize TextViews
        time = view.findViewById(R.id.timeTextView)
        date = view.findViewById(R.id.dateTextView)

        updateTimeAndDate()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.postDelayed(updateTimeRunnable, 1000)
    }

    private fun updateTimeAndDate() {
        val currentTime = Calendar.getInstance().time
        val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        val formattedTime = timeFormat.format(currentTime)
        time.text = formattedTime

        // Format date
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(currentTime)
        date.text = formattedDate
    }

    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            updateTimeAndDate()
            view?.postDelayed(this, 1000)
        }
    }
}
