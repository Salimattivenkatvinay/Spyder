package com.vinay.spyder.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.util.ArrayMap;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.allattentionhere.fabulousfilter.AAH_FabulousFragment;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.mxn.soul.flowingdrawer_core.FlowingDrawer;
import com.vinay.spyder.R;
import com.vinay.spyder.Spyder;
import com.vinay.spyder.utils.DataBaseHelper;
import com.vinay.spyder.utils.Preferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class RatingActivity extends AppCompatActivity
        implements AAH_FabulousFragment.Callbacks, AAH_FabulousFragment.AnimationListener,SwipeRefreshLayout.OnRefreshListener {

    RecyclerView rv_movie;
    ArrayList<String> mvieId = new ArrayList<>();
    ImageView userPic;
    View view;
    FloatingActionButton fab;
    private ArrayMap<String, List<String>> applied_filters = new ArrayMap<>();
    FiltersFragment dialogFrag;
    MovieAdapter movieAdapter;
    SwipeRefreshLayout swipeRefreshLayout;

    DataBaseHelper dataBaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating1);
        view = findViewById(R.id.root_layout);
        swipeRefreshLayout=findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        view=findViewById(R.id.root_layout);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (getIntent() != null && getIntent().getStringArrayListExtra("showingList") != null) {
            mvieId = getIntent().getStringArrayListExtra("showingList");
        }else {
            if (Preferences.isIntialRated(this)){
                getRecommended();
            }else {
                ArrayList<String> tmdbIds = new ArrayList<>();
                dataBaseHelper = new DataBaseHelper(RatingActivity.this);
                ArrayList<HashMap<String, String>> arrayList = dataBaseHelper.getMovies(2, 10, false);
                for (HashMap<String, String> h : arrayList) {
                    tmdbIds.add(h.get(DataBaseHelper.TMDB_ID));
                }
                mvieId = tmdbIds;
            }
            /*
        } else {
            mvieId.add("155");
            mvieId.add("465099");
            mvieId.add("157336");
            mvieId.add("672");
            mvieId.add("293660");
            mvieId.add("271110");*/
        }

        setupDrawer();
        rv_movie = findViewById(R.id.rv_movies);
        movieAdapter = new MovieAdapter();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(RatingActivity.this, LinearLayoutManager.VERTICAL, false);
        rv_movie.setLayoutManager(layoutManager);
        rv_movie.setAdapter(movieAdapter);

        dialogFrag = FiltersFragment.newInstance();
        dialogFrag.setParentFab(fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogFrag.show(getSupportFragmentManager(), dialogFrag.getTag());
            }
        });
    }

    public void showUserList(View view) {
        String title = "";
        ArrayList<String> temp = new ArrayList<>();
        switch (view.getId()) {
            case R.id.recommended:
                getRecommended();
                return;
            case R.id.watchlist:
                temp = Preferences.getWatchList(RatingActivity.this);
                title = "Watch List";
                break;
            case R.id.favourites:
                temp = Preferences.getFavourites(RatingActivity.this);
                title = "Favourites";
                break;
        }
        if (temp != null && temp.size() > 0) {
            if (null != getSupportActionBar())
                getSupportActionBar().setTitle(title);
            mvieId = temp;
            rv_movie.getAdapter().notifyDataSetChanged();
        } else
            Toast.makeText(RatingActivity.this, "List is Empty", Toast.LENGTH_SHORT).show();

        ((FlowingDrawer) findViewById(R.id.drawerlayout)).closeMenu(true);
    }

    private void getRecommended() {
        final ProgressDialog progressDialog = new ProgressDialog(RatingActivity.this);
        progressDialog.setMessage("loading");
        progressDialog.setCancelable(false);
        progressDialog.show();
        final Intent intent = new Intent(RatingActivity.this, RatingActivity.class);
        List<String> topmovies = Preferences.getTopRatedMovies(RatingActivity.this);
        if (topmovies != null && !topmovies.isEmpty()) {
            //Collections.sort(topmovies);
            final int k = (topmovies.size()<3)? topmovies.size():3;
            final ArrayList<String> similarmovies = new ArrayList<>();
            for (int i = 0; i < k; i++) {
                String url = "https://www.themoviedb.org/movie/" + topmovies.get(i);
                RequestQueue requestQueue = Volley.newRequestQueue(RatingActivity.this);
                final int finalI = i;
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Document doc = Jsoup.parse(response);
                        Elements content = doc.getElementsByClass("item mini backdrop mini_card");
                        progressDialog.setMessage("Task "+(finalI+1)+" of 3");
                        for (Element l : content) {
                            Elements link = l.getElementsByClass("image_content");
                            Elements w = link.get(0).getElementsByTag("a");
                            similarmovies.add(w.get(0).attr("href").substring(7));
                        }

                        if (finalI == k-1 && similarmovies != null && similarmovies.size() > 0) {
                            Log.e("key", similarmovies.toString());
                            progressDialog.dismiss();
                            Collections.shuffle(similarmovies);
                            mvieId = similarmovies;
                            rv_movie.getAdapter().notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (finalI == k-1){
                            swipeRefreshLayout.setRefreshing(false);
                            progressDialog.dismiss();
                        }
                        Toast.makeText(RatingActivity.this, "Failed to load Recommendations", Toast.LENGTH_SHORT).show();
                    }
                });
                requestQueue.add(stringRequest);
            }
        }
    }

    private void setupDrawer() {
        userPic = findViewById(R.id.userdp);
        String uname = Preferences.getCredentials(RatingActivity.this).get("email");
        uname = uname.substring(0, uname.indexOf("@"));
        ((TextView) findViewById(R.id.username)).setText(uname);
        RequestQueue queue = Volley.newRequestQueue(RatingActivity.this);
        String url = "https://www.googleapis.com/plus/v1/people/" +
                Preferences.getCredentials(RatingActivity.this).get("password") +
                "?fields=image&key=AIzaSyCFchHhGGRMvDs4SVUgIeE7qXcyS45bgnQ";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            Log.d("userresponse", response);

                            String url = (json.getJSONObject("image").getString("url"));
                            url = url.substring(0, url.indexOf("?")) + "sz=150";
                            Log.d("userUrl", url);
                            Glide.with(RatingActivity.this)
                                    .load(url)
                                    .into(userPic);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(stringRequest);

    }

    @Override
    public void onOpenAnimationStart() {

    }

    @Override
    public void onOpenAnimationEnd() {

    }

    @Override
    public void onCloseAnimationStart() {

    }

    @Override
    public void onCloseAnimationEnd() {

    }

    @Override
    public void onResult(Object result) {

/*http://api.themoviedb.org/3/person/200?api_key=7e8f60e325cd06e164799af1e317d7a7*/
        Log.d("k9res", "onResult: " + result.toString());
        if (result.toString().equalsIgnoreCase("swiped_down")) {
            //do something or nothing
        } else {
            if (result != null) {
                ArrayMap<String, List<String>> applied_filters = (ArrayMap<String, List<String>>) result;
                if (applied_filters.size() != 0) {
                    List<String> filteredList =new ArrayList<>();
                    //iterate over arraymap
                    for (Map.Entry<String, List<String>> entry : applied_filters.entrySet()) {
                        Log.d("k9res", "entry.key: " + entry.getKey());
                        if (dataBaseHelper==null)dataBaseHelper = new DataBaseHelper(RatingActivity.this);
                        switch (entry.getKey()) {
                            case "genre":
                                List<HashMap<String,String>> a = dataBaseHelper.getFilteredList(0,20,false,entry.getValue(),null);
                                if(a!=null) {
                                    for (HashMap<String, String> h : a) {
                                        filteredList.add(h.get(DataBaseHelper.TMDB_ID));
                                    }
                                }
                                break;
                            case "year":
                                List<HashMap<String,String>> b = dataBaseHelper.getFilteredList(0,20, false, null, entry.getValue());
                                if(b!=null) {
                                    for (HashMap<String, String> h : b) {
                                        filteredList.add(h.get(DataBaseHelper.TMDB_ID));
                                    }
                                }
                                break;
                        }
                    }
                    Log.d("k9res", "new size: " + filteredList.size());
                    mvieId.clear();
                    mvieId.addAll(filteredList);
                    movieAdapter.notifyDataSetChanged();

                } else {
                    List<String> list = new ArrayList<>();
                    List<HashMap<String,String>> b = dataBaseHelper.getMovies(0,50,false);
                    for (HashMap<String,String> h : b){
                        list.add(h.get(DataBaseHelper.TMDB_ID));
                    }
                    mvieId.addAll(list);
                    movieAdapter.notifyDataSetChanged();
                }
            }
            //handle result
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (dialogFrag.isAdded()) {
            dialogFrag.dismiss();
            dialogFrag.show(getSupportFragmentManager(), dialogFrag.getTag());
        }
    }

    public ArrayMap<String, List<String>> getApplied_filters() {
        return applied_filters;
    }

    @Override
    public void onRefresh() {
        getRecommended();
        //movieAdapter.notifyDataSetChanged();
    }

    class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.Myholder> {

        private int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked}, // checked
                new int[]{-android.R.attr.state_checked}, // unchecked
        };

        private int[] colors = new int[]{
                Color.GREEN, // checked
                Color.YELLOW // unchecked set default in onCreate
        };

        class Myholder extends RecyclerView.ViewHolder {
            TextView titleView, taglineView, userRatingView, tmbdRateView, genreView, yearView;
            ImageView backdropView, posterView;
            MaterialRatingBar ratingBar;
            View parentView, loadingMask, errorMask;

            public Myholder(View itemView) {
                super(itemView);
                errorMask = itemView.findViewById(R.id.errormask);
                errorMask.setVisibility(View.GONE);
                loadingMask = itemView.findViewById(R.id.loadingmask);
                loadingMask.setVisibility(View.VISIBLE);
                parentView = itemView.findViewById(R.id.parent);
                titleView = itemView.findViewById(R.id.title);
                taglineView = itemView.findViewById(R.id.tagline);
                userRatingView = itemView.findViewById(R.id.userrating);
                tmbdRateView = itemView.findViewById(R.id.tmdb_rating);
                genreView = itemView.findViewById(R.id.genre);
                yearView = itemView.findViewById(R.id.year);
                backdropView = itemView.findViewById(R.id.backdrop);
                posterView = itemView.findViewById(R.id.poster);
                ratingBar = itemView.findViewById(R.id.ratingbar);
            }
        }

        @Override
        public Myholder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.movie_list_row, parent, false);

            return new Myholder(itemView);
        }

        @Override
        public void onBindViewHolder(final Myholder holder, final int position) {

            holder.parentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(RatingActivity.this, MoviePreview.class);
                    intent.putExtra("tmdbId", mvieId.get(position));
                    startActivity(intent);
                }
            });
            RequestQueue queue = Volley.newRequestQueue(RatingActivity.this);
            String url = "http://api.themoviedb.org/3/movie/" + mvieId.get(position) + "?api_key=7e8f60e325cd06e164799af1e317d7a7";
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject json = new JSONObject(response);
                                holder.titleView.setText(json.getString("original_title"));
                                holder.taglineView.setText(json.getString("tagline"));
                                String voteavg = json.getString("vote_average");
                                holder.tmbdRateView.setText(voteavg);
                                DisplayMetrics metrics = new DisplayMetrics();
                                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(metrics.widthPixels,(int)(0.56*metrics.widthPixels));
                                holder.backdropView.setLayoutParams(layoutParams);
                                if (dataBaseHelper!=null)
                                    holder.yearView.setText(dataBaseHelper.getYear(mvieId.get(position)));//"year : " + json.getString("release_date").substring(0,4));
                                else
                                    holder.yearView.setText("year : " + json.getString("release_date").substring(0,4));

                                Glide.with(RatingActivity.this)
                                        .load("http://image.tmdb.org/t/p/w185"+json.getString("poster_path"))
                                        .into(holder.posterView);

                                Glide.with(RatingActivity.this)
                                        .load("http://image.tmdb.org/t/p/w342"+json.getString("backdrop_path"))
                                        .into(holder.backdropView);

                                holder.ratingBar.setNumStars(5);
                                holder.ratingBar.setRating(Preferences.getRating(RatingActivity.this,mvieId.get(position)));
                                ColorStateList colorStateList = new ColorStateList(states,colors);
                                holder.ratingBar.setProgressTintList(colorStateList);
                                holder.ratingBar.setOnRatingChangeListener(new MaterialRatingBar.OnRatingChangeListener() {
                                    @Override
                                    public void onRatingChanged(MaterialRatingBar ratingBar, float rating) {
                                        Log.d("rating changed",rating+"");
                                        holder.userRatingView.setText(rating+"");
                                        Preferences.rateMovie(RatingActivity.this,mvieId.get(holder.getAdapterPosition()),rating+"");
                                        if (Preferences.getNoOfRatedMovies(RatingActivity.this) == 5 ){
                                            startActivity(new Intent(RatingActivity.this, GetRecommendations.class));
                                        }
                                    }
                                });

                                JSONArray array = json.getJSONArray("genres");
                                String genres = "";
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject k = array.getJSONObject(i);
                                    genres += k.getString("name") + " | ";

                                }
                                holder.genreView.setText(genres.substring(0, genres.length() - 3));

                                holder.loadingMask.setVisibility(View.GONE);
//                                getTrailerPath();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    holder.loadingMask.setVisibility(View.GONE);
                    holder.errorMask.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            queue.add(stringRequest);

        }

        void setupUI(Myholder myholder) {

        }

        @Override
        public int getItemCount() {
            return mvieId == null ? 0 : mvieId.size();
        }
/*

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
*/


    }


}
