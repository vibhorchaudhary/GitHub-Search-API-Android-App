package com.mapprr.githubsearch.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mapprr.githubsearch.R;
import com.mapprr.githubsearch.activities.RepositoryActivity;
import com.mapprr.githubsearch.models.ContributorModel;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContributorsAdapter extends RecyclerView.Adapter<ContributorsAdapter.MyViewHolder> {

    private Context mContext;
    private ArrayList<ContributorModel> contributorsArrayList;

    public ContributorsAdapter(Activity context, ArrayList<ContributorModel> contributorModels) {
        this.mContext = context;
        this.contributorsArrayList = contributorModels;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View searchResultsView = LayoutInflater.from(parent.getContext()).inflate(R.layout.contributors_layout, null);
        return new MyViewHolder(searchResultsView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ContributorModel contributorModel = contributorsArrayList.get(position);
        holder.name.setText(contributorModel.name);
        Glide.with(mContext)
                .load(contributorModel.imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.userImage);
    }

    @Override
    public int getItemCount() {
        if (contributorsArrayList != null) {
            return contributorsArrayList.size();
        } else {
            return 0;
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        CircleImageView userImage;

        MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.contributorName);
            userImage = (CircleImageView) view.findViewById(R.id.userImage);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            Intent activityIntent = new Intent(mContext, RepositoryActivity.class);
                            activityIntent.putExtra("contributionModel", contributorsArrayList.get(getAdapterPosition()));
                            mContext.startActivity(activityIntent);

                            Handler handler1 = new Handler();
                            handler1.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    notifyItemChanged(getAdapterPosition());
                                }
                            }, 50);
                        }
                    }, 100);
                }
            });


        }

    }
}
