package com.coderealities.simpletumblr;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Copyright (c) 2016 coderealities.com
 */
public class Complation implements Parcelable {
    public final String name;
    public final ArrayList<String> postList;

    public Complation(String name, ArrayList<String> postList) {
        this.name = name;
        this.postList = postList;
    }

    public int size() {
        return postList.size();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeStringList(postList);
    }

    @Override
    public String toString() {
        return name + " (" + size() + ")";
    }
}
