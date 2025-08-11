package com.example.techanalysisapp3.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OlxApiService {
    private static final String BASE_URL = "https://api.olx.ba/";
    private static OlxApi instance;

    public static OlxApi getInstance() {
        if (instance == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            instance = retrofit.create(OlxApi.class);
        }
        return instance;
    }
}

