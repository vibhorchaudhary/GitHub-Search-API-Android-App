package com.mapprr.githubsearch.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mapprr.githubsearch.R;
import com.mapprr.githubsearch.activities.ScrollingActivity;
import com.mapprr.githubsearch.models.ProfileModel;

import java.text.DecimalFormat;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.MyViewHolder> {

    private Context mContext;
    private ArrayList<ProfileModel> profileModels;
    private boolean shouldDisplayImage;

    public SearchAdapter(Activity context, ArrayList<ProfileModel> profiles, boolean shouldDisplayImage) {
        this.mContext = context;
        this.profileModels = profiles;
        this.shouldDisplayImage = shouldDisplayImage;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View searchResultsView = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_results_layout_grid, null);
        return new MyViewHolder(searchResultsView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ProfileModel profileModel = profileModels.get(position);
        holder.name.setText(profileModel.name);
        holder.fullName.setText(profileModel.fullName);
        holder.watchers.setText(new DecimalFormat("#").format(profileModel.watchers));
        holder.forks.setText(new DecimalFormat("#").format(profileModel.forks));
        if (shouldDisplayImage) {
            Glide.with(mContext)
                    .load(profileModel.userImageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.userImage);
        }
    }

    @Override
    public int getItemCount() {
        if (profileModels != null) {
            return profileModels.size();
        } else {
            return 0;
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name, fullName, watchers, forks;
        CardView cardView;
        CircleImageView userImage;

        MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            fullName = (TextView) view.findViewById(R.id.fullName);
            watchers = (TextView) view.findViewById(R.id.watchers);
            forks = (TextView) view.findViewById(R.id.fork);
            cardView = (CardView) view.findViewById(R.id.card_view);
            userImage = (CircleImageView) view.findViewById(R.id.avatar);

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                cardView.setCardElevation(0);
                cardView.setMaxCardElevation(0);
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            Intent activityIntent = new Intent(mContext, ScrollingActivity.class);
                            activityIntent.putExtra("profileModel", profileModels.get(getAdapterPosition()));
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
