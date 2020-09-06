package com.jun.weather.repository.web.api.entity;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class MidLandItem {
    @SerializedName("regId")
    public String regId;
    @SerializedName("rnSt3Am")
    public String rnSt3Am;
    @SerializedName("rnSt3Pm")
    public String rnSt3Pm;
    @SerializedName("rnSt4Am")
    public String rnSt4Am;
    @SerializedName("rnSt4Pm")
    public String rnSt4Pm;
    @SerializedName("rnSt5Am")
    public String rnSt5Am;
    @SerializedName("rnSt5Pm")
    public String rnSt5Pm;
    @SerializedName("rnSt6Am")
    public String rnSt6Am;
    @SerializedName("rnSt6Pm")
    public String rnSt6Pm;
    @SerializedName("rnSt7Am")
    public String rnSt7Am;
    @SerializedName("rnSt7Pm")
    public String rnSt7Pm;
    @SerializedName("rnSt8")
    public String rnSt8;
    @SerializedName("rnSt9")
    public String rnSt9;
    @SerializedName("rnSt10")
    public String rnSt10;

    @SerializedName("wf3Am")
    public String wf3Am;
    @SerializedName("wf3Pm")
    public String wf3Pm;
    @SerializedName("wf4Am")
    public String wf4Am;
    @SerializedName("wf4Pm")
    public String wf4Pm;
    @SerializedName("wf5Am")
    public String wf5Am;
    @SerializedName("wf5Pm")
    public String wf5Pm;
    @SerializedName("wf6Am")
    public String wf6Am;
    @SerializedName("wf6Pm")
    public String wf6Pm;
    @SerializedName("wf7Am")
    public String wf7Am;
    @SerializedName("wf7Pm")
    public String wf7Pm;
    @SerializedName("wf8")
    public String wf8;
    @SerializedName("wf9")
    public String wf9;
    @SerializedName("wf10")
    public String wf10;

    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
