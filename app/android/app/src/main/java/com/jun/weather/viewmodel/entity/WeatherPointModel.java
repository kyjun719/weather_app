package com.jun.weather.viewmodel.entity;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WeatherPointModel {
    public HashMap<String, Integer[]> indexMap = new HashMap<>();
    public List<WeatherPoint> weatherPointList = new ArrayList<>();

    public static class WeatherPoint {
        public String deg_1="",deg_2="",deg_3="";
        public int x,y;
        //temp
        public String midTempCode;
        //weather
        public String midWeatherCode;

        @Override
        @NonNull
        public String toString() {
            return deg_1+(deg_2.isEmpty()?"":" "+deg_2)+(deg_3.isEmpty()?deg_3:" "+deg_3);
        }
    }
}
