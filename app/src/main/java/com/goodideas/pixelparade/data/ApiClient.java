package com.goodideas.pixelparade.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.PurchaseInfo;
import com.goodideas.pixelparade.BuildConfig;
import com.goodideas.pixelparade.SharedPreferencesHelper;
import com.goodideas.pixelparade.data.analityc.AnalyticHelper;
import com.goodideas.pixelparade.data.analityc.event.PackDownloaded;
import com.goodideas.pixelparade.ui.MainActivity;
import com.goodideas.pixelparade.ui.adapters.StickerPacksAdapter;
import com.goodideas.pixelparade.ui.screen.AllLoadedFragment;
import com.goodideas.pixelparade.ui.screen.MainFragment;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.readystatesoftware.chuck.ChuckInterceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import timber.log.Timber;

public class ApiClient implements BillingProcessor.IBillingHandler {
    private static final Handler mHandler = new Handler(Looper.getMainLooper());

    private static ApiClient instance = null;
    private final ApiService mApiService;


    private static final String LICENSE_KEY = "";
    private static final String MERCHANT_ID = "";
    private final BillingProcessor bp;

    private ArrayList<StickersPackJSON> packsToBuy;
    private ArrayList<StickersPackJSON> allPacks = new ArrayList<>();
    private ArrayList<StickersPackJSON> installedPacks;
    private int selectedTab = -1;
    private int previousSelectedTab = -1;

    public static ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
        return instance;
    }

    public ApiClient(Context context) {
        //HttpLoggingInterceptor httpLoggingInterceptor =
        //        new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                //.addInterceptor(httpLoggingInterceptor)
                .addInterceptor(new ChuckInterceptor(context))
                .build();
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        Retrofit retrofitApis = new Retrofit.Builder()
                .baseUrl(BuildConfig.ENDPOINT)
                .client(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        mApiService = retrofitApis.create(ApiService.class);

        packsToBuy = new ArrayList<>();
        installedPacks = new ArrayList<>();

        bp = new BillingProcessor(context, LICENSE_KEY, MERCHANT_ID, this);
    }

    public ApiService getApiService() {
        return mApiService;
    }

    public ArrayList<StickersPackJSON> getPacksToBuy() {
        return packsToBuy;
    }

    public void setPacksToBuy(List<StickersPackJSON> packs) {
        packsToBuy = new ArrayList<>(packs);
    }

    public ArrayList<StickersPackJSON> getAllPacks() {
        return allPacks;
    }

    public void setAllPacks(ArrayList<StickersPackJSON> allPacks) {
        this.allPacks.addAll(allPacks);
    }

    public ArrayList<StickersPackJSON> getInstalledPacks() {
        return installedPacks;
    }

    public void setInstalledPacks(List<StickersPackJSON> packs) {
        installedPacks = new ArrayList<>(packs);
    }

    public int getSelectedTab() {
        return selectedTab;
    }

    public void setSelectedTab(int selectedTab) {
        this.previousSelectedTab = this.selectedTab;
        this.selectedTab = selectedTab;
    }

    public int getPreviousSelectedTab() {
        return previousSelectedTab;
    }

    public Observable<Boolean> setPurchasedPackOnServer(String purchaseToken, int packId) {
        return mApiService.purchaseStickerPack(
                SharedPreferencesHelper.getUserToken(MainActivity.getInstance()),
                packId,
                purchaseToken,
                "android"
        );
    }

    public BillingProcessor getBillingProcessor() {
        return bp;
    }

    // in-app billing
    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable PurchaseInfo details) {
        int pos = productId.lastIndexOf("_");
        int id = Integer.valueOf(productId.substring(pos + 1, productId.length()));
        int index = -1;
        for (int i = 0; i < getPacksToBuy().size(); i++) {
            if (getPacksToBuy().get(i).getId() == id) {
                index = i;
                break;
            }
        }

        final int finalIndex = index;

        mHandler.post(() -> {
            if (finalIndex != -1) {
                SharedPreferencesHelper.addPurchaseToken(MainActivity.getInstance(), getPacksToBuy().get(finalIndex).getId(),
                        details.purchaseData.purchaseToken);
                mApiService.purchaseStickerPack(SharedPreferencesHelper.getUserToken(MainActivity.getInstance()), getPacksToBuy().get(finalIndex).getId(),
                        details.purchaseData.purchaseToken, "android");
                AnalyticHelper.Companion.sendEvent(new PackDownloaded(
                        getPacksToBuy().get(finalIndex).getName(),
                        getPacksToBuy().get(finalIndex).getQuantity(),
                        Math.round(getPacksToBuy().get(finalIndex).getPrice())
                ));

                MainFragment.getInstance().getStickerPacksAdapter().switchToPack(finalIndex);
            }

            setAllAddedFragmentIfAllPacksBought();
        });
    }

    private void setAllAddedFragmentIfAllPacksBought() {
        if (packsToBuy.size() == 0)
            ((MainActivity) MainFragment.getInstance()
                    .getActivity()).replaceFragment(new AllLoadedFragment());
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {
        Timber.e(error, "onBillingError: %d", errorCode);
        mHandler.post(() -> {
            MainFragment fragment = MainFragment.getInstance();

            if(fragment == null)
                return;

            StickerPacksAdapter adapter = fragment.getStickerPacksAdapter();
            adapter.clearLoadingFlag();
        });
    }

    @Override
    public void onBillingInitialized() {
    }

    public static class Sticker implements Parcelable {
        @SerializedName("id")
        private int id;
        @SerializedName("stickerpack_id")
        private int packid;
        @SerializedName("filename")
        private String filename;
        @SerializedName("position")
        private int position;

        public Sticker() {
        }

        protected Sticker(Parcel in) {
            id = in.readInt();
            packid = in.readInt();
            filename = in.readString();
            position = in.readInt();
        }

        public static final Creator<Sticker> CREATOR = new Creator<Sticker>() {
            @Override
            public Sticker createFromParcel(Parcel in) {
                return new Sticker(in);
            }

            @Override
            public Sticker[] newArray(int size) {
                return new Sticker[size];
            }
        };

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getPackId() {
            return packid;
        }

        public void setPackId(int packid) {
            this.packid = packid;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(id);
            dest.writeInt(packid);
            dest.writeString(filename);
            dest.writeInt(position);
        }
    }

    public static class StickersPackJSON implements Parcelable {
        @SerializedName("id")
        private int id;
        @SerializedName("name")
        private String name;
        @SerializedName("description")
        private String tags;
        @SerializedName("price")
        private float price;
        @SerializedName("stickers")
        List<Sticker> stickers;
        @SerializedName("quantity")
        private int quantity;

        public StickersPackJSON() {
        }

        protected StickersPackJSON(Parcel in) {
            id = in.readInt();
            name = in.readString();
            tags = in.readString();
            price = in.readFloat();
            stickers = in.createTypedArrayList(Sticker.CREATOR);
            quantity = in.readInt();
        }

        public static final Creator<StickersPackJSON> CREATOR = new Creator<StickersPackJSON>() {
            @Override
            public StickersPackJSON createFromParcel(Parcel in) {
                return new StickersPackJSON(in);
            }

            @Override
            public StickersPackJSON[] newArray(int size) {
                return new StickersPackJSON[size];
            }
        };

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTags() {
            return tags;
        }

        public void setTags(String tags) {
            this.tags = tags;
        }

        public float getPrice() {
            return price;
        }

        public void setPrice(float price) {
            this.price = price;
        }

        public List<Sticker> getStickers() {
            return stickers;
        }

        public void setStickers(List<Sticker> stickers) {
            this.stickers = stickers;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(id);
            dest.writeString(name);
            dest.writeString(tags);
            dest.writeFloat(price);
            dest.writeTypedList(stickers);
            dest.writeInt(quantity);
        }
    }

    public class EmailResponse {
        @SerializedName("email")
        private String email;
        @SerializedName("id")
        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class BannerResponse {
        @SerializedName("banner")
        @Nullable
        private BannerDetailResponse banner;

        @Nullable
        public BannerDetailResponse getBanner() {
            return banner;
        }

        public void setBanner(@Nullable BannerDetailResponse banner) {
            this.banner = banner;
        }
    }

    public static class BannerDetailResponse {
        @SerializedName("id")
        private int id;

        @SerializedName("image")
        private String image;

        @SerializedName("url")
        private String url;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }


    public StickersPackJSON createPackFromPref(int packId, ArrayList<String> list) {
        StickersPackJSON pack = new StickersPackJSON();
        ArrayList<Sticker> stickers = new ArrayList<>();
        for (String name : list) {
            Sticker sticker = new Sticker();
            sticker.setFilename(String.valueOf(packId) + "/" + name);
            stickers.add(sticker);
        }
        pack.setStickers(stickers);
        pack.setQuantity(stickers.size());
        return pack;
    }

    public StickersPackJSON createInstalledPacksList(Context context) {
        ArrayList<String> names = SharedPreferencesHelper.getBoughtAndDownloadedPacks(context);
        ArrayList<Sticker> stickers = new ArrayList<>();
        for (String name : names) {
            StickersPackJSON pack = null;
            for (StickersPackJSON stickerpack : installedPacks) {
                if (stickerpack.getName().compareTo(name) == 0) {
                    pack = stickerpack;
                    break;
                }
            }
            if (pack != null) {
                stickers.add(pack.getStickers().get(0));
            }
        }
        StickersPackJSON newpack = new StickersPackJSON();
        newpack.setName("packs");
        newpack.setStickers(stickers);
        newpack.setQuantity(stickers.size());
        return newpack;
    }
}
