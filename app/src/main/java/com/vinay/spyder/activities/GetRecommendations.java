package com.vinay.spyder.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import java.util.List;

public class GetRecommendations extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_get_recommendations);
        final ProgressDialog progressDialog = new ProgressDialog(GetRecommendations.this);
        progressDialog.setMessage("loading");
        progressDialog.setCancelable(false);
        findViewById(R.id.proceed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                final Intent intent = new Intent(GetRecommendations.this, RatingActivity.class);
                List<String> topmovies = Preferences.getTopRatedMovies(GetRecommendations.this);
                if (topmovies != null && !topmovies.isEmpty()) {
                    //Collections.sort(topmovies);
                    final int k = topmovies.size();//(topmovies.size()<3)? topmovies.size():3;
                    final ArrayList<String> similarmovies = new ArrayList<>();
                    ArrayList<String> rated = new ArrayList<>();
                    rated = Preferences.getRatedMovies(GetRecommendations.this);
                    for (int i = 0; i < k; i++) {
                        String url = "https://www.themoviedb.org/movie/" + topmovies.get(i);
                        RequestQueue requestQueue = Volley.newRequestQueue(GetRecommendations.this);
                        final int finalI = i;
                        final ArrayList<String> finalRated = rated;
                        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Document doc = Jsoup.parse(response);
                                Elements content = doc.getElementsByClass("item mini backdrop mini_card");
                                progressDialog.setMessage("Task "+(finalI+1)+" of "+k);
                                for (Element l : content) {
                                    Elements link = l.getElementsByClass("image_content");
                                    Elements w = link.get(0).getElementsByTag("a");
                                    String mv = w.get(0).attr("href").substring(7);
                                    if (!similarmovies.contains(mv))
                                        similarmovies.add(mv);

                                    if (finalRated != null && !finalRated.contains(mv))
                                        similarmovies.remove(mv);
                                }

                                if (finalI == k-1 && similarmovies != null && similarmovies.size() > 0) {
                                    Log.e("key", similarmovies.toString());
                                    progressDialog.dismiss();
                                    Collections.shuffle(similarmovies);

                                    intent.putStringArrayListExtra("showingList", similarmovies);
                                    startActivity(intent);
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if (finalI == k-1)progressDialog.dismiss();
                                Toast.makeText(GetRecommendations.this, "Failed to load Recommendations", Toast.LENGTH_SHORT).show();
                            }
                        });
                        requestQueue.add(stringRequest);
                    }
                }
            }
        });
    }
}
