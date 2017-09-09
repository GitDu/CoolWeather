package com.dwj.coolweather;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dwj.coolweather.db.CountyForSearch;

import org.litepal.crud.DataSupport;
import org.litepal.crud.callback.FindMultiCallback;

import java.util.ArrayList;
import java.util.List;

public class SearchCityActivity extends AppCompatActivity {

    private static final String TAG = "SearchCityActivity";
    private DeleteEditText mEdit;
    private TextView mTextView;
    private ListView mList;
    private List<SpannableStringBuilder> mData = new ArrayList<SpannableStringBuilder>();
    private ArrayAdapter<SpannableStringBuilder> mAdapter;
    private ArrayList<CountyForSearch> mLocalList;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            //数据初话完成 并且开始查询
            super.handleMessage(msg);
            search(((String) msg.obj));
        }
    };
    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_city);
        mEdit = ((DeleteEditText) findViewById(R.id.edit));
        mTextView = ((TextView) findViewById(R.id.cancel_action));
        mList = ((ListView) findViewById(R.id.data_list));
        mAdapter = new ArrayAdapter<SpannableStringBuilder>(SearchCityActivity.this,
                R.layout.search_item, mData);
        mList.setAdapter(mAdapter);
        mEdit.registerCallBack(new DeleteEditText.TextChangeCallBack() {
            @Override
            public void callBack(String string) {
                //回调动态查询数据库
                searchAtLocal(string.trim());
            }
        });
    }

    private void searchAtLocal(String string) {
        //查询数据库
        final String search = string;
        if (mLocalList != null && mLocalList.size() > 0) {
            search(search);
        } else {
            DataSupport.findAllAsync(CountyForSearch.class).listen(new FindMultiCallback() {
                @Override
                public <T> void onFinish(List<T> list) {
                    mLocalList = (ArrayList<CountyForSearch>) list;
                    Message message = Message.obtain();
                    message.obj = search;
                    mHandler.sendMessage(message);
                }
            });
        }
    }

    private void search(String search) {
        mData.clear();
        if (search.length() != 0) {
            for (CountyForSearch countyForSearch : mLocalList) {
                if (countyForSearch.getCountyName().contains(search)) {
                    SpannableStringBuilder name = dealWithString(countyForSearch.getCountyName(), search);
                    mData.add(name);
                }
            }
        }
        //更新adapter数据显示
        mList.setVisibility(View.VISIBLE);
        mAdapter.notifyDataSetChanged();
        if (mData.size() == 0) {
            if (mToast == null) {
                mToast = Toast.makeText(this, " 没有找到需要查找的数据 ", Toast.LENGTH_SHORT);
            } else {
                mToast.setText(" 没有找到需要查找的数据 ");
            }
            mToast.show();
        }
    }

    /**
     * 如果要改变字符串中多处字体颜色，setSpan方法中第一个参数必须要每次new一个对象出来才能显示效果
     * */
    private SpannableStringBuilder dealWithString(String name, String search) {
        //给搜索到的字符标记为红色
        SpannableStringBuilder spannableString = new SpannableStringBuilder(name);
        char[] chars = search.toCharArray();
        int[] ints = new int[chars.length];
        for (int i = 0; i < chars.length; i++) {
            ints[i] = name.indexOf(chars[i]);
        }
        for (int i = 0; i < ints.length; i++) {
            //给字符串分开设置特性时  需要单独生成Colorspan 否则后面的会把前面的效果覆盖掉
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.RED);
            Log.d(TAG, "dealWithString: " + ints[i]);
            spannableString.setSpan(foregroundColorSpan, ints[i],
                    ints[i] + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        //不能toString()返回 不然设置的特殊颜色不起作用
        return spannableString;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mEdit.unRegisterCallBack();
    }
}
