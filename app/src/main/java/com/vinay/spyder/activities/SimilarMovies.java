package com.vinay.spyder.activities;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

/**
 * Created by vinay on 27-08-2017.
 */

public class SimilarMovies {

    String mvid;
    Context context;
    ArrayList<String> similarmvies=new ArrayList<>();

    public SimilarMovies(String mvid,Context context) {
        this.context=context;
        this.mvid = mvid;
    }

    public String getMvid() {
        return mvid;
    }

    public void setMvid(String mvid) {
        this.mvid = mvid;
    }

    public ArrayList<String> getSimilarmvies() {
        String url="https://www.themoviedb.org/movie/"+mvid;
        RequestQueue requestQueue= Volley.newRequestQueue(context);
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Document doc = Jsoup.parse(response);
                Elements content = doc.getElementsByClass("item mini backdrop mini_card");
                for(Element l:content){
                    Elements link = l.getElementsByClass("image_content");
                    Elements w=link.get(0).getElementsByTag("a");
                    similarmvies.add(w.get(0).attr("href").substring(6));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(stringRequest);
        return null;
    }
}
