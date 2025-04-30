package com.example.findinglogs.viewmodel;


import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.findinglogs.model.model.Weather;
import com.example.findinglogs.model.repo.Repository;
import com.example.findinglogs.model.repo.remote.api.WeatherCallback;
import com.example.findinglogs.model.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainViewModel extends AndroidViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();
    private static final int FETCH_INTERVAL = 120_000;
    private final Repository mRepository;
    private final MutableLiveData<List<Weather>> _weatherList = new MutableLiveData<>(new ArrayList<>());
    private final LiveData<List<Weather>> weatherList = _weatherList;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable fetchRunnable = this::fetchAllForecasts;

    public MainViewModel(Application application) {
        super(application);
        mRepository = new Repository(application);
        startFetching();
    }

    public LiveData<List<Weather>> getWeatherList() {
        return weatherList;
    }

    private void startFetching() {
        fetchAllForecasts();
        handler.postDelayed(fetchRunnable, FETCH_INTERVAL);
    }

    private void fetchAllForecasts() {
        if (Logger.ISLOGABLE) Logger.d(TAG, "fetchAllForecasts()");

        handler.removeCallbacks(fetchRunnable);
        Set<String> uniqueCoords = new HashSet<>(mRepository.getLocalizations().values());
        List<Weather> updatedList = new ArrayList<>();

        for (String latlon : uniqueCoords) {
            mRepository.retrieveForecast(latlon, new WeatherCallback() {
                @Override
                public void onSuccess(Weather result) {
                    if (Logger.ISLOGABLE) Logger.d(TAG, "Weather recebido: " + result.toString());
                    if (Logger.ISLOGABLE) Logger.d(TAG, "Cidade recebida: " + result.getName());

                    // Verificar se a cidade já está na lista antes de adicionar
                    boolean alreadyInList = updatedList.stream()
                            .anyMatch(w -> w.getName().equalsIgnoreCase(result.getName()));

                    if (!alreadyInList) {
                        updatedList.add(result);
                    }

                    // Verificar se já finalizou todas as chamadas
                    if (updatedList.size() == uniqueCoords.size()) {
                        _weatherList.setValue(updatedList);
                        handler.postDelayed(fetchRunnable, FETCH_INTERVAL);
                    }
                }

                @Override
                public void onFailure(String error) {
                    if (Logger.ISLOGABLE) Logger.w(TAG, "Erro ao buscar previsão: " + error);
                    handler.postDelayed(fetchRunnable, FETCH_INTERVAL);
                }
            });
        }
    }


    @Override
    protected void onCleared() {
        handler.removeCallbacks(fetchRunnable);
        super.onCleared();
    }

    public void retrieveForecast(String latLon, WeatherCallback callback) {
        mRepository.retrieveForecast(latLon, callback);
    }

    public void refreshDataNow() {
        fetchAllForecasts();
    }
}