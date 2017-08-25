package com.vinay.spyder.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.vinay.spyder.R;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.MovieDb;

public class Tmdbex extends AppCompatActivity {
    MovieDb movie;
@Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tmdb);

        MovieTask mt = new MovieTask();
        mt.execute();

        }

protected class MovieTask extends AsyncTask<Void, Void, MovieDb> {

    protected MovieDb doInBackground(Void... v) {
        TmdbMovies movies = new TmdbApi("7e8f60e325cd06e164799af1e317d7a7").getMovies();
        movie = movies.getMovie(5353, "en");
        return movie;
    }

    protected void onPostExecute(MovieDb movie) {
        // Do something with movie
        Toast.makeText(getApplicationContext(),movie.toString(),Toast.LENGTH_LONG).show();
    }
}
}


