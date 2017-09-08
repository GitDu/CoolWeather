package com.dwj.coolweather;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
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
 */

public class DeleteEditText extends AppCompatEditText implements View.OnFocusChangeListener, TextWatcher {

    private static final String TAG = "DeleteEditText";
    private Drawable mRightDrawable;
    private Drawable mLeftDrawable;

    public DeleteEditText(Context context) {
        this(context, null);
    }

    public DeleteEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.support.v7.appcompat.R.attr.editTextStyle);
    }

    public DeleteEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initBitmap();
    }

    private void initBitmap() {
        mRightDrawable = getCompoundDrawables()[2];
        mLeftDrawable = getCompoundDrawables()[0];
        if (mRightDrawable == null) {
            mRightDrawable = getContext().getResources().getDrawable(R.drawable.delete_round);
            mLeftDrawable = getContext().getResources().getDrawable(R.drawable.search);
        }
        mLeftDrawable.setBounds(0,0,mLeftDrawable.getIntrinsicWidth(), mLeftDrawable.getIntrinsicHeight());
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
                    startAnimation();
                }
            }
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            setClearIconVisible(charSequence.length() > 0);
    }

    @Override
    public void afterTextChanged(Editable editable) {

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


    public void startAnimation(){
        DeleteEditText.this.startAnimation(getAnimation());
        Toast.makeText(getContext(), " 请输入需要查询的地址 ", Toast.LENGTH_SHORT).show();
    }

    public Animation getAnimation() {
        Animation translateAnimation = new TranslateAnimation(0, 10, 0, 0);
        translateAnimation.setInterpolator(new CycleInterpolator(5));
        translateAnimation.setFillAfter(true);
        translateAnimation.setDuration(1000);
        return translateAnimation;
    }
}
