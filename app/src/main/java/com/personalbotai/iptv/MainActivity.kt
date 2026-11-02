package com.personalbotai.iptv

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChannelAdapter(private var items: List<Channel>, private val onClick: (Channel) -> Unit) :
    RecyclerView.Adapter<ChannelAdapter.ViewHolder>() {

    class ViewHolder(val card: LinearLayout, val title: TextView, val category: TextView) : RecyclerView.ViewHolder(card)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val dp = context.resources.displayMetrics.density

        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = RecyclerView.LayoutParams(
                (200 * dp).toInt(),
                (120 * dp).toInt()
            ).apply {
                setMargins((12 * dp).toInt(), (8 * dp).toInt(), (12 * dp).toInt(), (8 * dp).toInt())
            }
            setBackgroundColor(0xFF1F1F1F.toInt())
            setPadding((16 * dp).toInt(), (16 * dp).toInt(), (16 * dp).toInt(), (16 * dp).toInt())
            isFocusable = true
            isFocusableInTouchMode = true
            isClickable = true
            setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    v.setBackgroundColor(0xFFE50914.toInt()) // Red focus
                    v.scaleX = 1.08f
                    v.scaleY = 1.08f
                } else {
                    v.setBackgroundColor(0xFF1F1F1F.toInt())
                    v.scaleX = 1.0f
                    v.scaleY = 1.0f
                }
            }
        }

        val title = TextView(context).apply {
            setTextColor(Color.WHITE)
            textSize = 15f
            maxLines = 2
            ellipsize = TextUtils.TruncateAt.END
            setTypeface(null, Typeface.BOLD)
        }
        card.addView(title)

        val category = TextView(context).apply {
            setTextColor(0xFFB3B3B3.toInt())
            textSize = 12f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (8 * dp).toInt()
            }
        }
        card.addView(category)

        return ViewHolder(card, title, category)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val channel = items[position]
        holder.title.text = channel.name
        holder.category.text = channel.group
        holder.card.setOnClickListener { onClick(channel) }
    }

    override fun getItemCount() = items.size
}

class CategoryRowAdapter(private val categories: List<Pair<String, List<Channel>>>, private val onChannelClick: (Channel) -> Unit) :
    RecyclerView.Adapter<CategoryRowAdapter.RowViewHolder>() {

    class RowViewHolder(val root: LinearLayout, val title: TextView, val recycler: RecyclerView) : RecyclerView.ViewHolder(root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {
        val context = parent.context
        val dp = context.resources.displayMetrics.density

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, (14 * dp).toInt(), 0, (14 * dp).toInt())
            }
        }

        val title = TextView(context).apply {
            setTextColor(Color.WHITE)
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setPadding((40 * dp).toInt(), 0, 0, (10 * dp).toInt())
        }
        root.addView(title)

        val recycler = RecyclerView(context).apply {
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            )
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
        root.addView(recycler)

        return RowViewHolder(root, title, recycler)
    }

    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
        val (cat, list) = categories[position]
        holder.title.text = "$cat (${list.size})"
        holder.recycler.adapter = ChannelAdapter(list, onChannelClick)
    }

    override fun getItemCount() = categories.size
}

class MainActivity : Activity() {

    private lateinit var mainContainer: LinearLayout
    private lateinit var headerTitle: TextView
    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var categoryRecycler: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val dp = resources.displayMetrics.density

        mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF141414.toInt())
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Header bar: "IPTV | Live TV Worldwide" (Tanpa nama Netflix)
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding((40 * dp).toInt(), (20 * dp).toInt(), (40 * dp).toInt(), (12 * dp).toInt())
            gravity = Gravity.CENTER_VERTICAL
        }

        headerTitle = TextView(this).apply {
            text = "IPTV"
            setTextColor(0xFFE50914.toInt()) // Red accent
            textSize = 28f
            setTypeface(null, Typeface.BOLD)
        }
        header.addView(headerTitle)

        statusText = TextView(this).apply {
            text = "  |  Memuat channel global..."
            setTextColor(0xFFB3B3B3.toInt())
            textSize = 15f
        }
        header.addView(statusText)

        progressBar = ProgressBar(this).apply {
            val size = (24 * dp).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                leftMargin = (16 * dp).toInt()
            }
        }
        header.addView(progressBar)

        mainContainer.addView(header)

        // Categories Recycler
        categoryRecycler = RecyclerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        mainContainer.addView(categoryRecycler)

        setContentView(mainContainer)

        ChannelRepository.onLoaded = {
            renderChannels()
        }
        ChannelRepository.load()
    }

    private fun renderChannels() {
        val total = ChannelRepository.channels.size
        statusText.text = "  |  $total Channels Worldwide"
        progressBar.visibility = View.GONE

        // Group by category/country, prioritize Indonesia first, then popular ones
        val groups = ChannelRepository.channels.groupBy { it.group.ifEmpty { "Worldwide" } }
        val sortedList = groups.toList().sortedWith(compareByDescending { 
            if (it.first.equals("Indonesia", ignoreCase = true) || it.first.equals("ID", ignoreCase = true)) 100000 
            else it.second.size 
        })

        categoryRecycler.adapter = CategoryRowAdapter(sortedList) { channel ->
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra("name", channel.name)
                putExtra("url", channel.url)
            }
            startActivity(intent)
        }
    }
}
