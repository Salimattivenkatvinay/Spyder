package com.vinay.spyder.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.vinay.spyder.R;
import com.vinay.spyder.utils.Preferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class RatingActivity extends AppCompatActivity {

    RecyclerView rv_movie;
    ArrayList<String> mvieId=new ArrayList<>();
    ImageView userPic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating1);

        if (getIntent()!=null && getIntent().getStringArrayListExtra("showingList")!=null){
            mvieId = getIntent().getStringArrayListExtra("showingList");
        }else {
            mvieId.add("155");
            mvieId.add("465099");
            mvieId.add("157336");
            mvieId.add("672");
            mvieId.add("293660");
            mvieId.add("271110");
        }

        setupDrawer();
        rv_movie=findViewById(R.id.rv_movies);
        MovieAdapter movieAdapter=new MovieAdapter();
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(RatingActivity.this, LinearLayoutManager.VERTICAL,false);
        rv_movie.setLayoutManager(layoutManager);
        rv_movie.setAdapter(movieAdapter);
    }

    public void showUserList(View view){
        Intent intent = new Intent(RatingActivity.this,RatingActivity.class);
        switch (view.getId()){
            case R.id.recommended:
                intent.putStringArrayListExtra("showingList",Preferences.getFavourites(RatingActivity.this));
                break;
            case R.id.watchlist:
                intent.putStringArrayListExtra("showingList",Preferences.getWatchList(RatingActivity.this));
                break;
            case R.id.favourites:
                intent.putStringArrayListExtra("showingList",Preferences.getFavourites(RatingActivity.this));
                break;
        }
    }

    private void setupDrawer() {
        userPic = findViewById(R.id.userdp);
        String uname = Preferences.getCredentials(RatingActivity.this).get("email");
        uname = uname.substring(0,uname.indexOf("@"));
        ((TextView)findViewById(R.id.username)).setText(uname);
        RequestQueue queue = Volley.newRequestQueue(RatingActivity.this);
        String url = "https://www.googleapis.com/plus/v1/people/"+
                Preferences.getCredentials(RatingActivity.this).get("password")+
                "?fields=image&key=AIzaSyCFchHhGGRMvDs4SVUgIeE7qXcyS45bgnQ";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            Log.d("userresponse",response);

                            String url = (json.getJSONObject("image").getString("url"));
                            url = url.substring(0,url.indexOf("?")) + "sz=150";
                            Log.d("userUrl",url);
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

    class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.Myholder>{

        private int[][] states = new int[][] {
                new int[] {android.R.attr.state_checked}, // checked
                new int[] {-android.R.attr.state_checked}, // unchecked
        };

        private int[] colors = new int[]{
                Color.GREEN, // checked
                Color.YELLOW // unchecked set default in onCreate
        };
        class Myholder extends RecyclerView.ViewHolder{
            TextView titleView,taglineView,userRatingView,tmbdRateView,genreView,yearView;
            ImageView backdropView,posterView;
            MaterialRatingBar ratingBar;
            View parentView,loadingMask,errorMask;

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
                    intent.putExtra("tmdbId",mvieId.get(position));
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
                                holder.tmbdRateView.setText(json.getString("vote_average"));
                                DisplayMetrics metrics = new DisplayMetrics();
                                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(metrics.widthPixels,(int)(0.56*metrics.widthPixels));
                                holder.backdropView.setLayoutParams(layoutParams);
                                holder.yearView.setText("year : " + json.getString("release_date").substring(0,4));

                                Glide.with(RatingActivity.this)
                                        .load("http://image.tmdb.org/t/p/w185"+json.getString("poster_path"))
                                        .into(holder.posterView);

                                Glide.with(RatingActivity.this)
                                        .load("http://image.tmdb.org/t/p/w342"+json.getString("backdrop_path"))
                                        .into(holder.backdropView);

                                holder.ratingBar.setNumStars(5);
                                ColorStateList colorStateList = new ColorStateList(states,colors);
                                holder.ratingBar.setProgressTintList(colorStateList);
                                holder.ratingBar.setOnRatingChangeListener(new MaterialRatingBar.OnRatingChangeListener() {
                                    @Override
                                    public void onRatingChanged(MaterialRatingBar ratingBar, float rating) {
                                        Log.d("rating changed",rating+"");
                                        holder.userRatingView.setText(rating+"");
                                    }
                                });

                                JSONArray array=json.getJSONArray("genres");
                                String genres="";
                                for(int i=0;i<array.length();i++){
                                    JSONObject k=array.getJSONObject(i);
                                    genres+=k.getString("name")+" | ";

                                }
                                holder.genreView.setText(genres.substring(0,genres.length()-3));

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

        void setupUI(Myholder myholder){

        }

        @Override
        public int getItemCount() {
            return mvieId.size();
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
