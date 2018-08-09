package com.mapprr.githubsearch.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ProfileModel implements Parcelable {

    public String name;
    public String fullName;
    public String userImageUrl;
    public Double watchers;
    public Double forks;
    public Double commits;
    public String projectUrl;
    public String description;
    public String contributorsUrl;

    public ProfileModel(String name, String fullName, String userImageUrl, Double watchers, Double forks, String projectUrl, String description, String contributorsUrl) {
        this.name = name;
        this.fullName = fullName;
        this.userImageUrl = userImageUrl;
        this.watchers = watchers;
        this.forks = forks;
        this.projectUrl = projectUrl;
        this.description = description;
        this.contributorsUrl = contributorsUrl;
    }

    protected ProfileModel(Parcel in) {
        name = in.readString();
        fullName = in.readString();
        userImageUrl = in.readString();
        if (in.readByte() == 0) {
            watchers = null;
        } else {
            watchers = in.readDouble();
        }
        if (in.readByte() == 0) {
            forks = null;
        } else {
            forks = in.readDouble();
        }
        if (in.readByte() == 0) {
            commits = null;
        } else {
            commits = in.readDouble();
        }
        projectUrl = in.readString();
        description = in.readString();
        contributorsUrl = in.readString();
    }

    public static final Creator<ProfileModel> CREATOR = new Creator<ProfileModel>() {
        @Override
        public ProfileModel createFromParcel(Parcel in) {
            return new ProfileModel(in);
        }

        @Override
        public ProfileModel[] newArray(int size) {
            return new ProfileModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(fullName);
        parcel.writeString(userImageUrl);
        if (watchers == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(watchers);
        }
        if (forks == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(forks);
        }
        if (commits == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(commits);
        }
        parcel.writeString(projectUrl);
        parcel.writeString(description);
        parcel.writeString(contributorsUrl);
    }
}
