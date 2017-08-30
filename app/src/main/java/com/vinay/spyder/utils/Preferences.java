package com.vinay.spyder.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.maksim88.easylogin.AccessToken;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
        Set<String> movies = preferences.getStringSet("ratedmovies",null);
        Set<String> ratings = preferences.getStringSet("ratedratings",null);
        if (movies == null) {
            ratings = new HashSet<>();
            movies = new HashSet<>();
            increaseNoOfRatedMovies(context);
        }
        else if (movies.contains(tmdb_id)) {
            for (int i=0; i<movies.size();i++){
                if(movies.toArray()[i].equals(tmdb_id)){
                    movies.remove(i);
                    if (ratings != null) {
                        ratings.remove(i);
                    }
                }
            }
        }else {
            increaseNoOfRatedMovies(context);
        }
        movies.add(tmdb_id);
        if (ratings != null) {
            ratings.add(rating);
        }
        editor.putStringSet("ratedmovies",ratings);
        editor.apply();
    }

    public static ArrayList<String> getTopRatedMovies(Context context){
        ArrayList<String> topmovies = new ArrayList<>();
        SharedPreferences preferences = context.getSharedPreferences("ratings",Context.MODE_PRIVATE);
        Set<String> movies = preferences.getStringSet("ratedmovies",null);
        Set<String> ratings = preferences.getStringSet("ratedratings",null);
        if (movies == null) {
            return null;
        }
        for (int i=0; i<ratings.size(); i++ ){
            if(Float.compare(Float.parseFloat(ratings.toArray()[i].toString()),0.25f)>=0){
                topmovies.add(movies.toArray()[i].toString());
                if (topmovies.size()==3) break;
            }
        }
        if (topmovies!=null && topmovies.size() >0){

        }else {
            topmovies.addAll(movies);
            if (movies.size()>3)
                topmovies.subList(0,2);
            else
                topmovies.subList(0,movies.size()-1);
        }

        return topmovies;
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

    public static void removeFromFavourite(Context context,String tmdb_id){
        SharedPreferences preferences = context.getSharedPreferences("userlist",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Set<String> movies = preferences.getStringSet("favourite",null);
        if (movies != null && movies.contains(tmdb_id)){
            movies.remove(tmdb_id);
        }
        editor.putStringSet("watch",movies);
        editor.apply();
    }

    public static boolean isFavourite(Context context, String tmdb_id){
        SharedPreferences preferences = context.getSharedPreferences("userlist",Context.MODE_PRIVATE);
        Set<String> movies = preferences.getStringSet("favourite",null);
        if (movies != null && movies.contains(tmdb_id)){
            return true;
        }
        return false;
    }


    public static boolean isWatchList(Context context, String tmdb_id){
        SharedPreferences preferences = context.getSharedPreferences("userlist",Context.MODE_PRIVATE);
        Set<String> movies = preferences.getStringSet("watch",null);
        if (movies != null && movies.contains(tmdb_id)){
            return true;
        }
        return false;
    }

    public static void removeFromWatchList(Context context,String tmdb_id){
        SharedPreferences preferences = context.getSharedPreferences("userlist",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Set<String> movies = preferences.getStringSet("watch",null);
        if (movies != null && movies.contains(tmdb_id)){
            movies.remove(tmdb_id);
        }
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
        Set<String> movies = preferences.getStringSet("watch",null);
        if (movies == null){
            return null;
        }else {
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.addAll(movies);
            return arrayList;
        }
    }
}
