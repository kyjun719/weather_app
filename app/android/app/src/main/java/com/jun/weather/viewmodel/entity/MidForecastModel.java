package com.jun.weather.viewmodel.entity;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

public class MidForecastModel {
    public String lowTemp, highTemp;
    public String amRainPercentage, pmRainPercentage, dayRainPercentage;
    public int amSky, pmSky, daySky;
    public String baseDate;

    public static class Builder {
        private String lowTemp="", highTemp="";
        private String amRainPercentage="", pmRainPercentage="", dayRainPercentage="";
        private int amSky=0, pmSky=0, daySky=0;
        private String baseDate;

        public Builder lowTemp(String lowTemp) {
            this.lowTemp = lowTemp;
            return this;
        }

        public Builder highTemp(String highTemp) {
            this.highTemp = highTemp;
            return this;
        }

        public Builder amRainPercentage(String amRainPercentage) {
            this.amRainPercentage = amRainPercentage;
            return this;
        }

        public Builder pmRainPercentage(String pmRainPercentage) {
            this.pmRainPercentage = pmRainPercentage;
            return this;
        }

        public Builder dayRainPercentage(String dayRainPercentage) {
            this.dayRainPercentage = dayRainPercentage;
            return this;
        }

        public Builder amSky(int amSky) {
            this.amSky = amSky;
            return this;
        }

        public Builder pmSky(int pmSky) {
            this.pmSky = pmSky;
            return this;
        }

        public Builder daySky(int daySky) {
            this.daySky = daySky;
            return this;
        }

        public Builder baseDate(String baseDate) {
            this.baseDate = baseDate;
            return this;
        }

        public MidForecastModel build() {
            return new MidForecastModel(this);
        }
    }

    private MidForecastModel(Builder builder) {
        lowTemp=builder.lowTemp;
        highTemp=builder.highTemp;
        amRainPercentage=builder.amRainPercentage;
        pmRainPercentage=builder.pmRainPercentage;
        dayRainPercentage=builder.dayRainPercentage;
        amSky=builder.amSky;
        pmSky=builder.pmSky;
        daySky=builder.daySky;
        baseDate=builder.baseDate;
    }

    public MidForecastModel() {}

    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
