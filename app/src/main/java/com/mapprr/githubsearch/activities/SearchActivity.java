package com.mapprr.githubsearch.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapprr.githubsearch.R;
import com.mapprr.githubsearch.adapters.SearchAdapter;
import com.mapprr.githubsearch.client.ServiceFactory;
import com.mapprr.githubsearch.constants.ServerConstants;
import com.mapprr.githubsearch.models.ProfileModel;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Response;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SearchActivity extends AppCompatActivity implements MaterialSearchView.OnQueryTextListener {

    @BindView(R.id.search_view)
    MaterialSearchView searchView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitleTextView;
    private boolean doubleBackToExitPressedOnce = false;

    private ArrayList<ProfileModel> repoArrayList;
    private SearchAdapter searchAdapter;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.noResultsFound)
    RelativeLayout noResultsFoundLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        ButterKnife.bind(this);
        noResultsFoundLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSupportActionBar(toolbar);
        toolbarTitleTextView.setText("Search on GitHub");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        searchView.showSearch(true);
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                searchView.showSearch(true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        getRepos(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            if (doubleBackToExitPressedOnce) {
                finish();
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getString(R.string.press_back_to_exit) + " " + getString(R.string.app_name), Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }


    private void getRepos(String queryText) {
        ServiceFactory serviceFactory = new ServiceFactory(ServerConstants.BASE_URL, this);
        serviceFactory.getBaseService()
                .getRepositories(queryText)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Response<Object>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("ERROR", e.toString());
                    }

                    @Override
                    public void onNext(Response<Object> jsonObjectResponse) {
                        if (jsonObjectResponse.code() == 200) {
                            String responseString = new Gson().toJson(jsonObjectResponse.body());
                            JSONObject jsonObject;
                            try {
                                jsonObject = new JSONObject(responseString);
                                if (jsonObject.get("items") != null) {
                                    JSONArray repos = (JSONArray) jsonObject.get("items");
                                    if (repos.length() != 0) {
                                        repoArrayList = getProfileDetails(repos);
                                        searchAdapter = new SearchAdapter(SearchActivity.this, repoArrayList);
                                        recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
                                        recyclerView.setAdapter(searchAdapter);
                                        noResultsFoundLayout.setVisibility(View.GONE);
                                        recyclerView.setVisibility(View.VISIBLE);
                                    } else {
                                        noResultsFoundLayout.setVisibility(View.VISIBLE);
                                        recyclerView.setVisibility(View.GONE);
                                        Toast.makeText(SearchActivity.this, "No Results Found", Toast.LENGTH_LONG).show();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                });
    }


    private ArrayList<ProfileModel> getProfileDetails(JSONArray jsonArray) {
        ArrayList<ProfileModel> profiles = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject repoDetails = null;
            try {
                repoDetails = jsonArray.getJSONObject(i);
                String name = repoDetails.getString("name");
                String fullName = repoDetails.getString("full_name");
                JSONObject owner = repoDetails.getJSONObject("owner");
                String url = owner.getString("avatar_url");
                Double watchers = repoDetails.getDouble("watchers_count");
                Double forks = repoDetails.getDouble("forks_count");
                String projectUrl = repoDetails.getString("html_url");
                String desc = repoDetails.getString("description");
                String contributorsUrl = repoDetails.getString("contributors_url");
                ProfileModel profileModel = new ProfileModel(name, fullName, url, watchers, forks, projectUrl, desc, contributorsUrl);
                profiles.add(profileModel);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Collections.sort(profiles, new Comparator<ProfileModel>() {
            @Override
            public int compare(ProfileModel profileModel1, ProfileModel profileModel2) {
                return profileModel2.watchers.compareTo(profileModel1.watchers);
            }
        });

        return profiles;
    }
}
