package com.goodideas.pixelparade.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.goodideas.pixelparade.BuildConfig;
import com.goodideas.pixelparade.R;
import com.goodideas.pixelparade.SharedPreferencesHelper;
import com.goodideas.pixelparade.data.ApiClient;
import com.goodideas.pixelparade.data.analityc.AnalyticHelper;
import com.goodideas.pixelparade.data.analityc.event.BuyPack;
import com.goodideas.pixelparade.data.analityc.event.EmailClosed;
import com.goodideas.pixelparade.data.analityc.event.EmailPopup;
import com.goodideas.pixelparade.data.analityc.event.GetPack;
import com.goodideas.pixelparade.data.analityc.event.LinkClicked;
import com.goodideas.pixelparade.data.analityc.event.PackDownloaded;
import com.goodideas.pixelparade.ext.TextViewExtKt;
import com.goodideas.pixelparade.ui.MainActivity;
import com.goodideas.pixelparade.ui.screen.AllLoadedFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;

public class StickerPacksAdapter extends RecyclerView.Adapter<StickerPacksAdapter.VH> {
    private WeakReference<Context> mContext;
    private boolean isLoading;
    private ArrayList<ApiClient.StickersPackJSON> savedStickers;
    private final int TYPE_BANNER = 1;
    private final int TYPE_PACK = 2;

    public StickerPacksAdapter(Context context) {
        this.mContext = new WeakReference<>(context);
        savedStickers = new ArrayList<ApiClient.StickersPackJSON>(
                ApiClient.getInstance(mContext.get()).getPacksToBuy()
        );
        this.isLoading = false;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_BANNER;
        } else {
            return TYPE_PACK;
        }
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_BANNER) {
            return new BannerViewHolder(inflater.inflate(R.layout.sticker_pack_banner_item, parent, false));
        }
        return new PackViewHolder(inflater.inflate(R.layout.sticker_pack_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final VH holder, int position) {
        holder.bindTo(position);
    }

    public void replacePacks(ArrayList<ApiClient.StickersPackJSON> packs) {
        savedStickers.clear();
        savedStickers.addAll(packs);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return savedStickers.size() + 1;
    }

    public void clearLoadingFlag() {
        isLoading = false;
    }

    public abstract class VH extends RecyclerView.ViewHolder {

        public VH(@NonNull View itemView) {
            super(itemView);
        }

        public abstract void bindTo(int position);
    }

    public class BannerViewHolder extends VH {
        @BindView(R.id.main_banner)
        ImageView mainBanner;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindTo(int position) {
            try {
                ApiClient.getInstance(itemView.getContext()).getApiService().getBanner()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(bannerResponse -> {
                            if (bannerResponse != null) {
                                ApiClient.BannerDetailResponse bannerData = bannerResponse.getBanner();
                                RequestManager glide = Glide.with(itemView);
                                if (bannerData.getImage().contains("gif")) glide.asGif();
                                glide.load(BuildConfig.BASE_URL + bannerData.getImage()).into(mainBanner);
                                mainBanner.setVisibility(View.VISIBLE);
                                mainBanner.setOnClickListener(view -> {
                                    try {
                                        mainBanner.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(bannerData.getUrl())));
                                    } catch (Exception ignored) {
                                    }
                                });
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class PackViewHolder extends VH {
        @BindView(R.id.tv_stickers_pack_name)
        TextView tvPackName;
        @BindView(R.id.tv_stickers_count)
        TextView tvCount;
        @BindView(R.id.tv_stickers_pack_tags)
        TextView tvTags;
        @BindView(R.id.btn_buy)
        Button btnBuy;
        @BindView(R.id.horizontal_list)
        public RecyclerView rvStickers;

        private int stickerPosition;

        @Override
        public void bindTo(int position) {
            stickerPosition = position - 1;
            tvPackName.setText(savedStickers.get(stickerPosition).getName());
            tvTags.setLinksClickable(true);
            tvTags.setMovementMethod(new LinkMovementMethod());
            TextViewExtKt.setHtml(tvTags, savedStickers.get(stickerPosition).getTags(), url -> {
                AnalyticHelper.Companion.sendEvent(new LinkClicked(
                        savedStickers.get(stickerPosition).getName(),
                        savedStickers.get(stickerPosition).getQuantity(),
                        url
                ));
                return Unit.INSTANCE;
            });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btnBuy.setBackgroundResource(R.drawable.btn_v21);
            } else {
                btnBuy.setBackgroundResource(R.drawable.btn_rounded);
            }
            String btnBuyText = "GET IT";
            if (savedStickers.get(stickerPosition).getPrice() > 0f) {
                int price100 = (int) Math.round(savedStickers.get(stickerPosition).getPrice() * 100);
                String val = String.valueOf(price100);
                if (val.length() == 1) val = "0" + val;
                btnBuyText = "$ " + ((val.length() == 2) ? "0" : val.substring(0, val.length() - 2)) + "." + val.substring(val.length() - 2, val.length());
            }
            btnBuy.setText(btnBuyText);

            tvCount.setText(savedStickers.get(stickerPosition).getQuantity() + " stickers");

            StickersAdapter adapter = new StickersAdapter(savedStickers.get(stickerPosition).getName(), savedStickers.get(stickerPosition), true);
            adapter.setFirstVisibleItem(0);
            adapter.setLastVisibleItem(savedStickers.get(stickerPosition).getStickers().size());
            adapter.setItemsInRow(1);
            rvStickers.setAdapter(adapter);
        }

        @OnClick(R.id.btn_buy)
        void buyBtnClick() {
            if (mContext == null || mContext.get() == null || isLoading) return;
            if (this.stickerPosition < 0 || this.stickerPosition >= savedStickers.size() + 1)
                return;
            isLoading = true;
            if (savedStickers.get(stickerPosition).getPrice() < 0.001f) {
                AnalyticHelper.Companion.sendEvent(new GetPack(
                        savedStickers.get(stickerPosition).getName(),
                        savedStickers.get(stickerPosition).getQuantity()
                ));
                if (!TextUtils.isEmpty(SharedPreferencesHelper.getUserEmail(mContext.get()))) {
                    fakePurchase(stickerPosition);
                } else {
                    showEmailDialog();
                }
            } else {
                AnalyticHelper.Companion.sendEvent(new BuyPack(
                        savedStickers.get(stickerPosition).getName(),
                        savedStickers.get(stickerPosition).getQuantity(),
                        Math.round(savedStickers.get(stickerPosition).getPrice())
                ));
                ApiClient.getInstance(mContext.get())
                        .getBillingProcessor()
                        .purchase((MainActivity) mContext.get(), "com.ltst.pixelparade.stickerpacks_" + savedStickers.get(stickerPosition).getId());
            }
        }

        public PackViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void showEmailDialog() {
            AnalyticHelper.Companion.sendEvent(new EmailPopup());
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this.itemView.getContext());
            LayoutInflater inflater = ((MainActivity) this.itemView.getContext()).getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.dialog_enter_email_view, null);
            dialogBuilder.setView(dialogView);

            final EditText etEmail = (EditText) dialogView.findViewById(R.id.edit1);

            dialogBuilder.setTitle(R.string.get_more);
            dialogBuilder.setMessage(R.string.get_more_text);
            dialogBuilder.setPositiveButton("OK", (dialog1, whichButton) -> {
                AnalyticHelper.Companion.sendEvent(new EmailClosed(true));
                String email = etEmail.getText().toString();
                if (!TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    SharedPreferencesHelper.setUserEmail(mContext.get(), email);
                    dialog1.dismiss();
                    dialog1 = null;
                    ApiClient.getInstance(mContext.get()).getApiService().registerEmail(SharedPreferencesHelper.getUserToken(mContext.get()), email)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe((emailResponse) -> {
                                if (savedStickers.get(stickerPosition).getPrice() < 0.001f) {
                                    fakePurchase(stickerPosition);
                                } else {
                                    ApiClient.getInstance(mContext.get()).getBillingProcessor().purchase((MainActivity) mContext.get(), "com.ltst.pixelparade.stickerpacks_" + savedStickers.get(stickerPosition).getId());
                                }
                            }, (throwable) -> {
                            });
                }
            });
            dialogBuilder.setNegativeButton("Cancel", (dialog1, whichButton) -> {
                AnalyticHelper.Companion.sendEvent(new EmailClosed(false));
                dialog1.dismiss();
                dialog1 = null;
                if (savedStickers.get(stickerPosition).getPrice() < 0.001f) {
                    fakePurchase(stickerPosition);
                } else {
                    ApiClient.getInstance(mContext.get()).getBillingProcessor().purchase((MainActivity) mContext.get(), "com.ltst.pixelparade.stickerpacks_" + savedStickers.get(stickerPosition).getId());
                }
            });
            final AlertDialog dialog = dialogBuilder.create();
            etEmail.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    boolean dialogButtonEnabledCondition = !TextUtils.isEmpty(s) && android.util.Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(dialogButtonEnabledCondition);
                }
            });
            dialog.setOnCancelListener((dialog1) -> {
                AnalyticHelper.Companion.sendEvent(new EmailClosed(false));
                if (savedStickers.get(stickerPosition).getPrice() < 0.001f) {
                    fakePurchase(stickerPosition);
                } else {
                    ApiClient.getInstance(mContext.get()).getBillingProcessor().purchase((MainActivity) mContext.get(), "com.ltst.pixelparade.stickerpacks_" + savedStickers.get(stickerPosition).getId());
                }
            });

            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }
    }

    private void fakePurchase(final int position) {
        ApiClient.getInstance(mContext.get()).getApiService().purchaseStickerPack(SharedPreferencesHelper.getUserToken(mContext.get()),
                savedStickers.get(position).getId(), "abc", "android")
                .subscribeOn(Schedulers.io())
                .subscribe(flag -> {
                    ApiClient.getInstance(mContext.get()).getApiService().getStickerPackById(SharedPreferencesHelper.getUserToken(mContext.get()),
                            savedStickers.get(position).getId())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(stickers -> {
                                ApiClient.StickersPackJSON pack = savedStickers.get(position);
                                pack.setStickers(stickers);
                                pack.setQuantity(stickers.size());
                                switchToPack(position);
                                setAllAddedFragmentIfAllPacksBought();
                                AnalyticHelper.Companion.sendEvent(new PackDownloaded(
                                        pack.getName(),
                                        pack.getQuantity(),
                                        0
                                ));
                            }, this::handleError);
                }, this::handleError);
    }

    private void handleError(Throwable t) {
        isLoading = false;
        t.printStackTrace();
    }

    private void setAllAddedFragmentIfAllPacksBought() {
        if (savedStickers.size() == 0)
            ((MainActivity) mContext.get()).replaceFragment(new AllLoadedFragment());
    }

    public void switchToPack(int stickerPosition) {
        ApiClient.StickersPackJSON pack = savedStickers.get(stickerPosition);
        int position = stickerPosition + 1;
        ArrayList<String> names = SharedPreferencesHelper.getBoughtAndDownloadedPacks(mContext.get());
        if (names.indexOf(pack.getName()) >= 0 && names.indexOf(pack.getName()) < names.size()) {
            isLoading = false;
            return;
        }
        SharedPreferencesHelper.addPack(mContext.get(), pack.getName());
        ApiClient.getInstance(mContext.get()).getInstalledPacks().add(pack);
        savedStickers.remove(stickerPosition);
        //DON'T REMOVE THIS
        ApiClient.getInstance(mContext.get()).getPacksToBuy().remove(stickerPosition);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, savedStickers.size() + 1);
        ((MainActivity) mContext.get()).createTabs();
        isLoading = false;
    }
}
