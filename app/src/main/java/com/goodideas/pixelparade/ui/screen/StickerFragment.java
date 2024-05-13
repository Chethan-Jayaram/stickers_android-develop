package com.goodideas.pixelparade.ui.screen;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.goodideas.pixelparade.R;
import com.goodideas.pixelparade.Utils;
import com.goodideas.pixelparade.data.analityc.AnalyticHelper;
import com.goodideas.pixelparade.data.analityc.event.QuickShare;
import com.goodideas.pixelparade.ui.MainActivity;
import com.goodideas.pixelparade.ui.adapters.ShareAppsAdapter;
import com.goodideas.pixelparade.ui.model.StickerAnalyticData;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StickerFragment extends Fragment implements ShareAppsAdapter.OnShareAppSelectedListener {
    @BindView(R.id.iv_sticker)
    ImageView ivSticker;
    @BindView(R.id.share_layout)
    LinearLayout llShare;
    @BindView(R.id.tv_share_caption)
    TextView tvCaption;
    @BindView(R.id.rv_share_apps)
    RecyclerView rvShareApps;

    public static String DOWNLOAD_URL = "url";
    public static String STICKER_ANALYTIC_DATA = "sticker_analytic_data";

    private static StickerFragment instance = null;
    private String downloadUrl;
    private StickerAnalyticData stickerAnalyticData;
    private String sharePath;
    private String imgInCachePath;
    private BottomSheetBehavior behavior;
    private String packageName;

    public static StickerFragment newInstance(String stickerDownloadUrl, StickerAnalyticData stickerAnalyticData) {
        StickerFragment fragment = new StickerFragment();

        Bundle state = new Bundle();
        state.putString(DOWNLOAD_URL, stickerDownloadUrl);
        state.putSerializable(STICKER_ANALYTIC_DATA, stickerAnalyticData);
        fragment.setArguments(state);

        return fragment;
    }

    public static StickerFragment getInstance() {
        return instance;
    }

    public LinearLayout getBottomSheetLayout() {
        return llShare;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sticker, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    public ImageView getImageView() {
        return ivSticker;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        instance = this;

        if (savedInstanceState != null) {
            downloadUrl = savedInstanceState.getString(DOWNLOAD_URL);
            stickerAnalyticData = (StickerAnalyticData) savedInstanceState.getSerializable(STICKER_ANALYTIC_DATA);
        } else {
            downloadUrl = getArguments().getString(DOWNLOAD_URL);
            stickerAnalyticData = (StickerAnalyticData) getArguments().getSerializable(STICKER_ANALYTIC_DATA);
        }

        loadOrFindStickerInCache();

        behavior = BottomSheetBehavior.from(llShare);
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        if (downloadUrl.contains(".gif")) {
            Glide.with(this)
                    .asGif()
                    .load(downloadUrl)
                    .into(ivSticker);
        } else {
            Glide.with(this)
                    .load(downloadUrl)
                    .into(ivSticker);

        }

        int itemsInRow = getActivity().getResources().getSystem().getDisplayMetrics().widthPixels / getActivity().getResources().getDimensionPixelSize(R.dimen.share_app_item_width);

        rvShareApps.setLayoutManager(new GridLayoutManager(getActivity(), itemsInRow));
        rvShareApps.setHasFixedSize(true);
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        if (downloadUrl.contains(".gif")) {
            share.setType("image/*");
        } else {
            share.setType("image/png");
        }
        List<ResolveInfo> resInfo = getActivity().getPackageManager().queryIntentActivities(share, 0);
        ShareAppsAdapter adapter = new ShareAppsAdapter(Utils.prepareAppsList(getActivity(), resInfo));
        adapter.setOnShareAppSelectedListener(this);
        rvShareApps.setAdapter(adapter);
    }

    private void loadOrFindStickerInCache() {
        Glide.with(this)
                .asFile()
                .load(downloadUrl)
                .into(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                        imgInCachePath = resource.getPath();
                        doShare();
                    }
                });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(DOWNLOAD_URL, downloadUrl);
        outState.putSerializable(STICKER_ANALYTIC_DATA, stickerAnalyticData);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setTabsVisibility(false);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setHideable(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!TextUtils.isEmpty(sharePath)) {
            new File(sharePath).delete();
        }
        instance = null;
    }

    @Override
    public void onShareAppSelected(ResolveInfo info) {
        packageName = info.activityInfo.packageName;
        if (!TextUtils.isEmpty(downloadUrl) && imgInCachePath != null) {
            ((MainActivity) getActivity()).permissionBinder.activePermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    getString(R.string.permission_write_external_storage_rationale),
                    getString(R.string.permission_write_external_storage_button),
                    isGranted -> {
                        if (isGranted) {
                            doShare();
                        }
                        return null;
                    });
        }
    }

    public void doShare() {
        Intent share = new Intent(Intent.ACTION_SEND);
        boolean isGif = downloadUrl.contains(".gif");
        if (isGif) {
            share.setType("image/gif");
        } else {
            share.setType("image/png");
        }
        int index = downloadUrl.lastIndexOf("/");
        String fileToSaveName = downloadUrl.substring(index + 1);
        String filePath = requireContext().getExternalFilesDir(null).getAbsolutePath()+"/"+fileToSaveName;
        sharePath = Utils.prepareToShare(imgInCachePath, filePath);
        if (!TextUtils.isEmpty(sharePath)) {
            if (Build.VERSION.SDK_INT >= 24) {
                share.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                        getActivity(),
                        getActivity().getApplicationContext()
                                .getPackageName() + ".provider", new File(sharePath)));
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(sharePath)));
            }
            share.setPackage(packageName);
            /*AnalyticHelper.Companion.sendEvent(new QuickShare(
                    stickerAnalyticData.getPackName(),
                    stickerAnalyticData.getPackSize(),
                    stickerAnalyticData.getStickerNumberInPack(),
                    AnalyticHelper.Companion.getStickerTypeByFileName(fileToSaveName),
                    packageName
            ));*/
             startActivity(Intent.createChooser(share, "Select"));

        }
        /*int index = downloadUrl.lastIndexOf("/");
        String fileToSaveName = downloadUrl.substring(index + 1);
        String filePath = requireContext().getExternalFilesDir(null).getAbsolutePath()+"/"+fileToSaveName;
        Intent shareIntent = new Intent();

        boolean isGif = downloadUrl.contains(".gif");
        if (isGif) {
            shareIntent.setType("image/gif");
        } else {
            shareIntent.setType("image/png");
        }
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(new File(fil)) );
        startActivity(Intent.createChooser(shareIntent, "Select App"));*/
    }

}
