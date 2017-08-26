package com.vinay.spyder.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.maksim88.easylogin.AccessToken;

import java.util.HashMap;

/**
 * Created by Vinay Sajjanapu on 8/26/2017.
 */

public class Preferences {
    public static void saveCredentials(Context context, String email, String password){
        SharedPreferences preferences = context.getSharedPreferences("login",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("password",password);
        editor.putString("email",email);
        editor.apply();
    }

    public static HashMap<String,String> getCredentials(Context context){
        SharedPreferences preferences = context.getSharedPreferences("login",Context.MODE_PRIVATE);
        HashMap<String,String> details = new HashMap<>();
        if (preferences.getString("password",null)!="" && preferences.getString("password",null)!= null){
            details.put("password",preferences.getString("token",null));
            details.put("email",preferences.getString("email",null));
            return details;
        }
        return null;
    }

    public static boolean isIntialRated(Context context){
        SharedPreferences preferences = context.getSharedPreferences("constants",Context.MODE_PRIVATE);
        return preferences.getBoolean("is_initial_rated",false);
    }

    public static void setInitialRated(Context context, Boolean isRated){
        SharedPreferences preferences = context.getSharedPreferences("constants",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("is_initial_rated",isRated);
        editor.apply();
    }
}
