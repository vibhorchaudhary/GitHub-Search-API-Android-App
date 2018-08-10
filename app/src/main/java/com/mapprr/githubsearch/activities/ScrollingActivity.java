package com.mapprr.githubsearch.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.mapprr.githubsearch.R;
import com.mapprr.githubsearch.adapters.ContributorsAdapter;
import com.mapprr.githubsearch.client.ServiceFactory;
import com.mapprr.githubsearch.models.ContributorModel;
import com.mapprr.githubsearch.models.ProfileModel;
import com.mapprr.githubsearch.utils.ConnectionUtils;
import com.mapprr.githubsearch.utils.SingleToast;

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

public class ScrollingActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.app_bar)
    AppBarLayout appBarLayout;
    @BindView(R.id.userImage)
    CircleImageView userImage;
    @BindView(R.id.projectLink)
    TextView projectLink;
    @BindView(R.id.description)
    TextView description;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private ProgressDialog pDialog;

    @BindView(R.id.contributors)
    TextView contributorsTv;

    private ProfileModel profileModel;

    private ArrayList<ContributorModel> contributorModels;
    private ContributorsAdapter contributorsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);

        ButterKnife.bind(this);

        profileModel = getIntent().getParcelableExtra("profileModel");

        Glide.with(this)
                .load(profileModel.userImageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(userImage);

        getContributorsListFromServer();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }
        collapsingToolbarLayout.setTitle(profileModel.name);
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbarLayout.setExpandedTitleColor(Color.BLACK);

        projectLink.setText(profileModel.projectUrl);
        projectLink.setTextColor(Color.BLUE);
        projectLink.getPaint().setUnderlineText(true);
        description.setText(profileModel.description);

        projectLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ScrollingActivity.this, WebViewActivity.class);
                intent.putExtra("url", profileModel.projectUrl);
                intent.putExtra("name", profileModel.name);
                startActivity(intent);
            }
        });
    }


    private void getContributorsListFromServer() {

        if (!ConnectionUtils.isConnected()) {
            SingleToast.showToast(this, "No Internet Connection. Please try again later!", Toast.LENGTH_SHORT);
            return;
        }

        setProgressBar();
        ServiceFactory serviceFactory = new ServiceFactory(profileModel.contributorsUrl.replace("contributors", ""), this);
        serviceFactory.getBaseService()
                .getContributorDetails()
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
                    public void onNext(Response<Object> objectResponse) {
                        contributorModels = new ArrayList<>();
                        if (objectResponse.code() == 200) {
                            String responseString = new Gson().toJson(objectResponse.body());
                            JSONArray jsonArray;
                            try {
                                jsonArray = new JSONArray(responseString);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String name = jsonObject.getString("login");
                                    String avatar = jsonObject.getString("avatar_url");
                                    String repoUrl = jsonObject.getString("repos_url");
                                    contributorModels.add(new ContributorModel(name, avatar, repoUrl));
                                }
                                contributorsAdapter = new ContributorsAdapter(ScrollingActivity.this, contributorModels);
                                GridLayoutManager mLayoutManager = new GridLayoutManager(ScrollingActivity.this, 2);
                                recyclerView.setLayoutManager(mLayoutManager);
                                recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
                                recyclerView.setItemAnimator(new DefaultItemAnimator());
                                recyclerView.setAdapter(contributorsAdapter);
                                hideProgressBar();
                                if (contributorModels.size() == 0) {
                                    contributorsTv.setVisibility(View.GONE);
                                    SingleToast.showToast(ScrollingActivity.this, "No contributors found!", Toast.LENGTH_LONG);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            SingleToast.showToast(ScrollingActivity.this, "No contributors found!", Toast.LENGTH_LONG);
                            hideProgressBar();
                            contributorsTv.setVisibility(View.GONE);
                        }

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
