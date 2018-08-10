package com.mapprr.githubsearch.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ContributorModel implements Parcelable {

    public String name;
    public String imageUrl;
    public String profileUrl;

    public ContributorModel(String name, String imageUrl, String profileUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.profileUrl = profileUrl;
    }


    protected ContributorModel(Parcel in) {
        name = in.readString();
        imageUrl = in.readString();
        profileUrl = in.readString();
    }

    public static final Creator<ContributorModel> CREATOR = new Creator<ContributorModel>() {
        @Override
        public ContributorModel createFromParcel(Parcel in) {
            return new ContributorModel(in);
        }

        @Override
        public ContributorModel[] newArray(int size) {
            return new ContributorModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(imageUrl);
        parcel.writeString(profileUrl);
    }
}
