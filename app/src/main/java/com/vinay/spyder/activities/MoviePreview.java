package com.vinay.spyder.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
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
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.vinay.spyder.R;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

import static android.provider.MediaStore.Video.Thumbnails.VIDEO_ID;

public class MoviePreview extends YouTubeBaseActivity {

    String title,tagline,year,genres="",language,imdb_id,overview,
            posterpath,backdroppath,trailerpath,is_adult,voteavg,revenue;
    String tmdb_id;
    MaterialRatingBar ratingBar;
    float rate = 0.0f;
    ProgressDialog pb;
    YouTubePlayerFragment playerFragment;
    FloatingActionButton floatingActionButton;

    ImageView posterView,backdropView;
    TextView taglineView,yearView,genresView,langView,voteView,revenueView,ratingView,overviewView;
    CollapsingToolbarLayout collapsingToolbarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_preview);
        tmdb_id = "672";
        pb = new ProgressDialog(MoviePreview.this);
        pb.setMessage("Loading...");
        getMovieData();
    }
    private int[][] states = new int[][] {
            new int[] {android.R.attr.state_checked}, // checked
            new int[] {-android.R.attr.state_checked}, // unchecked
    };

    private int[] colors = new int[] {
            Color.GREEN, // checked
            Color.YELLOW // unchecked set default in onCreate
    };
    private void setupUI() {
        collapsingToolbarLayout = findViewById(R.id.collapsingToolbar);
        taglineView = findViewById(R.id.tagline);
        yearView = findViewById(R.id.year);
        genresView = findViewById(R.id.genre);
        voteView = findViewById(R.id.tmdb_rating);
        posterView = findViewById(R.id.poster);
        backdropView = findViewById(R.id.backdrop);
        ratingBar = findViewById(R.id.ratingbar);
        ratingView = findViewById(R.id.userrating);
        floatingActionButton = findViewById(R.id.fab);
        overviewView = findViewById(R.id.overview);

        collapsingToolbarLayout.setTitle(title);
        taglineView.setText(tagline);
        genresView.setText(genres);
        voteView.setText(voteavg);
        overviewView.setText(overview);

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

        ratingBar.setNumStars(5);
        ColorStateList colorStateList = new ColorStateList(states,colors);
        ratingBar.setProgressTintList(colorStateList);
        ratingBar.setOnRatingChangeListener(new MaterialRatingBar.OnRatingChangeListener() {
            @Override
            public void onRatingChanged(MaterialRatingBar ratingBar, float rating) {
                Log.d("rating changed",rating+"");
                ratingView.setText(rating+"");
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playVideo(trailerpath);
            }
        });
    }

    private void playVideo(final String key) {
        FragmentManager fm = getFragmentManager();
        String tag = YouTubePlayerFragment.class.getSimpleName();
        playerFragment = (YouTubePlayerFragment) fm.findFragmentByTag(tag);
        if (playerFragment == null) {
            FragmentTransaction ft = fm.beginTransaction();
            playerFragment = YouTubePlayerFragment.newInstance();
            ft.add(R.id.youtubefrag, playerFragment, tag);
            findViewById(R.id.youtubefrag).setVisibility(View.VISIBLE);
            ft.commit();
        }

        playerFragment.initialize("AIzaSyD_OzuWiyifQ8zzd6cLnL4X1v7WEo80dgI", new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.loadVideo(key);
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Toast.makeText(MoviePreview.this, "Error while initializing YouTubePlayer.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (playerFragment==null)
            super.onBackPressed();
        else {
            findViewById(R.id.youtubefrag).setVisibility(View.GONE);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.remove(playerFragment);
            ft.commit();
        }
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
