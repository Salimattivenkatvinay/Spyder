package com.vinay.spyder.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
    static String currentShowingList = "init";
    String INIT = "init";
    String RECOMMENDED = "recommended";
    String FAVOURITES = "favourites";
    String WATCHLIST = "watch";
    String RATEDMOVIES = "rated";

    DataBaseHelper dataBaseHelper;

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(RatingActivity.this)
                .setTitle("Confirm")
                .setMessage("Do you really want to exit?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        RatingActivity.super.onBackPressed();
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    FlowingDrawer drawer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating1);

        view = findViewById(R.id.root_layout);
        swipeRefreshLayout=findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        view=findViewById(R.id.root_layout);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        currentShowingList = INIT;

        drawer = ((FlowingDrawer) findViewById(R.id.drawerlayout));
        setupDrawer();

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_dehaze_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.toggleMenu(true);
            }
        });

        rv_movie = findViewById(R.id.rv_movies);
        movieAdapter = new MovieAdapter();
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(RatingActivity.this, LinearLayoutManager.VERTICAL, false);
        rv_movie.setLayoutManager(layoutManager);
        rv_movie.setAdapter(movieAdapter);
        rv_movie.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (swipeRefreshLayout.isRefreshing())
                    return;

                LinearLayoutManager lm= (LinearLayoutManager) rv_movie.getLayoutManager();
                int visibleItemCount = lm.getChildCount();
                int totalItemCount = lm.getItemCount();
                int pastVisibleItems = lm.findFirstCompletelyVisibleItemPosition();
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    fab.setVisibility(View.VISIBLE);
                }else {
                    fab.setVisibility(View.GONE);
                }
            }
        });
        loadList();
        dialogFrag = FiltersFragment.newInstance();
        dialogFrag.setParentFab(fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swipeRefreshLayout.setRefreshing(true);
              //  dialogFrag.show(getSupportFragmentManager(), dialogFrag.getTag());
                loadList();
            }
        });
    }

    private void loadList(){
        swipeRefreshLayout.setRefreshing(true);
        String title = "Spyder";
        if (getIntent() != null && getIntent().getStringArrayListExtra("showingList") != null) {
            mvieId = getIntent().getStringArrayListExtra("showingList");
            rv_movie.getAdapter().notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
        }else {
            if (currentShowingList.equals(INIT) || currentShowingList.equals(RECOMMENDED)) {
                if (Preferences.isIntialRated(this)) {
                    getRecommended();
                    title = "Recommended List";
                } else {
                    ArrayList<String> tmdbIds = new ArrayList<>();
                    dataBaseHelper = new DataBaseHelper(RatingActivity.this);

                    ArrayList<HashMap<String, String>> arrayList = dataBaseHelper.getMovies((int) (Math.random() * 1000), 15, false);
                    Collections.shuffle(arrayList);
                    for (HashMap<String, String> h : arrayList) {
                        tmdbIds.add(h.get(DataBaseHelper.TMDB_ID));
                    }
                    mvieId = tmdbIds;
                    rv_movie.getAdapter().notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                    title = "Spyder";
                }
            }else {
                ArrayList<String> temp = new ArrayList<>();
                if (currentShowingList.equals(RATEDMOVIES)){
                    temp = Preferences.getRatedMovies(RatingActivity.this);
                    title = "Rated Movies";
                }else if (currentShowingList.equals(WATCHLIST)){
                    temp = Preferences.getWatchList(RatingActivity.this);
                    title = "Watch List";
                }else if(currentShowingList.equals(FAVOURITES)){
                    temp = Preferences.getFavourites(RatingActivity.this);
                    title = "Favourites";
                }
                if (temp != null && temp.size() > 0) {
                    mvieId = temp;
                    rv_movie.getAdapter().notifyDataSetChanged();
                } else
                    Toast.makeText(RatingActivity.this, "List is Empty", Toast.LENGTH_SHORT).show();

                swipeRefreshLayout.setRefreshing(false);
            }
        }
        if (null != getSupportActionBar())
            getSupportActionBar().setTitle(title);

        rv_movie.scrollToPosition(0);
    }

    public void showUserList(View view) {
        String title = "";
        String current = INIT;
        ArrayList<String> temp = new ArrayList<>();
        swipeRefreshLayout.setRefreshing(true);
        switch (view.getId()) {
            case R.id.recommended:
                currentShowingList = RECOMMENDED;
                getRecommended();
                ((FlowingDrawer) findViewById(R.id.drawerlayout)).closeMenu(true);
                return;
            case R.id.watchlist:
                current = WATCHLIST;
                temp = Preferences.getWatchList(RatingActivity.this);
                title = "Watch List";
                break;
            case R.id.favourites:
                current = FAVOURITES;
                temp = Preferences.getFavourites(RatingActivity.this);
                title = "Favourites";
                break;
            case R.id.ratedmovies:
                current = RATEDMOVIES;
                temp = Preferences.getRatedMovies(RatingActivity.this);
                title = "Rated Movies";
                break;
        }
        if (temp != null && temp.size() > 0) {
            if (null != getSupportActionBar())
                getSupportActionBar().setTitle(title);
            mvieId = temp;
            currentShowingList = current;
            rv_movie.getAdapter().notifyDataSetChanged();
            rv_movie.scrollToPosition(0);
        } else {
            Toast.makeText(RatingActivity.this, "List is Empty", Toast.LENGTH_SHORT).show();
        }
        swipeRefreshLayout.setRefreshing(false);
        ((FlowingDrawer) findViewById(R.id.drawerlayout)).closeMenu(true);
    }

    private void getRecommended() {
        swipeRefreshLayout.setRefreshing(true);
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
            ArrayList<String> rated = new ArrayList<>();
            rated = Preferences.getRatedMovies(RatingActivity.this);
            for (int i = 0; i < k; i++) {
                String url = "https://www.themoviedb.org/movie/" + topmovies.get(i);
                RequestQueue requestQueue = Volley.newRequestQueue(RatingActivity.this);
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

                            if (finalRated != null && finalRated.contains(mv))
                                similarmovies.remove(mv);
                        }

                        if (finalI == k-1 && similarmovies != null && similarmovies.size() > 0) {
                            Log.e("key", similarmovies.toString());
                            progressDialog.dismiss();
                            Collections.shuffle(similarmovies);
                            mvieId = similarmovies;
                            rv_movie.getAdapter().notifyDataSetChanged();
                            rv_movie.scrollToPosition(0);
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
                                    .error(R.drawable.avatar)
                                    .placeholder(R.drawable.avatar)
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
        loadList();
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
            holder.loadingMask.setVisibility(View.VISIBLE);
            holder.errorMask.setVisibility(View.GONE);
            if (!swipeRefreshLayout.isRefreshing()) {
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
                                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(metrics.widthPixels, (int) (0.56 * metrics.widthPixels));
                                    holder.backdropView.setLayoutParams(layoutParams);
                                    if (dataBaseHelper != null)
                                        holder.yearView.setText(dataBaseHelper.getYear(mvieId.get(position)));//"year : " + json.getString("release_date").substring(0,4));
                                    else
                                        holder.yearView.setText("year : " + json.getString("release_date").substring(0, 4));

                                    Glide.with(RatingActivity.this)
                                            .load("http://image.tmdb.org/t/p/w185" + json.getString("poster_path"))
                                            .error(R.drawable.gradient_1)
                                            .placeholder(R.drawable.gradient_1)
                                            .into(holder.posterView);

                                    Glide.with(RatingActivity.this)
                                            .load("http://image.tmdb.org/t/p/w342" + json.getString("backdrop_path"))
                                            .error(R.drawable.gradient_2)
                                            .placeholder(R.drawable.gradient_2)
                                            .into(holder.backdropView);

                                    holder.ratingBar.setNumStars(5);
                                    if (currentShowingList.equals(RATEDMOVIES) || currentShowingList.equals(WATCHLIST) || currentShowingList.equals(FAVOURITES))
                                        holder.ratingBar.setRating(Preferences.getRating(RatingActivity.this, mvieId.get(holder.getAdapterPosition())));

                                    ColorStateList colorStateList = new ColorStateList(states, colors);
                                    holder.ratingBar.setProgressTintList(colorStateList);
                                    holder.ratingBar.setOnRatingChangeListener(new MaterialRatingBar.OnRatingChangeListener() {
                                        @Override
                                        public void onRatingChanged(MaterialRatingBar ratingBar, float rating) {
                                            Log.d("rating changed", rating + "");
                                            holder.userRatingView.setText(rating + "");
                                            Preferences.rateMovie(RatingActivity.this, mvieId.get(holder.getAdapterPosition()), rating + "");
                                            int n = Preferences.getNoOfRatedMovies(RatingActivity.this);
                                            if(n < 5) Preferences.setInitialRated(RatingActivity.this,false);
                                            if ( n == 5) {
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
                                if (genres.length()>3)
                                    holder.genreView.setText(genres.substring(0, genres.length() - 3));
                                else
                                    holder.genreView.setText("N/A");

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
