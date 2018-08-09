package com.mapprr.githubsearch.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.mapprr.githubsearch.R;
import com.mapprr.githubsearch.models.ProfileModel;

public class ProjectDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_detail_layout);
        ProfileModel profileModel = getIntent().getParcelableExtra("profileModel");
    }
}
