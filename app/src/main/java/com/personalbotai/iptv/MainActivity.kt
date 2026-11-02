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
                    v.setBackgroundColor(0xFFE50914.toInt()) // Netflix Red focus
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
            textSize = 16f
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

class CategoryRowAdapter(private val categories: Map<String, List<Channel>>, private val onChannelClick: (Channel) -> Unit) :
    RecyclerView.Adapter<CategoryRowAdapter.RowViewHolder>() {

    val keys = categories.keys.toList()

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
                setMargins(0, (16 * dp).toInt(), 0, (16 * dp).toInt())
            }
        }

        val title = TextView(context).apply {
            setTextColor(Color.WHITE)
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setPadding((40 * dp).toInt(), 0, 0, (12 * dp).toInt())
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
        val cat = keys[position]
        val list = categories[cat].orEmpty()
        holder.title.text = cat
        holder.recycler.adapter = ChannelAdapter(list, onChannelClick)
    }

    override fun getItemCount() = keys.size
}

class MainActivity : Activity() {

    private lateinit var mainContainer: LinearLayout
    private lateinit var headerTitle: TextView
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

        // Netflix Header bar
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding((40 * dp).toInt(), (24 * dp).toInt(), (40 * dp).toInt(), (16 * dp).toInt())
            gravity = Gravity.CENTER_VERTICAL
        }

        headerTitle = TextView(this).apply {
            text = "NETFLIX IPTV"
            setTextColor(0xFFE50914.toInt())
            textSize = 28f
            setTypeface(null, Typeface.BOLD)
        }
        header.addView(headerTitle)

        val subtitle = TextView(this).apply {
            text = "  |  Live TV Worldwide"
            setTextColor(0xFFB3B3B3.toInt())
            textSize = 16f
        }
        header.addView(subtitle)

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

        loadData()
        ChannelRepository.onLoaded = {
            loadData()
        }
    }

    private fun loadData() {
        val grouped = ChannelRepository.channels.groupBy { it.group.ifEmpty { "General" } }
        categoryRecycler.adapter = CategoryRowAdapter(grouped) { channel ->
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra("name", channel.name)
                putExtra("url", channel.url)
            }
            startActivity(intent)
        }
    }
}
