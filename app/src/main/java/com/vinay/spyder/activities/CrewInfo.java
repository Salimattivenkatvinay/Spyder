package com.vinay.spyder.activities;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
                            JSONObject json = new JSONObject(response);
                            ((TextView)findViewById(R.id.tv_overview)).append(json.getString("biography"));
                            ((TextView)findViewById(R.id.tv_birth)).append(json.getString("birthday"));
                            if(json.getString("deathday")!=null)
                            ((TextView)findViewById(R.id.tv_death)).append(json.getString("deathday"));
                            String g=json.getString("gender").equals("1")?"female":"male";
                            ((TextView)findViewById(R.id.tv_gender)).append(g);
                            Glide.with(CrewInfo.this)
                                    .load("http://image.tmdb.org/t/p/w185"+json.getString("profile_path"))
                                    .placeholder(R.drawable.avatar)
                                    .error(R.drawable.avatar)
                                    .into((ImageView) findViewById(R.id.iv_profile));
                            collapsingToolbarLayout.setTitle(json.getString("name"));
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
