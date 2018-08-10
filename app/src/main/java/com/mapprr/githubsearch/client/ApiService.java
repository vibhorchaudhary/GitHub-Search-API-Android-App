package com.mapprr.githubsearch.client;

import com.mapprr.githubsearch.constants.ServerConstants;

import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface ApiService {

    @GET(ServerConstants.REPO)
    Observable<Response<Object>> getRepositories(@Query("q") String query);

    @GET("contributors")
    Observable<Response<Object>> getContributorDetails();

    @GET("repos")
    Observable<Response<Object>> getRepositoriesForUser();
}
