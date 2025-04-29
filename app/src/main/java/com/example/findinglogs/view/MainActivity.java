package com.example.findinglogs.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findinglogs.R;
import com.example.findinglogs.model.model.Weather;
import com.example.findinglogs.view.recyclerview.adapter.WeatherListAdapter;
import com.example.findinglogs.viewmodel.MainViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private WeatherListAdapter adapter;
    private final List<Weather> weathers = new ArrayList<>();
    private FloatingActionButton fetchButton;
    private ProgressBar progressBar;
    private MainViewModel mainViewModel;
    private  RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupRecyclerView();
        setupObservers();
        setupListeners();
    }

    private void initViews() {
        progressBar = findViewById(R.id.progressBar);
        fetchButton = findViewById(R.id.fetchButton);
        recyclerView = findViewById(R.id.recycler_view_weather);
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        adapter = new WeatherListAdapter(this, weathers);
    }

    private void setupRecyclerView() {
        recyclerView.setAdapter(adapter);
    }

    private void setupObservers() {
        mainViewModel.getWeatherList().observe(this, updatedWeathers -> {
            adapter.updateWeathers(updatedWeathers);
            progressBar.setVisibility(View.GONE);
        });
    }

    private void setupListeners() {
        fetchButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            mainViewModel.refreshDataNow();
            Toast.makeText(MainActivity.this, "Atualizando dados...", Toast.LENGTH_SHORT).show();
        });
    }
}