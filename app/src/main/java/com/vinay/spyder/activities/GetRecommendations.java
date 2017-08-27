package com.vinay.spyder.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.vinay.spyder.R;
import com.vinay.spyder.utils.Preferences;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;

public class GetRecommendations extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_recommendations);

        findViewById(R.id.proceed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(GetRecommendations.this, RatingActivity.class);
                ArrayList<String> topmovies = Preferences.getTopRatedMovies(GetRecommendations.this);
                if (topmovies != null && topmovies.size() > 0) {
                    Collections.sort(topmovies);
                    final ArrayList<String> similarmovies = new ArrayList<>();
                    String url = "https://www.themoviedb.org/movie/" + topmovies.get(topmovies.size() - 1);
                    RequestQueue requestQueue = Volley.newRequestQueue(GetRecommendations.this);
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Document doc = Jsoup.parse(response);
                            Elements content = doc.getElementsByClass("item mini backdrop mini_card");
                            for (Element l : content) {
                                Elements link = l.getElementsByClass("image_content");
                                Elements w = link.get(0).getElementsByTag("a");
                                similarmovies.add(w.get(0).attr("href").substring(6));
                            }
                            if (similarmovies != null && similarmovies.size() > 0) {

                                intent.putStringArrayListExtra("showingList", similarmovies);

                                startActivity(intent);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(GetRecommendations.this, "Failed to load Recommendations", Toast.LENGTH_SHORT).show();
                        }
                    });
                    requestQueue.add(stringRequest);
                }
            }
        });
    }
}
