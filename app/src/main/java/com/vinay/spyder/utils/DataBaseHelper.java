package com.vinay.spyder.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DataBaseHelper extends SQLiteAssetHelper {
    private static final String DATABASE_NAME = "movie_db.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "movies_data";
    public static final String MOVIE_ID = "movieId";
    public static final String TMDB_ID = "tmdbId";
    public static final String MOVIE_TITLE = "title";
    public static final String YEAR = "year";
    public static final String GENRE = "genres";


    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public ArrayList<HashMap<String,String>> getMovies(int from_index, int no_of_results, boolean asc){
        ArrayList<HashMap<String,String>> arrayList = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " ORDER BY "+ YEAR ;

        if (asc)selectQuery += " ASC ";
        else selectQuery += " DESC ";

        selectQuery += "LIMIT " + from_index + ", " + no_of_results;

        Cursor cursor = database.rawQuery(selectQuery,null);
        if (cursor!=null){
            if (cursor.moveToFirst()){
                do {
                    try {
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put(MOVIE_ID, cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_ID)));
                        hashMap.put(TMDB_ID, cursor.getString(cursor.getColumnIndexOrThrow(TMDB_ID)));
                        hashMap.put(YEAR, cursor.getString(cursor.getColumnIndexOrThrow(YEAR)));
                        hashMap.put(MOVIE_TITLE, cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_TITLE)));
                        hashMap.put(GENRE, cursor.getString(cursor.getColumnIndexOrThrow(GENRE)));
                        arrayList.add(hashMap);
                    }catch (Exception ignored){};
                }while (cursor.moveToNext());
                return arrayList;
            }
            cursor.close();
        }
        return null;
    }

    public List<HashMap<String,String>> getFilteredList(int start_index,
                                                        int no_of_results,
                                                        boolean asc,
                                                        List<String> genres, List<String> years){

        List<HashMap<String,String>> arrayList = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();

        boolean genreFilter = (genres!=null)&&(genres.size()>0),
                yearFilter = (years!=null)&&(years.size()>0);
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE ";

        if (genreFilter){
            for(String genre : genres){
                selectQuery +=  GENRE + " LIKE '%"+genre+ "%' OR ";
            }
            selectQuery = selectQuery.substring(0, selectQuery.length()-3);
        }

        if (yearFilter){
            if (genreFilter) selectQuery += "AND ";
            selectQuery += YEAR + " IN ('";
            for(String year : years){
                selectQuery += (year + "','");
            }
            selectQuery = selectQuery.substring(0, selectQuery.length()-2);
            selectQuery += ") ";
        }

        selectQuery += "ORDER BY " + YEAR;
        if (asc)selectQuery += " ASC ";
        else selectQuery += " DESC ";

        selectQuery += "LIMIT " + start_index + ", " + no_of_results;

        Cursor cursor = database.rawQuery(selectQuery,null);
        if (cursor!=null){
            if (cursor.moveToFirst()){
                do {
                    try {
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put(MOVIE_ID, cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_ID)));
                        hashMap.put(TMDB_ID, cursor.getString(cursor.getColumnIndexOrThrow(TMDB_ID)));
                        hashMap.put(YEAR, cursor.getString(cursor.getColumnIndexOrThrow(YEAR)));
                        hashMap.put(MOVIE_TITLE, cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_TITLE)));
                        hashMap.put(GENRE, cursor.getString(cursor.getColumnIndexOrThrow(GENRE)));
                        arrayList.add(hashMap);
                    }catch (Exception ignored){};
                }while (cursor.moveToNext());
                return arrayList;
            }
            cursor.close();
        }
        return null;
    }

    public String getYear(String tmdbId){
        SQLiteDatabase database = this.getReadableDatabase();
        String selectQuery = "SELECT "+ YEAR +" FROM " + TABLE_NAME + " WHERE "+ TMDB_ID + " = '" + tmdbId + "'";
        Cursor cursor = database.rawQuery(selectQuery,null);
        if (cursor!=null){
            if (cursor.moveToFirst()){
                do {
                    try {
                        return cursor.getString(cursor.getColumnIndexOrThrow(YEAR));
                    }catch (Exception ignored){};
                }while (cursor.moveToNext());
            }
            cursor.close();
        }
        return "N/A";
    }

    public ArrayList<String> getYears(){
        ArrayList<String> arrayList = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " ORDER BY "+ YEAR + " DESC";
        Cursor cursor = database.rawQuery(selectQuery,null);
        if (cursor!=null){
            if (cursor.moveToFirst()){
                do {
                    try {
                        String year = cursor.getString(cursor.getColumnIndexOrThrow(YEAR));
                        if (!arrayList.contains(year))
                           arrayList.add(year);
                    }catch (Exception ignored){};
                }while (cursor.moveToNext());
                return arrayList;
            }
            cursor.close();
        }
        return null;
    }
}