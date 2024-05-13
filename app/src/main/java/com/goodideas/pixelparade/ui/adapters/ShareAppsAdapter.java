package com.goodideas.pixelparade.ui.adapters;

import android.content.pm.ResolveInfo;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.goodideas.pixelparade.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShareAppsAdapter extends RecyclerView.Adapter<ShareAppsAdapter.ShareVH> {
    private ArrayList<ResolveInfo> appInfoList;

    private OnShareAppSelectedListener mListener;

    public ShareAppsAdapter(List<ResolveInfo> appList) {
        appInfoList = new ArrayList<>();
        appInfoList.addAll(appList);
    }

    public void setOnShareAppSelectedListener(OnShareAppSelectedListener listener) {
        this.mListener = listener;
    }

    @Override
    public ShareVH onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ShareVH(inflater.inflate(R.layout.share_app_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ShareVH holder, final int position) {
        holder.position = position;
        holder.appIcon.setImageDrawable(holder.itemView.getContext().getPackageManager().getApplicationIcon(appInfoList.get(position).activityInfo.applicationInfo));
        holder.appName.setText(appInfoList.get(position).activityInfo.applicationInfo.loadLabel(holder.itemView.getContext().getPackageManager()));
        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onShareAppSelected(appInfoList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return appInfoList.size();
    }

    class ShareVH extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_app_icon)
        ImageView appIcon;
        @BindView(R.id.tv_app_name)
        TextView appName;

        int position;

        public ShareVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface OnShareAppSelectedListener {
        public void onShareAppSelected(ResolveInfo info);
    }
}
