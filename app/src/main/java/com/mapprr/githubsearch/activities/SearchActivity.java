package com.mapprr.githubsearch.activities;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapprr.githubsearch.R;
import com.mapprr.githubsearch.adapters.SearchAdapter;
import com.mapprr.githubsearch.client.ServiceFactory;
import com.mapprr.githubsearch.constants.ServerConstants;
import com.mapprr.githubsearch.models.ProfileModel;
import com.mapprr.githubsearch.utils.ConnectionUtils;
import com.mapprr.githubsearch.utils.SingleToast;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.skyfishjy.library.RippleBackground;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

    @BindView(R.id.content)
    RippleBackground rippleBackground;

    private ProgressDialog pDialog;


    private boolean fabExpanded = false;

    @BindView(R.id.fabSetting)
    FloatingActionButton fab;

    @BindView(R.id.layoutFabReset)
    LinearLayout resetLayout;
    @BindView(R.id.layoutFabSortByFork)
    LinearLayout sortByForkLayout;
    @BindView(R.id.layoutFabSortByName)
    LinearLayout sortByNameLayout;
    @BindView(R.id.layoutFabSortByWatcher)
    LinearLayout sortByWatcherLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        ButterKnife.bind(this);
        noResultsFoundLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        closeSubMenusFab();

        setOnClickListeners();

    }

    private void reInit() {
        setProgressBar();
        ArrayList<ProfileModel> arrayList = getFinalListOfRepos();
        searchAdapter = new SearchAdapter(SearchActivity.this, arrayList, true);
        recyclerView.setAdapter(searchAdapter);
        hideProgressBar();
    }

    private void setOnClickListeners() {
        resetLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortByWatchers();
                reInit();
                closeSubMenusFab();
                SingleToast.showToast(SearchActivity.this, "Done Resetting", Toast.LENGTH_SHORT);
            }
        });

        sortByWatcherLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortByWatchers();
                reInit();
                closeSubMenusFab();
                SingleToast.showToast(SearchActivity.this, "Sorted by watchers", Toast.LENGTH_SHORT);
            }
        });

        sortByNameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortByName();
                reInit();
                closeSubMenusFab();
                SingleToast.showToast(SearchActivity.this, "Sorted by name", Toast.LENGTH_SHORT);
            }
        });

        sortByForkLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortByForks();
                reInit();
                closeSubMenusFab();
                SingleToast.showToast(SearchActivity.this, "Sorted by forks", Toast.LENGTH_SHORT);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSupportActionBar(toolbar);
        toolbarTitleTextView.setText(getString(R.string.app_name));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rippleBackground.isRippleAnimationRunning()) {
                    if (fabExpanded) {
                        closeSubMenusFab();
                    } else {
                        openSubMenusFab();
                    }
                } else {
                    SingleToast.showToast(SearchActivity.this, "You are not allowed to use this functionality without pulling the data!!", Toast.LENGTH_LONG);
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                searchView.showSearch(true);
            }
        }, 500);
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
        searchView.hideKeyboard(getWindow().getDecorView().getRootView());
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
        } else if (fabExpanded) {
            closeSubMenusFab();
        } else {
            if (doubleBackToExitPressedOnce) {
                finish();
            }
            this.doubleBackToExitPressedOnce = true;
            SingleToast.showToast(this, getString(R.string.press_back_to_exit) + " " + getString(R.string.app_name), Toast.LENGTH_SHORT);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }


    private void getRepos(String queryText) {

        if (!ConnectionUtils.isConnected()) {
            SingleToast.showToast(this, "No Internet Connection. Please try again later!", Toast.LENGTH_SHORT);
            return;
        }

        setProgressBar();

        ServiceFactory serviceFactory = new ServiceFactory(ServerConstants.BASE_URL, this);
        serviceFactory.getBaseService()
                .getRepositories(queryText.trim())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Response<Object>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("ERROR", e.toString());
                        hideProgressBar();
                        SingleToast.showToast(SearchActivity.this, "Something went wrong. Please try again later", Toast.LENGTH_SHORT);
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

                                        ArrayList<ProfileModel> arrayList = getFinalListOfRepos();

                                        searchAdapter = new SearchAdapter(SearchActivity.this, arrayList, true);

                                        GridLayoutManager mLayoutManager = new GridLayoutManager(SearchActivity.this, 2);
                                        recyclerView.setLayoutManager(mLayoutManager);
                                        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
                                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                                        recyclerView.setAdapter(searchAdapter);

                                        noResultsFoundLayout.setVisibility(View.GONE);
                                        recyclerView.setVisibility(View.VISIBLE);
                                        rippleBackground.startRippleAnimation();
                                        hideProgressBar();
                                    } else {
                                        noResultsFoundLayout.setVisibility(View.VISIBLE);
                                        recyclerView.setVisibility(View.GONE);
                                        hideProgressBar();
                                        rippleBackground.stopRippleAnimation();
                                        SingleToast.showToast(SearchActivity.this, "No Results Found", Toast.LENGTH_LONG);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            noResultsFoundLayout.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                            hideProgressBar();
                            rippleBackground.stopRippleAnimation();
                            SingleToast.showToast(SearchActivity.this, "No Results Found", Toast.LENGTH_LONG);
                        }
                    }
                });
    }


    private ArrayList<ProfileModel> getProfileDetails(JSONArray jsonArray) {
        ArrayList<ProfileModel> profiles = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject repoDetails;
            try {
                String name = "", fullName = "", url = "", projectUrl = "", desc = "", contributorsUrl = "";
                Double watchers, forks;
                repoDetails = jsonArray.getJSONObject(i);
                if (repoDetails.has("name")) {
                    name = repoDetails.getString("name") == null ? "" : repoDetails.getString("name");
                }
                if (repoDetails.has("full_name")) {
                    fullName = repoDetails.getString("full_name") == null ? "" : repoDetails.getString("full_name");
                }
                JSONObject owner = repoDetails.getJSONObject("owner");
                if (owner.has("avatar_url")) {
                    url = owner.getString("avatar_url") == null ? "" : owner.getString("avatar_url");
                }
                watchers = repoDetails.getDouble("watchers_count");
                forks = repoDetails.getDouble("forks_count");
                if (repoDetails.has("html_url")) {
                    projectUrl = repoDetails.getString("html_url") == null ? "" : repoDetails.getString("html_url");
                }
                if (repoDetails.has("description")) {
                    desc = repoDetails.getString("description") == null ? "" : repoDetails.getString("description");
                }
                if (repoDetails.has("contributors_url")) {
                    contributorsUrl = repoDetails.getString("contributors_url") == null ? "" : repoDetails.getString("contributors_url");
                }
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

    private void sortByWatchers() {
        Collections.sort(repoArrayList, new Comparator<ProfileModel>() {
            @Override
            public int compare(ProfileModel profileModel1, ProfileModel profileModel2) {
                return profileModel2.watchers.compareTo(profileModel1.watchers);
            }
        });
    }

    private void sortByForks() {
        Collections.sort(repoArrayList, new Comparator<ProfileModel>() {
            @Override
            public int compare(ProfileModel profileModel1, ProfileModel profileModel2) {
                return profileModel2.forks.compareTo(profileModel1.forks);
            }
        });
    }

    private void sortByName() {
        Collections.sort(repoArrayList, new Comparator<ProfileModel>() {
            @Override
            public int compare(ProfileModel profileModel1, ProfileModel profileModel2) {
                return profileModel1.name.compareTo(profileModel2.name);
            }
        });
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    private void setProgressBar() {
        if (pDialog == null) {
            pDialog = new ProgressDialog(this, R.style.Theme_ProgressDialog);
        }
        if (!pDialog.isShowing() && !this.isFinishing()) {
            pDialog.setMessage("Fetching Info! Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
    }

    private void hideProgressBar() {
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.cancel();
        }
    }

    private void closeSubMenusFab() {
        resetLayout.setVisibility(View.INVISIBLE);
        sortByForkLayout.setVisibility(View.INVISIBLE);
        sortByNameLayout.setVisibility(View.INVISIBLE);
        sortByWatcherLayout.setVisibility(View.INVISIBLE);
        fabExpanded = false;
    }

    private void openSubMenusFab() {
        resetLayout.setVisibility(View.VISIBLE);
        sortByForkLayout.setVisibility(View.VISIBLE);
        sortByNameLayout.setVisibility(View.VISIBLE);
        sortByWatcherLayout.setVisibility(View.VISIBLE);
        fabExpanded = true;
    }

    private ArrayList<ProfileModel> getFinalListOfRepos() {
        if (repoArrayList.size() > 10) {
            List<ProfileModel> listOfProfiles = repoArrayList.subList(0, 10);
            ArrayList<ProfileModel> newArrayListOfProfiles = new ArrayList<>(listOfProfiles.size());
            newArrayListOfProfiles.addAll(listOfProfiles);
            return newArrayListOfProfiles;
        } else {
            return repoArrayList;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (fabExpanded) {
            closeSubMenusFab();
        }
    }
}
