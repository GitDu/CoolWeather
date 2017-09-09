package com.dwj.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by duWenJun on 17-9-2.
 */

public class HttpUtil {

    private static OkHttpClient okHttpClient;
    private static final String TAG = "HttpUtil";

    public static boolean handleHttpRequest(String url, okhttp3.Callback callback) {
        if (url != null && callback != null) {
            if (okHttpClient == null) {
                synchronized (HttpUtil.class) {
                    if (okHttpClient == null) {
                    okHttpClient = new OkHttpClient();
                    }
                }
            }
            Request build = new Request.Builder().url(url).build();
            okHttpClient.newCall(build).enqueue(callback);
            return true;
        } else {
            return false;
        }
    }
}
