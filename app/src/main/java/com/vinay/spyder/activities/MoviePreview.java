package com.vinay.spyder.activities;

import android.app.ProgressDialog;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.vinay.spyder.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MoviePreview extends AppCompatActivity {

    String title,tagline,year,genres="",language,imdb_id,overview,
            posterpath,backdroppath,trailerpath,is_adult,voteavg,revenue;
    String tmdb_id;

    ProgressDialog pb;

    ImageView posterView,backdropView;
    TextView taglineView,yearView,genresView,langView,voteView,revenueView;
    CollapsingToolbarLayout collapsingToolbarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_preview);
        tmdb_id = "155";
        pb = new ProgressDialog(MoviePreview.this);
        pb.setMessage("Loading...");
        getMovieData();
    }

    private void setupUI() {
        collapsingToolbarLayout = findViewById(R.id.collapsingToolbar);
        taglineView = findViewById(R.id.tagline);
        yearView = findViewById(R.id.year);
        genresView = findViewById(R.id.genre);
        voteView = findViewById(R.id.tmdb_rating);
        posterView = findViewById(R.id.poster);
        backdropView = findViewById(R.id.backdrop);

        collapsingToolbarLayout.setTitle(title);
        collapsingToolbarLayout.setEX
        taglineView.setText(tagline);
        genresView.setText(genres);
        voteView.setText(voteavg);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(metrics.widthPixels,(int)(0.56*metrics.widthPixels));
        backdropView.setLayoutParams(layoutParams);

        yearView.setText("year : " + year.substring(0,4));

        Glide.with(MoviePreview.this)
                .load("http://image.tmdb.org/t/p/w185"+posterpath)
                .into(posterView);


        Glide.with(MoviePreview.this)
                .load("http://image.tmdb.org/t/p/w342"+backdroppath)
                .into(backdropView);

    }

    private void getMovieData() {
        pb.show();
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://api.themoviedb.org/3/movie/" + tmdb_id + "?api_key=7e8f60e325cd06e164799af1e317d7a7";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            title = json.getString("original_title");
                            tagline = json.getString("tagline");
                            year = json.getString("release_date");
                            language = json.getString("original_language");
                            imdb_id = json.getString("imdb_id");
                            overview = json.getString("overview");
                            is_adult = json.getString("adult");
                            voteavg = json.getString("vote_average");
                            revenue = json.getString("revenue");

                            posterpath = json.getString("poster_path");
                            backdroppath = json.getString("backdrop_path");

                            JSONArray array=json.getJSONArray("genres");

                            for(int i=0;i<array.length();i++){
                                JSONObject k=array.getJSONObject(i);
                                genres+=k.getString("name")+" | ";

                            }
                            genres=genres.substring(0,genres.length()-3);

                            getTrailerPath();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pb.dismiss();
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        queue.add(stringRequest);
    }

    private void getTrailerPath() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://api.themoviedb.org/3/movie/"+tmdb_id+"/videos?api_key=7e8f60e325cd06e164799af1e317d7a7";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pb.dismiss();
                        try {
                            JSONObject json = new JSONObject(response);
                            JSONArray jsonArray = json.getJSONArray("results");
                            if (jsonArray.length()>0) {
                                JSONObject object=jsonArray.getJSONObject(0);
                                //object.get("site");
                                if ("YouTube".equals(object.getString("site"))){
                                    trailerpath = object.getString("key");
                                }
                            }

                            setupUI();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pb.dismiss();
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(stringRequest);
    }

}
