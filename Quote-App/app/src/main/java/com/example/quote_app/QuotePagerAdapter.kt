package com.example.quote_app


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import kotlin.math.min

class QuotePagerAdapter(private val quotesList: List<QuotesModel>) : PagerAdapter() {

    private lateinit var textViewQuote: TextView
    private lateinit var textViewAuthor: TextView

    override fun getCount(): Int {
        return quotesList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(container.context).inflate(R.layout.quote_item, container, false)
        textViewQuote = view.findViewById(R.id.textViewQuote)
        textViewAuthor = view.findViewById(R.id.textViewAuthor)
        val quote = quotesList[position]
        textViewQuote.text = quote.content
        textViewAuthor.text = "~ ${quote.author}"

        val maxLength = 100 // Maximum number of characters for minimum text size
        val textSize = min(24f, 24f - (quote.content!!.length - maxLength) / 10f) // Adjust as needed

        // Set text size
        textViewQuote.textSize = textSize

        container.addView(view)
        return view
    }

    fun returnQuote():String{
        return textViewQuote.text.toString()+textViewAuthor.text.toString()
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}