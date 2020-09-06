package com.jun.weather.repository.web.api.entity;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class KakaoRegionCodeRes {
    @SerializedName("meta")
    public Meta meta;

    public static class Meta {
        @SerializedName("total_count")
        int total_count;
    }

    @SerializedName("documents")
    public List<RegionItem> documents;

    public static class RegionItem {
        @SerializedName("region_type")
        public String region_type;

        @SerializedName("code")
        public String code;

        @SerializedName("address_name")
        public String address_name;

        @SerializedName("region_1depth_name")
        public String region_1depth_name;

        @SerializedName("region_2depth_name")
        public String region_2depth_name;

        @SerializedName("region_3depth_name")
        public String region_3depth_name;

        @SerializedName("region_4depth_name")
        public String region_4depth_name;

        @SerializedName("x")
        public double x;

        @SerializedName("y")
        public double y;
    }

    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
