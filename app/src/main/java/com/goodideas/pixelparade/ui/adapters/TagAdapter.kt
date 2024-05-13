package com.goodideas.pixelparade.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goodideas.pixelparade.R
import kotlinx.android.synthetic.main.item_tag.view.*

class TagAdapter(
    private val onTagClick: ((text: String) -> Unit)? = null
) : RecyclerView.Adapter<TagAdapter.TagVH>() {

    var tags: MutableList<String> = mutableListOf()

    fun setAll(tags: List<String>) {
        this.tags = tags.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagVH {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_tag, parent, false)
        return TagVH(view)
    }

    override fun onBindViewHolder(holder: TagVH, position: Int) {
        holder.setText(tags[position])
        holder.setOnTagClickListener { onTagClick?.invoke(tags[position]) }
    }

    override fun getItemCount() = tags.size

    class TagVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun setText(text: String) {
            itemView.tag_chip.text = text
        }

        fun setOnTagClickListener(onTagClick: () -> Unit) {
            itemView.tag_chip.setOnClickListener { onTagClick.invoke() }
        }
    }
}
