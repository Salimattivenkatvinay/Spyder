package com.vinay.spyder.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.maksim88.easylogin.AccessToken;

import java.util.HashMap;

/**
 * Created by Vinay Sajjanapu on 8/26/2017.
 */

public class Preferences {
    public static void saveCredentials(Context context, AccessToken token){
        SharedPreferences preferences = context.getSharedPreferences("login",Context.MODE_WORLD_WRITEABLE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("token",token.getToken());
        editor.putString("userid",token.getUserId());
        editor.putString("username",token.getUserName());
        editor.putString("email",token.getEmail());
        editor.apply();
    }

    public static HashMap<String,String> getCredentials(Context context){
        SharedPreferences preferences = context.getSharedPreferences("login",Context.MODE_WORLD_WRITEABLE);
        HashMap<String,String> details = new HashMap<>();
        if (preferences.getString("token",null)!=null){
            details.put("token",preferences.getString("token",null));
            details.put("userid",preferences.getString("userid",null));
            details.put("username",preferences.getString("username",null));
            details.put("email",preferences.getString("email",null));
            return details;
        }
        return null;
    }
}
