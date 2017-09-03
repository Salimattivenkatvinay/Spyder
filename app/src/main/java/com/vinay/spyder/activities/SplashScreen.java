package com.vinay.spyder.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.vinay.spyder.R;

import java.util.HashMap;
import java.util.prefs.Preferences;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_splash_screen);
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                findViewById(R.id.progress_bar).setVisibility(View.GONE);
                goToNextActivity();
                finish();
            }
        }.execute();
    }

    private void goToNextActivity() {
        if(null == com.vinay.spyder.utils.Preferences.getCredentials(SplashScreen.this)){
            startActivity(new Intent(SplashScreen.this, LoginActivity.class));
        }
        else {
            if (com.vinay.spyder.utils.Preferences.isIntialRated(SplashScreen.this)){
                startActivity(new Intent(SplashScreen.this,GetRecommendations.class));
            }else {
                startActivity(new Intent(SplashScreen.this,AskToRate.class));
                finish();
            }
        }
    }
}
