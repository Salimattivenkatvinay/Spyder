package com.vinay.spyder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vinay.spyder.R;

import java.util.ArrayList;

public class RatingActivity extends AppCompatActivity {

    RecyclerView rv_movie;
    ArrayList<String> mvieId=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating1);

        rv_movie=findViewById(R.id.rv_movies);
        MovieAdapter movieAdapter=new MovieAdapter();
        mvieId.add("155");
        mvieId.add("155");
        mvieId.add("155");
        mvieId.add("155");
        mvieId.add("155");
        mvieId.add("155");
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(RatingActivity.this, LinearLayoutManager.VERTICAL,false);
        rv_movie.setLayoutManager(layoutManager);
        rv_movie.setAdapter(movieAdapter);

        findViewById(R.id.btn_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RatingActivity.this,MainActivity1.class));
            }
        });
    }

    class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.Myholder>{

        class Myholder extends RecyclerView.ViewHolder{
            TextView textView;

            public Myholder(View itemView) {
                super(itemView);
                textView=itemView.findViewById(R.id.some);
            }
        }

        @Override
        public Myholder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.movie_list_row, parent, false);

            return new Myholder(itemView);
        }

        @Override
        public void onBindViewHolder(Myholder holder, int position) {
            holder.textView.setText(mvieId.get(position));
        }

        @Override
        public int getItemCount() {
            return mvieId.size();
        }

    }
}
