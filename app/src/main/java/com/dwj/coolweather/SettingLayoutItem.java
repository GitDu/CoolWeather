package com.dwj.coolweather;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rey.material.widget.CheckBox;


/**
 * Created by duWenJun on 17-9-10.
 */

public class SettingLayoutItem extends LinearLayout {
    private static final String TAG = "SettingLayoutItem";
    private Context mContext;
    private ImageView mImageView;
    private TextView mTextView;
    private ImageView mGo;
    private Bitmap mIcon;
    private String mText;
    private boolean mIsHide;
    private CheckBox mCheck;
    private EventCallBack mCallBack;

    public SettingLayoutItem(Context context) {
        super(context);
        totalInit(context, null, 0);

    }

    public SettingLayoutItem(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        totalInit(context, attributeSet, 0);
    }

    public SettingLayoutItem(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        //初始化
        totalInit(context, attributeSet, defStyleAttr);

    }

    public void totalInit(Context context, AttributeSet attributeSet, int defStyleAttr) {
        this.mContext = context;
        initView();
        initAttrs(attributeSet, defStyleAttr);
        initData();
    }

    public void initView() {
        //第三个参数设置为true 否则不能成功加载到activity上.
        LayoutInflater.from(mContext).inflate(R.layout.setting_item, SettingLayoutItem.this, true);
        mImageView = ((ImageView) findViewById(R.id.setting_image));
        mTextView = ((TextView) findViewById(R.id.setting_text));
        mGo = ((ImageView) findViewById(R.id.go));
        mCheck = ((CheckBox) findViewById(R.id.checkbox));
    }

    //初始化属性 固定写法
    private void initAttrs(AttributeSet attributeSet, int defStyle) {
        TypedArray typedArray = mContext.getTheme().obtainStyledAttributes(attributeSet, R.styleable.SettingLayoutItem, defStyle, 0);
        for (int i = 0; i < typedArray.getIndexCount(); i++) {
            int attribute = typedArray.getIndex(i);
            switch (attribute) {
                case R.styleable.SettingLayoutItem_icon:
                    mIcon = BitmapFactory.decodeResource(getResources(), typedArray.getResourceId(attribute, -1));
                    break;
                case R.styleable.SettingLayoutItem_text:
                    mText = typedArray.getString(attribute);
                    break;
                case R.styleable.SettingLayoutItem_isHide:
                    mIsHide = typedArray.getBoolean(attribute, false);
                    break;
                default:
                    throw new RuntimeException("this attribute did not support ");
            }
        }
        typedArray.recycle();
    }

    public void initData() {
        if (mImageView != null && mTextView != null) {
            mImageView.setImageBitmap(mIcon);
            mTextView.setText(mText);
        }
        if (mIsHide) {
            mGo.setVisibility(View.VISIBLE);
            mCheck.setVisibility(GONE);
            mGo.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mCallBack != null) {
                        mCallBack.callBack(true);
                    }
                }
            });
        } else {
            mGo.setVisibility(View.GONE);
            mCheck.setVisibility(VISIBLE);
            mCheck.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mCallBack != null) {
                        mCallBack.callBack(mCheck.isChecked());
                    }
                }
            });
        }
    }


    public void registerCallBack(EventCallBack callBack) {
        this.mCallBack = callBack;
    }

    public void unRegisterCallBack() {
        if (mCallBack != null) {
            mCallBack = null;
        }
    }

    public interface EventCallBack {
        void callBack(boolean isCheck);
    }

    public void setCheckState(boolean state) {
        mCheck.setEnabled(state);
    }

}
