package com.goodideas.pixelparade.data;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.goodideas.pixelparade.Consts;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface ApiService {
    @Headers({
            "Content-type: application/x-www-form-urlencoded"
    })
    @POST(Consts.API_EMAIL)
    @FormUrlEncoded
    Observable<ApiClient.EmailResponse> registerEmail(
            @Header("Authorization") String token,
            @Field("email") String userEmail
    );

    @Streaming
    @GET
    Observable<Response<ResponseBody>> downloadSticker(@Url String fileUrl);

    @GET(Consts.API_CREATE_TOKEN)
    Observable<String> createToken();

    @GET(Consts.API_STICKER_PACK_BY_ID)
    Observable<List<ApiClient.Sticker>> getStickerPackById(@Header("Authorization") String token, @Path("id") int id);

    @GET(Consts.API_STICKER_PACK)
    Observable<List<ApiClient.StickersPackJSON>> getStickerPacksList(@Header("Authorization") String token, @Query("page") int page);

    @GET(Consts.API_KEY_SEARCH_WORDS)
    Observable<List<String>> getApiKeySearchWords(@Header("Authorization") String token);

    @Headers({
            "Content-type: application/x-www-form-urlencoded"
    })
    @FormUrlEncoded
    @POST(Consts.API_STICKER_PACK_PURCHASE)
    Observable<Boolean> purchaseStickerPack(@Header("Authorization") String token, @Path("id") int id, @Field("receipt") String purchaseToken, @Field("platform") String platform);

    @GET(Consts.API_BANNER)
    Observable<ApiClient.BannerResponse> getBanner();
}
