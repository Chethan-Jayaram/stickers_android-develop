package com.goodideas.pixelparade;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.provider.Telephony;

import androidx.annotation.Nullable;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

public class Utils {

    public static String getStickerDownloadURL(String stickerName) {
        return BuildConfig.ENDPOINT.replace("api/", "") + stickerName;
    }

    public static String prepareToShare(String imgInCachePath, String filePath) {
        String res = "";
        try {
            InputStream is = new FileInputStream(imgInCachePath);
            InputStream in = new FileInputStream(imgInCachePath);
            if (filePath.contains(".gif")) {
                try {
                    OutputStream out = new FileOutputStream(filePath);
                    try {
                        // Transfer bytes from in to out
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                    } finally {
                        res = filePath;
                        out.close();
                    }
                } finally {
                    in.close();
                }
            } else {
                int index1 = filePath.lastIndexOf(".");
                String pathCopy = filePath.substring(0, index1 + 1) + "png";
                Bitmap bmp = BitmapFactory.decodeStream(is);
                Bitmap newBmp = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
                Canvas canvas = new Canvas(newBmp);
                canvas.drawColor(Color.WHITE);
                canvas.drawBitmap(bmp, 0, 0, null);
                FileOutputStream out = new FileOutputStream(pathCopy);
                newBmp.setHasAlpha(true);
                newBmp.compress(Bitmap.CompressFormat.PNG, 80, out);
                out.flush();
                out.close();
                res = pathCopy;
            }
        } catch (FileNotFoundException e) {
            Timber.e(e);
        } catch (IOException e) {
            Timber.e(e);
        }
        return res;
    }

    @Nullable
    public static String getDefaultMessagingPackageName(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) // Not ready for Android 11+
            return null;

        return Telephony.Sms.getDefaultSmsPackage(context);
    }

    public static ArrayList<ResolveInfo> prepareAppsList(Context context, List<ResolveInfo> resInfo) {
        ArrayList<ResolveInfo> result = new ArrayList<ResolveInfo>();
        ArrayList<String> packagesFromManager = new ArrayList<String>();
        ArrayList<String> packages = new ArrayList<>(Arrays.asList(Consts.messengerPackages));

        String smsPackage = getDefaultMessagingPackageName(context);

        if (smsPackage != null)
            packages.add(0, smsPackage);

        ArrayList<Integer> positions = new ArrayList<>();
        for (ResolveInfo info : resInfo) {
            try {
                int index = -1;
                for (int i = 0; i < packages.size(); i++) {
                    String packageName = packages.get(i);
                    if (info.activityInfo.packageName.contains(packageName)) {
                        index = i;
                        break;
                    }
                }
                if (packagesFromManager.size() == 0 || !packagesFromManager.contains(info.activityInfo.packageName)) {
                    packagesFromManager.add(info.activityInfo.packageName);
                    if (index != -1) {
                        int pos = -1;
                        for (int j = 0; j < positions.size(); j++) {
                            if (index < positions.get(j)) {
                                pos = j;
                                break;
                            }
                        }
                        if (pos >= 0) {
                            positions.add(pos, index);
                            result.add(pos, info);
                        } else {
                            result.add(positions.size(), info);
                            positions.add(index);
                        }
                    } else {
                        result.add(info);
                    }
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }
        return result;
    }
}
