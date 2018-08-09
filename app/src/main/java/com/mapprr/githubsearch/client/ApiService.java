package com.mapprr.githubsearch.client;

import com.mapprr.githubsearch.constants.ServerConstants;

import org.json.JSONObject;

import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface ApiService {

    @GET(ServerConstants.REPO)
    Observable<Response<Object>> getRepositories(@Query("q") String query);

}
