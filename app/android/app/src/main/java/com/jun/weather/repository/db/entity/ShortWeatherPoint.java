package com.jun.weather.repository.db.entity;

import androidx.annotation.NonNull;

public class ShortWeatherPoint {
    public String deg_1,deg_2,deg_3;
    public int x,y;
    public String mid_temp_code, mid_weather_code;

    @Override
    @NonNull
    public String toString() {
        return deg_1+(deg_2.isEmpty()?"":" "+deg_2)+(deg_3.isEmpty()?deg_3:" "+deg_3);
    }
}
