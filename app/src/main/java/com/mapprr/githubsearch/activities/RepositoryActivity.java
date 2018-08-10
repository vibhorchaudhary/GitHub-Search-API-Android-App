package com.mapprr.githubsearch.activities;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.mapprr.githubsearch.R;
import com.mapprr.githubsearch.adapters.SearchAdapter;
import com.mapprr.githubsearch.client.ServiceFactory;
import com.mapprr.githubsearch.models.ContributorModel;
import com.mapprr.githubsearch.models.ProfileModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Response;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RepositoryActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.app_bar)
    AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.userImage)
    CircleImageView userImage;

    private ProgressDialog pDialog;

    private ContributorModel contributorModel;
    private ArrayList<ProfileModel> profileModels;
    private SearchAdapter searchAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repositories);
        ButterKnife.bind(this);
        contributorModel = getIntent().getParcelableExtra("contributionModel");

        Glide.with(this)
                .load(contributorModel.imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(userImage);

        getRepositories();

    }

    @Override
    protected void onResume() {
        super.onResume();
        collapsingToolbarLayout.setTitle(contributorModel.name);
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbarLayout.setExpandedTitleColor(Color.BLACK);
    }

    private void getRepositories() {
        setProgressBar();
        recyclerView.setVisibility(View.GONE);
        ServiceFactory serviceFactory = new ServiceFactory(contributorModel.profileUrl.replace("repos", ""), this);
        serviceFactory.getBaseService()
                .getRepositoriesForUser()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Response<Object>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Response<Object> objectResponse) {
                        if (objectResponse.code() == 200) {
                            String responseString = new Gson().toJson(objectResponse.body());
                            JSONArray jsonArray;
                            try {
                                jsonArray = new JSONArray(responseString);
                                profileModels = getProfileDetails(jsonArray);
                                searchAdapter = new SearchAdapter(RepositoryActivity.this, profileModels, false);
                                GridLayoutManager mLayoutManager = new GridLayoutManager(RepositoryActivity.this, 2);
                                recyclerView.setLayoutManager(mLayoutManager);
                                recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
                                recyclerView.setItemAnimator(new DefaultItemAnimator());
                                recyclerView.setAdapter(searchAdapter);
                                recyclerView.setVisibility(View.VISIBLE);
                                hideProgressBar();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                hideProgressBar();
                            }
                        }
                    }
                });
    }

    private ArrayList<ProfileModel> getProfileDetails(JSONArray jsonArray) {
        ArrayList<ProfileModel> profiles = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject repoDetails;
            try {
                repoDetails = jsonArray.getJSONObject(i);
                String name = repoDetails.getString("name") == null ? "" : repoDetails.getString("name");
                String fullName = repoDetails.getString("full_name") == null ? "" : repoDetails.getString("full_name");
                JSONObject owner = repoDetails.getJSONObject("owner");
                String url = owner.getString("avatar_url") == null ? "" : owner.getString("avatar_url");
                Double watchers = repoDetails.getDouble("watchers_count");
                Double forks = repoDetails.getDouble("forks_count");
                String projectUrl = repoDetails.getString("html_url") == null ? "" : repoDetails.getString("html_url");
                String desc = repoDetails.getString("description") == null ? "" : repoDetails.getString("description");
                String contributorsUrl = repoDetails.getString("contributors_url") == null ? "" : repoDetails.getString("contributors_url");
                ProfileModel profileModel = new ProfileModel(name, fullName, url, watchers, forks, projectUrl, desc, contributorsUrl);
                profiles.add(profileModel);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return profiles;
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
            pDialog.setMessage("Loading! Please wait...");
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


}
