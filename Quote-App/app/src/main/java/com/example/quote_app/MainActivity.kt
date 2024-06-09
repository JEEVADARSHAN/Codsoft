@file:OptIn(DelicateCoroutinesApi::class)

package com.example.quote_app

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.quote_app.Saving.Database
import com.example.quote_app.Saving.Quote
import com.example.quote_app.Saving.QuoteDao
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var quoteAdapter: QuotePagerAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var image : ImageView
    private lateinit var share : ImageButton
    private lateinit var fav : ImageButton
    private lateinit var quoteDao : QuoteDao
    private lateinit var noSaved : TextView
    private lateinit var recyclerView : RecyclerView
    private lateinit var chipGroup: ChipGroup
    private var viewPager: CustomViewPager? = null
    private var filterTag =""
    private var liked = false
    private val quotesHistory: MutableList<QuotesModel> = mutableListOf()
    private val filtersTagList = listOf("Happiness","Education","Business","Faith","Friendship","Future","History","Inspirational","Life","Love","Science","Wisdom","Technology")
    companion object {
        lateinit var database: Database
    }

    @SuppressLint("MissingInflatedId", "ClickableViewAccessibility", "InflateParams",
        "ResourceAsColor"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_main)

        database = Room.databaseBuilder(applicationContext, Database::class.java, "quotes-db").build()
        quoteDao = database.Dao()
        quoteAdapter = QuotePagerAdapter(listOf())
        viewPager?.adapter = quoteAdapter

        val saved = findViewById<ImageButton>(R.id.btn_save)
        image = findViewById(R.id.ImageForQuote)
        share = findViewById(R.id.btn_share)
        fav = findViewById(R.id.btn_fav)

        viewPager = findViewById(R.id.viewPager)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        chipGroup = findViewById(R.id.tagChipGroup)
        val inflaters = layoutInflater
        for (tagName in filtersTagList) {
            val chip =inflaters.inflate(R.layout.chip_select, chipGroup, false) as Chip
            chip.text = tagName
            chip.isCheckable = true
            val tagNameCrt = tagName.lowercase()
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {

                    chip.isChipIconVisible = false
                    chip.isCloseIconVisible = true

                    filterTag = if (filterTag.isEmpty()) tagNameCrt else filterTag + "|$tagNameCrt"
                    nextQuote()
                } else {

                    chip.isChipIconVisible = true
                    chip.isCloseIconVisible = false

                    filterTag = if (filterTag.endsWith("|$tagNameCrt")) filterTag.replace("|$tagNameCrt", "").removeSuffix("|")
                    else if (filterTag == tagNameCrt) ""
                    else filterTag.removeSuffix("|$tagNameCrt")
                    nextQuote()
                }
                setFavButton()
            }
            chipGroup.addView(chip)
        }
        getQuote()

        swipeRefreshLayout.setOnRefreshListener {
            quotesHistory.clear()
            getQuote()
        }

        swipeRefreshLayout.isRefreshing = true
        swipeRefreshLayout.post { swipeRefreshLayout.isRefreshing = false }

        viewPager?.setOnTouchListener(object : OnSwipeTouchListener(this@MainActivity) {
            override fun onSwipeLeft() {
                nextQuote()
            }
            override fun onSwipeRight() {
                previousQuote()
            }
        })

        share.setOnClickListener{
            shareQuote()
        }

        fav.setOnClickListener {
            val quotes = quoteAdapter.returnQuote().split("~")
            for (i in quotes.indices step 2) {
                val quoteText = quotes.getOrNull(i)
                val author = quotes.getOrNull(i + 1)

                if (quoteText != null && author != null) {
                    val quote = Quote(0, quoteText, author = author)
                    lifecycleScope.launch(Dispatchers.IO) {
                        val existingQuote = quoteDao.getQuoteByTextAndAuthor(quoteText, author)
                        if (existingQuote != null) {
                            quoteDao.deleteQuote(existingQuote)
                            withContext(Dispatchers.Main) {
                                fav.setImageResource(R.drawable.like)
                            }
                        } else {
                            quoteDao.insertQuote(quote)
                            withContext(Dispatchers.Main) {
                                fav.setImageResource(R.drawable.liked)
                            }
                        }
                    }
                }
            }
        }

        saved.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                val savedQuotes = quoteDao.getAllQuotes()
                // Show the custom dialog on the main thread
                launch(Dispatchers.Main) {
                    showCustomDialog(savedQuotes)
                }
            }
        }

    }

    private fun getQuote(){
        ApiCall().getRandomQuote { quote ->
            if (quote != null) {
                quotesHistory.add(quote)
                updateQuoteAdapter()
                getImage()
                setFavButton()
                } else {
                Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show()
            }
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun getQuoteWithTag(tag:String){
        ApiCall().getRandomQuoteWithTag(tag) { quote ->
            if (quote != null) {
                quotesHistory.add(quote)
                updateQuoteAdapter()
                getImage()
                setFavButton()
            } else {
                Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show()
            }
            swipeRefreshLayout.isRefreshing = false
        }
    }
    private fun getImage() {
        val random = java.util.Random().nextInt()
        val imageUrl = "https://picsum.photos/200/300/?random&$random"
        Glide.with(this)
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable caching
            .into(image)
    }

    private fun updateQuoteAdapter() {
        quoteAdapter = QuotePagerAdapter(quotesHistory)
        viewPager?.adapter = quoteAdapter
        viewPager?.setCurrentItem(quotesHistory.size - 1, true)
    }

    fun previousQuote() {
        if (quotesHistory.isNotEmpty() && viewPager!!.currentItem > 0) {
            viewPager!!.currentItem -= 1
        } else {
            Toast.makeText(this, "No previous quotes available", Toast.LENGTH_SHORT).show()
        }
        setFavButton()
    }

    fun nextQuote() {
        if (quotesHistory.isNotEmpty() && viewPager!!.currentItem < quotesHistory.size - 1) {
            viewPager!!.currentItem += 1
        } else {
            if(filterTag == ""){
                getQuote()
            }else{
                getQuoteWithTag(filterTag)
            }
        }
        setFavButton()
    }

    private fun shareQuote() {
        val text = quoteAdapter.returnQuote()
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun setFavButton(){
        val currentQuote = quotesHistory[viewPager!!.currentItem]

        GlobalScope.launch(Dispatchers.IO) {
            val allQuotes = quoteDao.getAllQuotes()
            val state = allQuotes.any { it.quote == currentQuote.content }

            launch(Dispatchers.Main) {
                liked = if (state) {
                    fav.setImageResource(R.drawable.liked)
                    true
                } else {
                    fav.setImageResource(R.drawable.like)
                    false
                }
            }
        }
    }

    private fun showCustomDialog(quotes: List<Quote>) {
        val dialogBuilder = AlertDialog.Builder(this,R.style.DialogTheme)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.view_fav, null)

        noSaved = dialogView.findViewById(R.id.NoSaved)
        recyclerView = dialogView.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        checkForEmpty(quotes)

        dialogBuilder.setView(dialogView)

        val dialog = dialogBuilder.create()
        dialog.show()
    }
    fun checkForEmpty(quotes: List<Quote>){
        if (quotes.isEmpty()) {
            noSaved.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            noSaved.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            val adapter = QuoteAdapter(this, quotes, quoteDao,this)
            recyclerView.adapter = adapter
        }
    }
}
