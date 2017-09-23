package com.dwj.coolweather;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by duWenJun on 17-9-23.
 * 退出程序
 */

public class ActivityController {
    private static List<Activity> sActivityList = new ArrayList<Activity>();


    public static void addActivity(Activity activity) {
        if (!sActivityList.contains(activity)) {
            sActivityList.add(activity);
        }
    }

    public static void removeActivity(Activity activity) {
        if (sActivityList.contains(activity)) {
            sActivityList.remove(activity);
        }
    }

    public static void finishAll() {
        for (Activity activity : sActivityList) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }
}
