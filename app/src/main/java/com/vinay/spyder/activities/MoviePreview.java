package com.vinay.spyder.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.vinay.spyder.R;
import com.vinay.spyder.utils.CrewItem;
import com.vinay.spyder.utils.DataBaseHelper;
import com.vinay.spyder.utils.Preferences;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

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
    ArrayList<CrewItem> castList = new ArrayList<>();
    ArrayList<CrewItem> crewList = new ArrayList<>();

    LikeButton favourite,watchlist;
    RecyclerView castView, crewView;
    DataBaseHelper dataBaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_preview);
        dataBaseHelper = new DataBaseHelper(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        pb = new ProgressDialog(MoviePreview.this);
        pb.setMessage("Loading...");
        pb.setCancelable(false);
        if (getIntent()!=null && getIntent().getStringExtra("tmdbId")!=null){
            tmdb_id = getIntent().getStringExtra("tmdbId");
            getMovieData();
        }
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
        castView = findViewById(R.id.cast_recycler);
        crewView = findViewById(R.id.crew_recycler);
        favourite = findViewById(R.id.fav);
        watchlist = findViewById(R.id.watch);

        collapsingToolbarLayout.setTitle(title);

        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0);
        if (tagline.isEmpty())taglineView.setLayoutParams(layoutParams1);
        taglineView.setText(tagline);
        genresView.setText(genres);
        voteView.setText(voteavg);
        overviewView.setText(overview);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(metrics.widthPixels,(int)(0.56*metrics.widthPixels));
        backdropView.setLayoutParams(layoutParams);

        yearView.setText(dataBaseHelper.getYear(tmdb_id));//"year : " + year.substring(0,4));

        Glide.with(MoviePreview.this)
                .load("http://image.tmdb.org/t/p/w185"+posterpath)
                .into(posterView);

        Glide.with(MoviePreview.this)
                .load("http://image.tmdb.org/t/p/w342"+backdroppath)
                .into(backdropView);

        ratingBar.setNumStars(5);
        ratingBar.setRating(((int)Float.parseFloat(voteavg))/2.0f);
        ratingView.setText(((int)Float.parseFloat(voteavg))/2.0f +"");

        ColorStateList colorStateList = new ColorStateList(states,colors);
        ratingBar.setProgressTintList(colorStateList);
        ratingBar.setOnRatingChangeListener(new MaterialRatingBar.OnRatingChangeListener() {
            @Override
            public void onRatingChanged(MaterialRatingBar ratingBar, float rating) {
                Log.d("rating changed",rating+"");
                ratingView.setText(rating+"");
                Preferences.rateMovie(MoviePreview.this,tmdb_id,rating+"");
               if (Preferences.getNoOfRatedMovies(MoviePreview.this) == 5 ){
                    startActivity(new Intent(MoviePreview.this, GetRecommendations.class));
                }
                //startActivity(new Intent(MoviePreview.this, GetRecommendations.class).putExtra("mvid",tmdb_id));
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playVideo(trailerpath);
            }
        });

        if (Preferences.isFavourite(MoviePreview.this,tmdb_id)){
            favourite.setLiked(true);
        }else favourite.setLiked(false);
        if (Preferences.isWatchList(MoviePreview.this,tmdb_id)){
            watchlist.setLiked(true);
        }else watchlist.setLiked(false);

        favourite.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                Preferences.addToFavourite(MoviePreview.this,tmdb_id);
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                Preferences.removeFromFavourite(MoviePreview.this,tmdb_id);
            }
        });


        watchlist.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                Preferences.addToWatchList(MoviePreview.this,tmdb_id);
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                Preferences.removeFromWatchList(MoviePreview.this,tmdb_id);
            }
        });


        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(MoviePreview.this,
                LinearLayoutManager.HORIZONTAL,false);
        RecyclerView.LayoutManager layoutManager1=new LinearLayoutManager(MoviePreview.this,
                LinearLayoutManager.HORIZONTAL,false);
        crewView.setLayoutManager(layoutManager);
        castView.setLayoutManager(layoutManager1);

        crewView.setAdapter(new CrewAdapter(crewList));
        castView.setAdapter(new CrewAdapter(castList));
        findViewById(R.id.youtubefrag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playerFragment!=null){
                    findViewById(R.id.youtubefrag).setVisibility(View.GONE);
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.remove(playerFragment);
                    ft.commit();
                }
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
        if (playerFragment==null || !playerFragment.isVisible())
            super.onBackPressed();
        else {
            findViewById(R.id.youtubefrag).setVisibility(View.GONE);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.remove(playerFragment);
            ft.commit();
        }
    }

    private void getMovieData() {
        pb.setMessage("loading Movie data");
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
        pb.setMessage("loading Trailer Data");
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://api.themoviedb.org/3/movie/"+tmdb_id+"/videos?api_key=7e8f60e325cd06e164799af1e317d7a7";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
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
                            getCrewData();
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

    private void getCrewData(){
        pb.setMessage("loading Crew and Cast");
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://api.themoviedb.org/3/movie/"+tmdb_id+"/casts?api_key=7e8f60e325cd06e164799af1e317d7a7";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pb.dismiss();
                        try {
                            JSONObject json = new JSONObject(response);
                            JSONArray cast = json.getJSONArray("cast");
                            for (int i=0; i< cast.length(); i++) {
                                JSONObject object=cast.getJSONObject(i);
                                CrewItem item = new CrewItem(object.getString("name"),object.getString("id"),
                                        object.getString("character"),object.getString("profile_path"));
                                castList.add(item);
                            }

                            JSONArray crew = json.getJSONArray("crew");
                            for (int i=0; i< crew.length(); i++) {
                                JSONObject object=crew.getJSONObject(i);
                                CrewItem item = new CrewItem(object.getString("name"),object.getString("id"),
                                        object.getString("job"),object.getString("profile_path"));
                                crewList.add(item);
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

    class CrewAdapter extends RecyclerView.Adapter<CrewAdapter.Myholder>{
        ArrayList<CrewItem> crewItems = new ArrayList<>();

        CrewAdapter(ArrayList<CrewItem> crewItems){
            this.crewItems = crewItems;
        }

        class Myholder extends RecyclerView.ViewHolder{
            TextView titleView;
            ImageView imageView;

            public Myholder(View itemView) {
                super(itemView);
                titleView = itemView.findViewById(R.id.title);
                imageView = itemView.findViewById(R.id.image);
            }
        }

        @Override
        public Myholder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_crew, parent, false);

            return new Myholder(itemView);
        }

        @Override
        public void onBindViewHolder(Myholder holder, int position) {
            holder.titleView.setText(crewItems.get(position).getName() + "\n" + crewItems.get(position).getRole());
            Glide.with(MoviePreview.this)
                    .load("http://image.tmdb.org/t/p/w185"+crewItems.get(position).getProfile_path())
                    .placeholder(R.drawable.avatar)
                    .error(R.drawable.avatar)
                    .into(holder.imageView);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    startActivity(new Intent(MoviePreview.this,castDetActivity.class));
                }
            });
        }

        @Override
        public int getItemCount() {
            return crewItems.size();
        }

    }

}
