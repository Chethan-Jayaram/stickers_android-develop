package com.goodideas.pixelparade.ui.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.goodideas.pixelparade.R
import com.goodideas.pixelparade.Utils
import com.goodideas.pixelparade.data.ApiClient
import kotlinx.android.synthetic.main.item_sticker_photo.view.*

class StickersOnPhotoAdapter(
        private val onStickerSelectedListener: ((position: Int) -> Unit)? = null
) : RecyclerView.Adapter<StickersOnPhotoAdapter.StickersOnPhotoVH>() {

    var stickers: MutableList<ApiClient.Sticker> = mutableListOf()

    fun setAll(stickers: List<ApiClient.Sticker>) {
        this.stickers.clear()
        stickers.forEach { this.stickers.add(it) }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickersOnPhotoVH {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_sticker_photo, parent, false)
        return StickersOnPhotoVH(view)
    }

    override fun onBindViewHolder(holder: StickersOnPhotoVH, position: Int) {
        val url = Utils.getStickerDownloadURL(stickers[position].filename)

        if (url.contains("gif")) {
            holder.setGif(url)
        } else {
            holder.setImage(url)
        }

        holder.itemView.setOnClickListener { onStickerSelectedListener?.invoke(position) }
    }

    override fun getItemCount() = stickers.size

    class StickersOnPhotoVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun setImage(url: String) {
            Glide.with(itemView)
                    .load(url)
                    .apply(RequestOptions().placeholder(R.drawable.placeholder))
                    .into(itemView.sticker_photo_item_sticker)
        }

        fun setGif(url: String) {
            Glide.with(itemView)
                    .asGif()
                    .load(url)
                    .apply(RequestOptions().placeholder(R.drawable.placeholder))
                    .into(itemView.sticker_photo_item_sticker)
        }
    }
}
