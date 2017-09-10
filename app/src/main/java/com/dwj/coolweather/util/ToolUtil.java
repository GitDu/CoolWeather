package com.dwj.coolweather.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.dwj.coolweather.SearchCityActivity;

import static com.dwj.coolweather.Contacts.BING;

/**
 * Created by duWenJun on 17-9-10.
 */

public class ToolUtil {
    private static final String TAG = "ToolUtil";

    public static void fitStatusBar(Activity activity) {
        //在5.0系统上的应用
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            View decorView = activity.getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public static void initBackupGround(Activity context, ImageView view) {
        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
        String urlPath = shared.getString(BING, null);
        if (urlPath != null && urlPath.length() > 0) {
            Glide.with(context).load(urlPath).into(view);
        }
    }
}
