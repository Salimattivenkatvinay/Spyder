package com.vinay.spyder;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.support.constraint.ConstraintLayout;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class RatingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating1);

//        ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.root_layout);
        PercentRelativeLayout percentRelativeLayout= (PercentRelativeLayout) findViewById(R.id.root_layout);
        AnimationDrawable animationDrawable = (AnimationDrawable) percentRelativeLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

        findViewById(R.id.btn_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RatingActivity.this,MainActivity.class));
            }
        });
    }
}
