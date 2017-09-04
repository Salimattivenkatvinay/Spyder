package com.vinay.spyder.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
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

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import org.json.JSONException;
import org.json.JSONObject;

public class CrewInfo extends AppCompatActivity {

    ProgressDialog pb;
    CollapsingToolbarLayout collapsingToolbarLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crew_info);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        collapsingToolbarLayout=findViewById(R.id.collapsingToolbar);
        pb=new ProgressDialog(CrewInfo.this);
        getCrewData();
    }


    private void getCrewData(){
        pb.setMessage("fetching details");
        pb.show();
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://api.themoviedb.org/3/person/"+getIntent().getExtras().getString("cid","200")+"?api_key=7e8f60e325cd06e164799af1e317d7a7";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pb.dismiss();
                        try {
                            final JSONObject json = new JSONObject(response);
                            if (json.has("biography"))
                                ((TextView)findViewById(R.id.tv_overview)).setText(json.getString("biography"));
                            if (json.has("birthday"))
                                ((TextView)findViewById(R.id.tv_birth)).setText(json.getString("birthday"));
                            if (json.has("deathday")){
                               if(json.getString("deathday")!=null && !json.getString("deathday").equals("null"))
                            ((TextView)findViewById(R.id.tv_death)).setText(json.getString("deathday"));
                            }
                            if (json.has("gender")) {
                                String g = json.getString("gender").equals("1") ? "female" : "male";
                                ((TextView) findViewById(R.id.tv_gender)).setText(g);
                            }
                            Glide.with(CrewInfo.this)
                                    .load("http://image.tmdb.org/t/p/w342"+json.getString("profile_path"))
                                    .placeholder(R.drawable.avatar)
                                    .error(R.drawable.avatar)
                                    .into((ImageView) findViewById(R.id.iv_profile));

                            collapsingToolbarLayout.setTitle(json.getString("name"));
                            final String url="http://www.imdb.com/name/"+json.getString("imdb_id");
                            findViewById(R.id.ib_imdb).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startActivity(new Intent(CrewInfo.this,WebviewActivity.class).putExtra("url",url));
                                }
                            });
                            final String ur="https://www.themoviedb.org/person/"+getIntent().getExtras().getString("cid","");
                            findViewById(R.id.ib_tmdb).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startActivity(new Intent(CrewInfo.this,WebviewActivity.class).putExtra("url",ur));
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pb.dismiss();

            }
        });
        queue.add(stringRequest);
    }
}
