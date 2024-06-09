package com.example.quote_app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quote_app.Saving.Quote
import com.example.quote_app.Saving.QuoteDao
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@DelicateCoroutinesApi
class QuoteAdapter(
    private val context: Context,
    quotes: List<Quote>,
    private val quoteDao: QuoteDao,
    private val mainActivity: MainActivity
) : RecyclerView.Adapter<QuoteAdapter.QuoteViewHolder>(){

    private val quotesList: MutableList<Quote> = quotes.toMutableList()

    class QuoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val quoteTextView: TextView = itemView.findViewById(R.id.quoteTxt)
        val authorTextView: TextView = itemView.findViewById(R.id.authorTxt)
        val shareButton: ImageView = itemView.findViewById(R.id.share)
        val deleteButton: ImageView = itemView.findViewById(R.id.remove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.saved_quotes, parent, false)
        return QuoteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        val currentQuote = quotesList[position]
        holder.quoteTextView.text = currentQuote.quote
        holder.authorTextView.text = currentQuote.author

        holder.shareButton.setOnClickListener {
            shareQuote(context, currentQuote.quote)
        }
        holder.deleteButton.setOnClickListener{
            removeQuote(currentQuote)
        }
    }

    override fun getItemCount() = quotesList.size

    private fun shareQuote(context: Context, quote: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, quote)
        context.startActivity(Intent.createChooser(intent, "Share Quote"))
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun removeQuote(quote: Quote) {
        GlobalScope.launch(Dispatchers.IO) {
            quoteDao.deleteQuote(quote)
            quotesList.remove(quote) // Remove from quotesList
            (context as Activity).runOnUiThread {
                notifyDataSetChanged() // Notify adapter about data changes
                if (quotesList.isEmpty()) {
                    notifyItemChanged(0)
                    mainActivity.checkForEmpty(quotesList)// Notify that the first item has changed, triggering onBind
                }
            }
        }
    }
}
