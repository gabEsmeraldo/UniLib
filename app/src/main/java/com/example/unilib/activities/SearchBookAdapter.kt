package com.example.unilib.activities

import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.unilib.R
import com.example.unilib.models.Book

class SearchBookAdapter(
    private var books: List<Book>,
    private val onBookClick: (Book) -> Unit
) : RecyclerView.Adapter<SearchBookAdapter.SearchBookViewHolder>() {

    private val colorMap = mapOf(
        "blue" to R.drawable.bg_book_blue,
        "green" to R.drawable.bg_book_green,
        "purple" to R.drawable.bg_book_purple,
        "red" to R.drawable.bg_book_red,
        "gray" to R.drawable.bg_book_gray
    )

    fun updateBooks(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchBookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_book_card, parent, false)
        return SearchBookViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchBookViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount(): Int = books.size

    inner class SearchBookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvBookTitle)
        private val tvAuthorIsbn: TextView = itemView.findViewById(R.id.tvBookAuthorIsbn)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        private val tvAvailable: TextView = itemView.findViewById(R.id.tvAvailable)
        private val tagsContainer: LinearLayout = itemView.findViewById(R.id.tagsContainer)
        private val bookCover: FrameLayout = itemView.findViewById(R.id.bookCover)
        private val tvEmoji: TextView = itemView.findViewById(R.id.tvBookEmoji)
        private val ivCoverImage: ImageView = itemView.findViewById(R.id.ivBookCoverImage)

        fun bind(book: Book) {
            tvTitle.text = book.title
            tvAuthorIsbn.text = "${book.author} • ISBN: ${book.isbn}"

            tvQuantity.text = "${book.quantity} Exemplares totais"

            if (book.available > 0) {
                tvAvailable.text = "${book.available} disponíveis"
                tvAvailable.setTextColor(Color.parseColor("#166534"))
                itemView.context.getDrawable(R.drawable.bg_chat_input_bar)?.let { bg ->
                    tvAvailable.background = bg
                }
                tvAvailable.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#DCFCE7"))
            } else {
                tvAvailable.text = "Indisponível"
                tvAvailable.setTextColor(Color.parseColor("#B91C1C"))
                itemView.context.getDrawable(R.drawable.bg_chat_input_bar)?.let { bg ->
                    tvAvailable.background = bg
                }
                tvAvailable.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FFDADA"))
            }

            tagsContainer.removeAllViews()
            val dp8 = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8f, itemView.context.resources.displayMetrics
            ).toInt()
            val dp6 = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 6f, itemView.context.resources.displayMetrics
            ).toInt()
            val dp12 = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 12f, itemView.context.resources.displayMetrics
            ).toInt()

            book.tags.take(2).forEach { tag ->
                val tv = TextView(itemView.context).apply {
                    text = tag
                    setTextColor(Color.parseColor("#0D4C92"))
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                    setBackgroundResource(R.drawable.bg_chat_input_bar)
                    setPadding(dp12, dp6, dp12, dp6)
                    backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#F3F4F6"))
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = dp8 }
                tagsContainer.addView(tv, params)
            }

            val coverDrawableRes = colorMap["blue"] ?: R.drawable.bg_book_blue
            bookCover.setBackgroundResource(coverDrawableRes)

            tvEmoji.visibility = View.VISIBLE
            ivCoverImage.visibility = View.GONE

            if (book.imageUrl.isNotEmpty()) {
                ImageUtils.base64ToBitmap(book.imageUrl)?.let { bitmap ->
                    tvEmoji.visibility = View.GONE
                    ivCoverImage.setImageBitmap(bitmap)
                    ivCoverImage.visibility = View.VISIBLE
                }
            }

            itemView.setOnClickListener { onBookClick(book) }
        }
    }
}
