package com.dwj.coolweather;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

    private DeleteEditText mEdit;
    private TextView mTextView;
    private ListView mList;
    private List<String> mData = new ArrayList<String>();
    private ArrayAdapter<String> mAdapter;
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
        mAdapter = new ArrayAdapter<String>(SearchCityActivity.this,
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
                    mData.add(countyForSearch.getCountyName());
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
