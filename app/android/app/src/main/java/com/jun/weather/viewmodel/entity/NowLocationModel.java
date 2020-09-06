package com.jun.weather.viewmodel.entity;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class NowLocationModel {
    public List<NowLocation> addressList = new ArrayList<>();

    public static class NowLocation {
        public String address, code, deg1, deg2, deg3;
        public double x,y;

        @NonNull
        @Override
        public String toString() {
            return new Gson().toJson(this);
        }
    }
}
