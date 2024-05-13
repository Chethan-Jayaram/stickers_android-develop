package com.goodideas.pixelparade.ui.adapters;

import android.graphics.Color;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.goodideas.pixelparade.R;
import com.goodideas.pixelparade.Utils;
import com.goodideas.pixelparade.data.ApiClient;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StickersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int TYPE_STICKER = 1;
    public static final int TYPE_POINTER = 4;

    private ApiClient.StickersPackJSON stickersPack;
    private boolean hasEndPointer;
    private boolean smallItems;

    private RecyclerView mRecyclerView;
    private int itemsInRow;
    private int firstVisibleItem;
    private int lastVisibleItem;
    private RequestManager glide;

    private OnStickerSelectedListener mListener = null;

    public StickersAdapter(String dirName, ApiClient.StickersPackJSON stickerPack, boolean hasEndPointer) {
        this.stickersPack = stickerPack;
        this.hasEndPointer = hasEndPointer;
        this.smallItems = false;
    }

    public StickersAdapter(String dirName, ApiClient.StickersPackJSON stickerPack, boolean hasEndPointer, boolean smallItems) {
        this.stickersPack = stickerPack;
        this.hasEndPointer = hasEndPointer;
        this.smallItems = smallItems;
    }

    public void setStickersPack(ApiClient.StickersPackJSON stickersPack) {
        this.stickersPack = stickersPack;
        notifyDataSetChanged();
    }

    public void setItemsInRow(int itemsInRow) {
        this.itemsInRow = itemsInRow;
    }

    public int getItemsInRow() {
        return itemsInRow;
    }

    public void setFirstVisibleItem(int pos) {
        firstVisibleItem = pos;
    }

    public int getFirstVisibleItem() {
        return firstVisibleItem;
    }

    public void setLastVisibleItem(int pos) {
        lastVisibleItem = pos;
    }

    public int getLastVisibleItem() {
        return lastVisibleItem;
    }

    public ApiClient.Sticker getSticker(int position) {
        return stickersPack.getStickers().get(position);
    }

    public void setOnStickerSelectedListener(OnStickerSelectedListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_POINTER) {
            View view = inflater.inflate(R.layout.item_sticker_pointer, parent, false);
            view.setMinimumHeight(parent.getContext().getResources().getDimensionPixelSize(R.dimen.default_sticker_height));
            return new EmptyVH(view);
        } else if (smallItems) {
            View view = inflater.inflate(R.layout.item_sticker_small, parent, false);
            view.setMinimumHeight(parent.getContext().getResources().getDimensionPixelSize(R.dimen.small_sticker_height));
            return new VH(view);
        } else {
            View view = inflater.inflate(R.layout.item_sticker, parent, false);
            view.setMinimumHeight(parent.getContext().getResources().getDimensionPixelSize(R.dimen.default_sticker_height));
            return new VH(view);
        }

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (getItemViewType(position) != TYPE_POINTER) {
            if (smallItems) {
                if (position == ApiClient.getInstance(holder.itemView.getContext()).getSelectedTab()) {
                    holder.itemView.setBackgroundResource(R.drawable.img_plus_bg);
                } else {
                    holder.itemView.setBackgroundColor(Color.WHITE);
                }
            }
            String url = Utils.getStickerDownloadURL(stickersPack.getStickers().get(position).getFilename());
            if (!(mRecyclerView.getLayoutManager() instanceof GridLayoutManager)) {
                ((FrameLayout) holder.itemView).setForeground(null);
            }
            glide = Glide.with(mRecyclerView);
            if (url.contains("gif")) glide.asGif();
                glide.load(url)
                    .override(((VH) holder).ivSticker.getWidth(), ((VH) holder).ivSticker.getHeight())
                    .apply(new RequestOptions().placeholder(R.drawable.placeholder))
                    .into(((VH) holder).ivSticker);
        }

        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onStickerSelected(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return stickersPack.getStickers().size() + ((hasEndPointer) ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        if (hasEndPointer && position == stickersPack.getStickers().size()) {
            return TYPE_POINTER;
        } else
            return TYPE_STICKER;
    }

    public static class VH extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_gif_sticker)
        public ImageView ivSticker;

        public VH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class EmptyVH extends RecyclerView.ViewHolder {
        public EmptyVH(View itemView) {
            super(itemView);
        }
    }

    public interface OnStickerSelectedListener {
        void onStickerSelected(int position);
    }
}
