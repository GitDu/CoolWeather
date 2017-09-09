package com.dwj.coolweather;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Toast;

/**
 * Created by duWenJun on 17-9-8.
 * 自定义可以监听输入 删除输入的控件
 */

public class DeleteEditText extends AppCompatEditText implements View.OnFocusChangeListener, TextWatcher {

    private static final String TAG = "DeleteEditText";
    private Drawable mRightDrawable;
    private Drawable mLeftDrawable;
    private TextChangeCallBack mCallBack;

    public interface TextChangeCallBack {
         void callBack(String string);
    }

    public DeleteEditText(Context context) {
        this(context, null);
    }

    public DeleteEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.support.v7.appcompat.R.attr.editTextStyle);
    }

    public DeleteEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //防止text变化回调多次
        this.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        initBitmap();
    }

    private void initBitmap() {
        mRightDrawable = getCompoundDrawables()[2];
        mLeftDrawable = getCompoundDrawables()[0];
        if (mRightDrawable == null) {
            mRightDrawable = getContext().getResources().getDrawable(R.drawable.delete_round);
        }
        if (mLeftDrawable == null) {
            mLeftDrawable = getContext().getResources().getDrawable(R.drawable.search);
        }
        mLeftDrawable.setBounds(0, 0, mLeftDrawable.getIntrinsicWidth(), mLeftDrawable.getIntrinsicHeight());
        mRightDrawable.setBounds(0, 0, mRightDrawable.getIntrinsicWidth(), mRightDrawable.getIntrinsicHeight());
        setClearIconVisible(false);
        setOnFocusChangeListener(this);
        addTextChangedListener(this);
    }

    private void setClearIconVisible(boolean visible) {
        Drawable rightDrawable = visible ? mRightDrawable : null;
        setCompoundDrawables(mLeftDrawable, getCompoundDrawables()[1], rightDrawable, getCompoundDrawables()[3]);

    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (getCompoundDrawables()[2] != null) {
                boolean touchable = event.getX() > (getWidth() - getTotalPaddingRight())
                        && (event.getX() < ((getWidth() - getPaddingRight())));
                Log.d(TAG, "onTouchEvent: " + touchable);
                if (touchable) {
                    this.setText("");
                    startViewAnimation();
                }
            }
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        setClearIconVisible(charSequence.length() > 0);
        //设置EditText的输入监听 动态的查询数据库
    }

    @Override
    public void afterTextChanged(Editable editable) {
        Log.d(TAG, "onTextChanged: " + editable.toString());
        String string = editable.toString();
        if (mCallBack != null) {
            mCallBack.callBack(string);
        }

    }

    @Override
    public void onFocusChange(View view, boolean b) {
        Log.d(TAG, "onFocusChange: " + b);
        if (b) {
            setClearIconVisible(getText().toString().length() > 0);
        } else {
            setClearIconVisible(false);
        }
    }


    public void startViewAnimation() {
        DeleteEditText.this.startAnimation(getAnimation());
    }

    public Animation getAnimation() {
        Animation translateAnimation = new TranslateAnimation(0, 10, 0, 0);
        translateAnimation.setInterpolator(new CycleInterpolator(5));
        translateAnimation.setFillAfter(true);
        translateAnimation.setDuration(1000);
        return translateAnimation;
    }

    public void registerCallBack(TextChangeCallBack callBack) {
        this.mCallBack = callBack;
    }

    public void unRegisterCallBack() {
        if (mCallBack != null) {
            mCallBack = null;
        }
    }
}
