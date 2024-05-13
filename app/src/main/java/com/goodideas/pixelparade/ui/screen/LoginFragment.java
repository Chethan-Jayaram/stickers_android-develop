package com.goodideas.pixelparade.ui.screen;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anjlab.android.iab.v3.PurchaseInfo;
import com.goodideas.pixelparade.R;
import com.goodideas.pixelparade.SharedPreferencesHelper;
import com.goodideas.pixelparade.data.ApiClient;
import com.goodideas.pixelparade.ui.MainActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import kotlin.jvm.functions.FunctionN;
import timber.log.Timber;

public class LoginFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String token = SharedPreferencesHelper.getUserToken(getActivity());
        if (TextUtils.isEmpty(token)) {
            ApiClient.getInstance(getActivity()).getApiService().createToken()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                        final String tokenFinal = result.substring(1, result.length() - 1);
                        SharedPreferencesHelper.setUserToken(getActivity(), tokenFinal);
                        getStickerPacksList(tokenFinal, 1);
                    }, this::handleError);
        } else {
            getStickerPacksList(token, 1);
        }
    }

    private void getStickerPacksList(String token, int page) {
        ApiClient.getInstance(getActivity()).getApiService().getStickerPacksList(token, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    ApiClient.getInstance(getActivity()).setAllPacks(new ArrayList<>(result));
                    if (!result.isEmpty() && result.size() == 100) {
                        getStickerPacksList(token, page + 1);
                    } else {
                        restorePurchasedPacks();
                    }
                }, this::handleError);
    }

    @SuppressLint("CheckResult")
    private void restorePurchasedPacks() {
        ApiClient client = ApiClient.getInstance(requireActivity());
        List<String> purchasedProductIds = client
                .getBillingProcessor()
                .listOwnedProducts();
        ArrayList<ApiClient.StickersPackJSON> packsFromServer = client.getAllPacks();
        ArrayList<Observable<Boolean>> observableList = new ArrayList<>();
        for (String productId : purchasedProductIds) {
            try {
                int packId = Integer.parseInt(productId.substring(productId.indexOf("_") + 1));
                PurchaseInfo details = client
                        .getBillingProcessor()
                        .getPurchaseInfo(productId);

                if(details != null) {
                    for (ApiClient.StickersPackJSON stickerPack : packsFromServer) {
                        if (stickerPack.getId() == packId) {
                            observableList.add(
                                    client.setPurchasedPackOnServer(
                                            details.purchaseData.purchaseToken,
                                            packId
                                    ).doOnComplete(() -> {
                                        SharedPreferencesHelper.addPack(getContext(), stickerPack.getName());
                                    })
                            );
                            break;
                        }
                    }
                }

            } catch (Exception e) {
                handleStickerPacksLoaded();
                return;
            }
        }
        if (observableList.isEmpty()) {
            handleStickerPacksLoaded();
        } else {
            Observable.zip(observableList, (Function<Object[], Object>) objects -> objects)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                        handleStickerPacksLoaded();
                    }, this::handleError);
        }
    }

    // Here lies the only logic that determines which packs
    // are still not bought and which are already bought and installed.
    private void handleStickerPacksLoaded() {
        ArrayList<String> boughtAndDownloadedPacks =
                SharedPreferencesHelper.getBoughtAndDownloadedPacks(getActivity());
        ArrayList<ApiClient.StickersPackJSON> packsFromServer = ApiClient.getInstance(getActivity()).getAllPacks();
        ArrayList<ApiClient.StickersPackJSON> installedPacks = new ArrayList<>();
        for (String boughtPackName : boughtAndDownloadedPacks) {
            int index = 0;
            while (index < packsFromServer.size()) {
                if (boughtPackName.compareTo(packsFromServer.get(index).getName()) == 0) {
                    installedPacks.add(0, packsFromServer.get(index));
                    packsFromServer.remove(index);
                    break;
                }
                index++;
            }
        }
        ApiClient.getInstance(getActivity()).setPacksToBuy(packsFromServer);
        ApiClient.getInstance(getActivity()).setInstalledPacks(installedPacks);
        ApiClient.getInstance(getActivity()).setSelectedTab(-1);

        if (packsFromServer.isEmpty()) {
            AllLoadedFragment fragment = new AllLoadedFragment();
            ((MainActivity) getActivity()).createTabs();
            ((MainActivity) getActivity()).replaceFragment(fragment);
        } else {
            MainFragment fragment = new MainFragment();
            ((MainActivity) getActivity()).replaceFragment(fragment);
        }
    }

    private void handleError(Throwable error) {
        Timber.e(error);
        showLoadingError();
    }

    private void showLoadingError() {
        try {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.loading_error_caption)
                    .setMessage(R.string.loading_error_text)
                    .setPositiveButton(R.string.btn_ok, (dialog, whichButton) -> getActivity().finish())
                    .setOnCancelListener((dialog) -> getActivity().finish())
                    .show();
        } catch (Exception e) {
            Timber.e(e);
        }
    }
}
