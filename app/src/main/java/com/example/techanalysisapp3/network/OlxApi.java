package com.example.techanalysisapp3.network;

import com.example.techanalysisapp3.model.Listing;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface OlxApi {
    @GET("listings/{id}")
    Call<Listing> getListing(@Header("Authorization") String token, @Path("id") String id);
}
