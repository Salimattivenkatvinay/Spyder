package com.vinay.spyder.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Vinay Sajjanapu on 8/26/2017.
 */

public class Preferences {
    public static void saveCredentials(Context context, String email, String password) {
        SharedPreferences preferences = context.getSharedPreferences("login", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("password", password);
        editor.putString("email", email);
        editor.apply();
    }

    public static HashMap<String, String> getCredentials(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("login", Context.MODE_PRIVATE);
        HashMap<String, String> details = new HashMap<>();
        if (preferences.getString("password", null) != "" && preferences.getString("password", null) != null) {
            details.put("password", preferences.getString("password", null));
            details.put("email", preferences.getString("email", null));
            return details;
        }
        return null;
    }

    public static boolean isIntialRated(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("constants", Context.MODE_PRIVATE);
        return preferences.getBoolean("is_initial_rated", false);
    }

    public static void setInitialRated(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("constants", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("is_initial_rated", true);
        editor.apply();
    }

    private static void increaseNoOfRatedMovies(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("constants", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        int num = preferences.getInt("no_of_rated_movies", 0);
        editor.putInt("no_of_rated_movies", num + 1);
        editor.apply();
        if (num == 5)
            setInitialRated(context);
    }

    public static int getNoOfRatedMovies(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("constants", Context.MODE_PRIVATE);
        return preferences.getInt("no_of_rated_movies", 0);
    }

    public static int getRating(Context context, String tmdb_id){
        TinyDB tinyDB=new TinyDB(context);
        ArrayList<LinkedTreeMap<String, String>> movies = tinyDB.getObject("ratedmovies", ArrayList.class);
        if (movies==null) {
            return 0;
        } else {
            boolean flag = true;
            for (LinkedTreeMap curMap : movies) {
                //If this map has the object, that is the key doesn't return a null object
                if ((tmdb_id.equals((String) curMap.get("movie")))) {
                    //Stop traversing because we are done
                    return (int)Float.parseFloat(String.valueOf(curMap.get("rating")));
                }
            }
        }
        return 0;
    }

    public static void rateMovie(Context context, String tmdb_id, String rating) {
        TinyDB tinyDB=new TinyDB(context);
        ArrayList<LinkedTreeMap<String, String>> movies = tinyDB.getObject("ratedmovies", ArrayList.class);
        if (movies==null) {
            movies = new ArrayList<LinkedTreeMap<String, String>>();
            LinkedTreeMap<String, String> v = new LinkedTreeMap<>();
            v.put("movie", tmdb_id);
            v.put("rating", rating);
            movies.add(v);
            increaseNoOfRatedMovies(context);

        } else {
            boolean flag = true;
            for (LinkedTreeMap curMap : movies) {
                //If this map has the object, that is the key doesn't return a null object
                if ((tmdb_id.equals((String) curMap.get("movie")))) {
                    //Stop traversing because we are done
                    movies.remove(curMap);
                    LinkedTreeMap<String, String> v = new LinkedTreeMap<>();
                    v.put("movie", tmdb_id);
                    v.put("rating", rating);
                    movies.add(v);
                    flag = false;
                    break;
                }
            }
            if (flag) {
                LinkedTreeMap<String, String> v = new LinkedTreeMap<>();
                v.put("movie", tmdb_id);
                v.put("rating", rating);
                movies.add(v);
                increaseNoOfRatedMovies(context);
            }
        }
        tinyDB.putObject("ratedmovies",movies);
    }

    public static ArrayList<String> getTopRatedMovies(Context context) {
        ArrayList<String> topmovies = new ArrayList<>();
        /*SharedPreferences preferences = context.getSharedPreferences("ratings",Context.MODE_PRIVATE);
        Set<String> movies = preferences.getStringSet("ratedmovies",null);
        Set<String> ratings = preferences.getStringSet("ratedratings",null);*/
        TinyDB tinyDB=new TinyDB(context);
        ArrayList<LinkedTreeMap<String, String>> movies = tinyDB.getObject("ratedmovies", ArrayList.class);
        if (movies == null) {
            return null;
        }
        for (int i = 0; i < movies.size(); i++) {
            if (Float.compare(Float.parseFloat(movies.get(i).get("rating")), 2.5f) >= 0) {
                topmovies.add(movies.get(i).get("movie"));
                if (topmovies.size() == 3) break;
            }
        }
        if (topmovies != null && topmovies.size() > 0) {

        } else {
            for (LinkedTreeMap<String, String> p : movies) {
                topmovies.add(p.get("movie"));
            }
            if (movies.size() > 3)
                topmovies.subList(0, 2);
            else
                topmovies.subList(0, movies.size() - 1);
        }

        return topmovies;
    }


    public static void addToFavourite(Context context, String tmdb_id) {
        SharedPreferences preferences = context.getSharedPreferences("userlist", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Set<String> movies = preferences.getStringSet("favourite", null);
        if (movies == null) movies = new HashSet<>();
        else if (movies.contains(tmdb_id)) return;
        movies.add(tmdb_id);
        editor.putStringSet("favourite", movies);
        editor.apply();
    }

    public static void addToWatchList(Context context, String tmdb_id) {
        SharedPreferences preferences = context.getSharedPreferences("userlist", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Set<String> movies = preferences.getStringSet("watch", null);
        if (movies == null) movies = new HashSet<>();
        else if (movies.contains(tmdb_id)) return;
        movies.add(tmdb_id);
        editor.putStringSet("watch", movies);
        editor.apply();
    }

    public static void removeFromFavourite(Context context, String tmdb_id) {
        SharedPreferences preferences = context.getSharedPreferences("userlist", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Set<String> movies = preferences.getStringSet("favourite", null);
        if (movies != null && movies.contains(tmdb_id)) {
            movies.remove(tmdb_id);
        }
        editor.putStringSet("watch", movies);
        editor.apply();
    }

    public static boolean isFavourite(Context context, String tmdb_id) {
        SharedPreferences preferences = context.getSharedPreferences("userlist", Context.MODE_PRIVATE);
        Set<String> movies = preferences.getStringSet("favourite", null);
        return movies != null && movies.contains(tmdb_id);
    }


    public static boolean isWatchList(Context context, String tmdb_id) {
        SharedPreferences preferences = context.getSharedPreferences("userlist", Context.MODE_PRIVATE);
        Set<String> movies = preferences.getStringSet("watch", null);
        return movies != null && movies.contains(tmdb_id);
    }

    public static void removeFromWatchList(Context context, String tmdb_id) {
        SharedPreferences preferences = context.getSharedPreferences("userlist", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Set<String> movies = preferences.getStringSet("watch", null);
        if (movies != null && movies.contains(tmdb_id)) {
            movies.remove(tmdb_id);
        }
        editor.putStringSet("watch", movies);
        editor.apply();
    }


    public static ArrayList<String> getFavourites(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("userlist", Context.MODE_PRIVATE);
        Set<String> movies = preferences.getStringSet("favourite", null);
        if (movies == null) {
            return null;
        } else {
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.addAll(movies);
            return arrayList;
        }
    }


    public static ArrayList<String> getWatchList(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("userlist", Context.MODE_PRIVATE);
        Set<String> movies = preferences.getStringSet("watch", null);
        if (movies == null) {
            return null;
        } else {
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.addAll(movies);
            return arrayList;
        }
    }
}
