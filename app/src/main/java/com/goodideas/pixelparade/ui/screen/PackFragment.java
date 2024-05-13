package com.goodideas.pixelparade.ui.screen;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.goodideas.pixelparade.R;
import com.goodideas.pixelparade.SharedPreferencesHelper;
import com.goodideas.pixelparade.Utils;
import com.goodideas.pixelparade.data.ApiClient;
import com.goodideas.pixelparade.data.analityc.AnalyticHelper;
import com.goodideas.pixelparade.data.analityc.event.LinkClicked;
import com.goodideas.pixelparade.data.analityc.event.StickerOpened;
import com.goodideas.pixelparade.ext.TextViewExtKt;
import com.goodideas.pixelparade.ui.MainActivity;
import com.goodideas.pixelparade.ui.adapters.StickersAdapter;
import com.goodideas.pixelparade.ui.model.StickerAnalyticData;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;

public class PackFragment extends Fragment implements StickersAdapter.OnStickerSelectedListener {
    @BindView(R.id.tv_pack_name)
    TextView tvPackName;
    @BindView(R.id.tv_tags)
    TextView tvTags;
    @BindView(R.id.rv_stickers)
    RecyclerView rvStickers;

    public static String PACK = "pack";

    private String dirName;
    private ApiClient.StickersPackJSON stickersPack;

    public static PackFragment newInstance(ApiClient.StickersPackJSON stickersPack) {
        PackFragment fragment = new PackFragment();

        Bundle state = new Bundle();
        state.putParcelable(PACK, stickersPack);
        fragment.setArguments(state);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pack, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            stickersPack = savedInstanceState.getParcelable(PACK);
        } else {
            stickersPack = getArguments().getParcelable(PACK);
        }
        dirName = stickersPack.getName();
        String tags = stickersPack.getTags();

        tvPackName.setText(dirName);

        tvTags.setLinksClickable(true);
        tvTags.setMovementMethod(new LinkMovementMethod());
        TextViewExtKt.setHtml(tvTags, tags, url -> {
            AnalyticHelper.Companion.sendEvent(new LinkClicked(
                    stickersPack.getName(),
                    stickersPack.getQuantity(),
                    url
            ));
            return Unit.INSTANCE;
        });

        final int itemsInRow = getActivity().getResources().getSystem().getDisplayMetrics().widthPixels / getActivity().getResources().getDimensionPixelSize(R.dimen.default_sticker_width);

        rvStickers.setLayoutManager(new GridLayoutManager(getActivity(), itemsInRow));
        if (stickersPack.getQuantity() > stickersPack.getStickers().size()) {
            if (stickersPack.getPrice() > 0.001f) {
                ApiClient.getInstance(getActivity()).getApiService().purchaseStickerPack(SharedPreferencesHelper.getUserToken(getActivity()), stickersPack.getId(),
                        SharedPreferencesHelper.getPurchaseTokens(getActivity()).get(stickersPack.getId()), "android")
                        .subscribeOn(Schedulers.io())
                        .flatMap((result) -> ApiClient.getInstance(getActivity()).getApiService().getStickerPackById(SharedPreferencesHelper.getUserToken(getActivity()), stickersPack.getId()))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(stickers -> {
                            stickersPack.setStickers(stickers);
                            stickersPack.setQuantity(stickers.size());
                            createAdapter(itemsInRow);
                        }, t -> {
                        });
            } else {
                ApiClient.getInstance(getActivity()).getApiService().getStickerPackById(SharedPreferencesHelper.getUserToken(getActivity()), stickersPack.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(stickers -> {
                            stickersPack.setStickers(stickers);
                            stickersPack.setQuantity(stickers.size());
                            createAdapter(itemsInRow);
                        }, t -> {
                        });
            }
        } else {
            createAdapter(itemsInRow);
        }
    }

    private void createAdapter(int itemsInRow) {
        StickersAdapter adapter = new StickersAdapter(dirName, stickersPack, false);
        adapter.setOnStickerSelectedListener(this);
        adapter.setItemsInRow(itemsInRow);
        adapter.setFirstVisibleItem(0);
        adapter.setLastVisibleItem(stickersPack.getStickers().size());
        rvStickers.setAdapter(adapter);
        rvStickers.setNestedScrollingEnabled(false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(PACK, stickersPack);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setTabsVisibility(true);
    }

    @Override
    public void onStickerSelected(int currentPosition) {
        String fileName = stickersPack.getStickers().get(currentPosition).getFilename();
        AnalyticHelper.Companion.sendEvent(new StickerOpened(
                stickersPack.getName(),
                stickersPack.getQuantity(),
                currentPosition,
                AnalyticHelper.Companion.getStickerTypeByFileName(fileName)
        ));

        final String downloadUrl = Utils.getStickerDownloadURL(fileName);
        StickerAnalyticData stickerAnalyticData = new StickerAnalyticData(
                stickersPack.getName(),
                stickersPack.getQuantity(),
                currentPosition
        );
        StickerFragment fragment = StickerFragment.newInstance(downloadUrl, stickerAnalyticData);
        int index = fileName.lastIndexOf("/");
        String name = fileName.substring(index + 1, fileName.length());
        SharedPreferencesHelper.addSticker(getActivity(), dirName, name);
        ((MainActivity) getActivity()).addFragment(fragment);
    }
}
