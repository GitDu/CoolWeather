package com.dwj.coolweather;

import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.TextView;

public class SearchCityActivity extends AppCompatActivity {

    private EditText mEdit;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_city);
        mEdit = ((EditText) findViewById(R.id.edit));
        mTextView = ((TextView) findViewById(R.id.cancel_action));
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

}
