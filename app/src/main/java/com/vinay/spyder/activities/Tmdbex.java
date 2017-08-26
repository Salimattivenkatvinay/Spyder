package com.vinay.spyder.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.vinay.spyder.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Tmdbex extends AppCompatActivity {
    TextView result;
    ImageView imageView;
    Context context;

    Button loadData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tmdb);
        result = findViewById(R.id.result);
        imageView = findViewById(R.id.imgview);
        final EditText editText = findViewById(R.id.edittext);
        loadData = findViewById(R.id.load);

        context = this;

        loadData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!editText.getText().toString().isEmpty()){
                        getData(editText.getText().toString());
                }
                else Toast.makeText(getApplicationContext(),"enter something",Toast.LENGTH_SHORT).show();
            }
        });
    }
    ProgressDialog pb;
    private void getData(String id) {
        pb = new ProgressDialog(Tmdbex.this);
        pb.setMessage("Loading...");
        pb.show();
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://api.themoviedb.org/3/movie/"+id+"?api_key=7e8f60e325cd06e164799af1e317d7a7";

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pb.dismiss();
                        //Toast.makeText(Tmdbex.this, response, Toast.LENGTH_LONG).show();
                        try {
                            JSONObject json = new JSONObject(response);
                           JSONArray jArray = json.getJSONArray("genres");
                            //JSONObject jsonObject=json.getJSONObject("overview");
                            result.setText("overview:-   "+json.getString("overview")+"\n\n"+"status:-   "+json.getString("status")
                            +"\n\ngeners:-"+jArray);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pb.dismiss();
                Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

}


