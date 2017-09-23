package com.dwj.coolweather;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.dwj.coolweather.service.AutoUpdateService;

import static com.dwj.coolweather.Contacts.CHOSE_INDEX;

public class SettingActivity extends AppCompatActivity {
    private static final String TAG = "SettingActivity";

    private SettingLayoutItem mVersion_up;
    private SettingLayoutItem mAuto_update;
    private SettingLayoutItem mAlarm;
    private SettingLayoutItem mLocation;
    private SettingLayoutItem mAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ActivityController.addActivity(SettingActivity.this);
        initView();
    }

    private void initView() {
        mAuto_update = ((SettingLayoutItem) findViewById(R.id.auto_update));
        mVersion_up = ((SettingLayoutItem) findViewById(R.id.version_up));
        mAlarm = ((SettingLayoutItem) findViewById(R.id.alarm));
        mLocation = ((SettingLayoutItem) findViewById(R.id.location));
        mAbout = ((SettingLayoutItem) findViewById(R.id.about));
    }

    /**
     * 功能 1.开启后台服务,自动更新数据
     * 功能 2. 设置单选框 选择定义更新的时间
     */
    @Override
    protected void onResume() {
        super.onResume();
        mAuto_update.registerCallBack(new SettingLayoutItem.EventCallBack() {
            @Override
            public void callBack(boolean isCheck) {
                if (isCheck) {
                    //开启自动更新天气信息的服务
                    startService(new Intent(SettingActivity.this, AutoUpdateService.class));
                    Toast.makeText(SettingActivity.this, " 自动更新服务已开启 ", Toast.LENGTH_SHORT).show();
                    mAlarm.setCheckState(false);
                } else {
                    //关闭自动更新天气信息的服务
                    mAlarm.setCheckState(true);
                    stopService(new Intent(SettingActivity.this, AutoUpdateService.class));
                    Toast.makeText(SettingActivity.this, " 自动更新服务已关闭 ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mAlarm.registerCallBack(new SettingLayoutItem.EventCallBack() {
            @Override
            public void callBack(boolean isCheck) {
                if (isCheck) {
                    mAuto_update.setCheckState(false);
                    final String[] strings = new String[]{"每隔4小时更新一次", "每隔6小时更新一次", "每隔8小时更新一次"};
                    final SharedPreferences share = PreferenceManager.getDefaultSharedPreferences(SettingActivity.this);
                    int index = share.getInt(CHOSE_INDEX, -1);
                    AlertDialog dialog = new AlertDialog.Builder(SettingActivity.this)
                            .setTitle("选择需要更新的时间间隔")
                            .setIcon(R.drawable.single_choose)
                            .setSingleChoiceItems(strings, index, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    share.edit().putInt(CHOSE_INDEX, i).apply();
                                    dialogInterface.dismiss();
                                    Toast.makeText(SettingActivity.this, strings[i], Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(SettingActivity.this, AutoUpdateService.class);
                                    intent.putExtra(CHOSE_INDEX, i);
                                    startService(intent);
                                }
                            }).setCancelable(false)
                            .create();
                    dialog.show();
                } else {
                    mAuto_update.setCheckState(true);
                    Intent intent = new Intent(SettingActivity.this, AutoUpdateService.class);
                    stopService(intent);
                    Toast.makeText(SettingActivity.this, " 取消了定时更新的任务 ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mVersion_up.registerCallBack(new SettingLayoutItem.EventCallBack() {
            @Override
            public void callBack(boolean isCheck) {
                if (isCheck) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(3000);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(SettingActivity.this, " 版本已经是最新版 ", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        });
        mAbout.registerCallBack(new SettingLayoutItem.EventCallBack() {
            @Override
            public void callBack(boolean isCheck) {
                Toast.makeText(SettingActivity.this, " 功能暂未实现 ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        //解回调注册
        mAuto_update.unRegisterCallBack();
        mAlarm.unRegisterCallBack();
        mAbout.unRegisterCallBack();
        mVersion_up.unRegisterCallBack();
        ActivityController.removeActivity(SettingActivity.this);
        super.onDestroy();
    }
}
