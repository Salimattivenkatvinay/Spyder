package com.vinay.spyder.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.vinay.spyder.R;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.MovieDb;

public class Tmdbex extends AppCompatActivity {
    MovieDb movie;
    TextView result;
    ImageView imageView;
    Context context;
    EditText editText;
    Button loadData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tmdb);
        result = findViewById(R.id.result);
        imageView = findViewById(R.id.imgview);
        editText = findViewById(R.id.edittext);
        loadData = findViewById(R.id.load);

        context = this;

        loadData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!editText.getText().toString().isEmpty()){
                    new MovieTask(){
                        protected void onPostExecute(MovieDb movie) {
                            // Do something with movie
                            Toast.makeText(getApplicationContext(), movie.toString(), Toast.LENGTH_LONG).show();
                            result.setText("title: " + movie.getTitle() + "\n" +
                                    "overview: " + movie.getOverview()+ "\n" +
                                    "rating: " + movie.getUserRating()+ "\n"
                            );
                            Glide.with(context).load("http://image.tmdb.org/t/p/original"+movie.getPosterPath()).into(imageView);
                        }
                    }.execute(Integer.parseInt(editText.getText().toString()));
                }
            }
        });
    }
    protected class MovieTask extends AsyncTask<Integer, Void, MovieDb> {

        protected MovieDb doInBackground(Integer... params) {
            TmdbMovies movies = new TmdbApi("7e8f60e325cd06e164799af1e317d7a7").getMovies();
            return movies.getMovie(params[0],"en");
        }
    }
}


