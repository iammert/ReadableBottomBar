package com.iammert.readablebottombar

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PageAdapter(private val titles: List<String>) : RecyclerView.Adapter<PageAdapter.PageViewHolder>() {

    class PageViewHolder(val title: TextView) : RecyclerView.ViewHolder(title)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_page, parent, false) as TextView
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.title.text = holder.title.context.getString(R.string.selected_page, titles[position])
    }

    override fun getItemCount(): Int = titles.size
}
