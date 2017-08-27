package com.vinay.spyder.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.maksim88.easylogin.AccessToken;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            details.put("password",preferences.getString("password",null));
            details.put("email",preferences.getString("email",null));
            return details;
        }
        return null;
    }

    public static boolean isIntialRated(Context context){
        SharedPreferences preferences = context.getSharedPreferences("constants",Context.MODE_PRIVATE);
        return preferences.getBoolean("is_initial_rated",false);
    }

    public static void setInitialRated(Context context){
        SharedPreferences preferences = context.getSharedPreferences("constants",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("is_initial_rated",true);
        editor.apply();
    }

    private static void increaseNoOfRatedMovies(Context context){
        SharedPreferences preferences = context.getSharedPreferences("constants",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        int num = preferences.getInt("no_of_rated_movies",0);
        editor.putInt("no_of_rated_movies",num+1);
        editor.apply();
        if (num==5)
            setInitialRated(context);
    }

    public static int getNoOfRatedMovies(Context context){
        SharedPreferences preferences = context.getSharedPreferences("constants",Context.MODE_PRIVATE);
        return preferences.getInt("no_of_rated_movies",0);
    }

    public static void rateMovie(Context context, String tmdb_id, String rating){
        SharedPreferences preferences = context.getSharedPreferences("ratings",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Set<String> ratings = preferences.getStringSet("ratedmovies",null);
        if (ratings == null) ratings = new HashSet<>();
        else {
            for (int i =0; i<=10; i++){
                if (ratings.contains(tmdb_id+"="+i/2.0))
                    return;
            }
        }
        ratings.add(tmdb_id + "=" + rating);
        editor.putStringSet("ratedmovies",ratings);
        editor.apply();
        increaseNoOfRatedMovies(context);
    }


    public static void addToFavourite(Context context, String tmdb_id){
        SharedPreferences preferences = context.getSharedPreferences("userlist",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Set<String> movies = preferences.getStringSet("favourite",null);
        if (movies == null) movies = new HashSet<>();
        else if (movies.contains(tmdb_id)) return;
        movies.add(tmdb_id);
        editor.putStringSet("favourite",movies);
        editor.apply();
    }

    public static void addToWatchList(Context context, String tmdb_id){
        SharedPreferences preferences = context.getSharedPreferences("userlist",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Set<String> movies = preferences.getStringSet("watch",null);
        if (movies == null) movies = new HashSet<>();
        else if (movies.contains(tmdb_id)) return;
        movies.add(tmdb_id);
        editor.putStringSet("watch",movies);
        editor.apply();
    }

    public static ArrayList<String> getFavourites(Context context){
        SharedPreferences preferences = context.getSharedPreferences("userlist",Context.MODE_PRIVATE);
        Set<String> movies = preferences.getStringSet("favourite",null);
        if (movies == null){
            return null;
        }else {
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.addAll(movies);
            return arrayList;
        }
    }


    public static ArrayList<String> getWatchList(Context context){
        SharedPreferences preferences = context.getSharedPreferences("userlist",Context.MODE_PRIVATE);
        Set<String> movies = preferences.getStringSet("favourite",null);
        if (movies == null){
            return null;
        }else {
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.addAll(movies);
            return arrayList;
        }
    }
}
