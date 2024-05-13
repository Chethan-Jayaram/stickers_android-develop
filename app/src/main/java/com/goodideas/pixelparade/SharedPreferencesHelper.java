package com.goodideas.pixelparade;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class SharedPreferencesHelper {
    private static final String PREFERENCES = "preferences";

    private static final int MODE = Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS;

    public static final String USEREMAIL = "user_email";
    public static final String DOWNLOADEDPACKS = "downloaded_packs";
    public static final String DOWNLOADEDSTICKERS = "downloaded_stickers";
    public static final String USERTOKEN = "user_token";
    public static final String PURCHASETOKENS = "purchase_tokens";
    public static final String PHOTOFILEPATH = "photo_file_path";
    public static final String FIRSTSTART = "first_start";

    public static String getUserEmail(Context context) {
        return context.getSharedPreferences(PREFERENCES, MODE).getString(USEREMAIL, "");
    }

    public static void setUserEmail(Context context, String value) {
        context.getSharedPreferences(PREFERENCES, MODE).edit().putString(USEREMAIL, value).commit();
    }

    public static String getUserToken(Context context) {
        return context.getSharedPreferences(PREFERENCES, MODE).getString(USERTOKEN, "");
    }

    public static void setUserToken(Context context, String value) {
        context.getSharedPreferences(PREFERENCES, MODE).edit().putString(USERTOKEN, value).commit();
    }

    public static ArrayList<String> getBoughtAndDownloadedPacks(Context context) {
        String[] splitPacks = context.getSharedPreferences(PREFERENCES, MODE).getString(DOWNLOADEDPACKS, "").split(";");
        ArrayList<String> res = new ArrayList<>(Arrays.asList(splitPacks));
        int i = 0;
        while (i < res.size()) {
            if (TextUtils.isEmpty(res.get(i))) {
                res.remove(i);
            } else {
                i++;
            }
        }
        return res;
    }

    public static void addPack(Context context, String name) {
        ArrayList<String> res = getBoughtAndDownloadedPacks(context);
        if (res.indexOf(name) == -1) {
            res.add(0, name);
            context.getSharedPreferences(PREFERENCES, MODE).edit().putString(DOWNLOADEDPACKS, TextUtils.join(";", res)).commit();
        }
    }

    public static HashMap<String, ArrayList<String>> getDownloadedStickers(Context context) {
        HashMap<String, ArrayList<String>> result = new HashMap<>();
        String prefValue = context.getSharedPreferences(PREFERENCES, MODE).getString(DOWNLOADEDSTICKERS, "");
        String[] downloadedPacks = prefValue.split(";");
        for (int i = 0; i < downloadedPacks.length; i++) {
            int index = downloadedPacks[i].indexOf(":");
            if (index > 0) {
                String name = downloadedPacks[i].substring(0, index);
                String stickers = downloadedPacks[i].substring(index + 1, downloadedPacks[i].length());
                String[] stickersArray = stickers.split(",");
                ArrayList<String> stickersList = new ArrayList<>(Arrays.asList(stickersArray));
                result.put(name, stickersList);
            }
        }
        return result;
    }

    public static void addSticker(Context context, String packName, String stickerName) {
        HashMap<String, ArrayList<String>> result = getDownloadedStickers(context);
        ArrayList<String> stickersList = result.get(packName);
        if (stickersList == null) {
            stickersList = new ArrayList<>();
        }
        if (!stickersList.contains(stickerName)) {
            stickersList.add(stickerName);
            Collections.sort(stickersList);
            result.put(packName, stickersList);
            String prefNewValue = "";
            for (String key : result.keySet()) {
                stickersList = result.get(key);
                String packString = key.trim() + ":" + TextUtils.join(",", stickersList);
                prefNewValue = prefNewValue + packString + ";";
            }
            context.getSharedPreferences(PREFERENCES, MODE).edit().putString(DOWNLOADEDSTICKERS, prefNewValue).commit();
        }
    }

    public static HashMap<Integer, String> getPurchaseTokens(Context context) {
        HashMap<Integer, String> result = new HashMap<>();
        String prefValue = context.getSharedPreferences(PREFERENCES, MODE).getString(PURCHASETOKENS, "");
        String[] packs = prefValue.split(";");
        for (int i = 0; i < packs.length; i++) {
            int index = packs[i].indexOf("=");
            if (index >= 0) {
                int id = Integer.valueOf(packs[i].substring(0, index));
                String token = packs[i].substring(index + 1, packs[i].length());
                result.put(id, token);
            }
        }
        return result;
    }

    public static void addPurchaseToken(Context context, int id, String token) {
        HashMap<Integer, String> result = getPurchaseTokens(context);
        result.put(id, token);
        String prefNewValue = "";
        for (Integer packId : result.keySet()) {
            String tmp = packId + "=" + result.get(packId);
            prefNewValue = ((prefNewValue.isEmpty()) ? "" : prefNewValue + ";") + tmp;
        }
        context.getSharedPreferences(PREFERENCES, MODE).edit().putString(PURCHASETOKENS, prefNewValue).commit();
    }

    public static void setPhotoFilePath(Context context, String value) {
        context.getSharedPreferences(PREFERENCES, MODE).edit().putString(PHOTOFILEPATH, value).commit();
    }

    public static String getPhotoFilePath(Context context) {
        return context.getSharedPreferences(PREFERENCES, MODE).getString(PHOTOFILEPATH, "");
    }

    public static void setStartIsNotFirst(Context context) {
        context.getSharedPreferences(PREFERENCES, MODE).edit().putBoolean(FIRSTSTART, false).commit();
    }

    public static Boolean isFirstStart(Context context) {
        return context.getSharedPreferences(PREFERENCES, MODE).getBoolean(FIRSTSTART, true);
    }
}
