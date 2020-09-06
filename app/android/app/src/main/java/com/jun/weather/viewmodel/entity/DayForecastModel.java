package com.jun.weather.viewmodel.entity;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.time.format.DateTimeFormatter;

public class DayForecastModel {
    /*
    POP	강수확률	%	8
	PTY	강수형태	코드값	4
	R06	6시간 강수량	범주 (1 mm)	8
	REH	습도	%	8
	S06	6시간 신적설	범주(1 cm)	8
	SKY	하늘상태	코드값	4
	T3H	3시간 기온	℃	10
	WAV	파고	M	8
	VEC	풍향	m/s	10
	WSD	풍속	1	10
     */
    public String forecastDate;
    public String forecastTime;
    public String nx, ny;
    public String forecastRainPercentage;
    public String forecastRainState;
    public String forecastRain;
    public String forecastHumidity;
    public String forecastSnow;
    public String forecastSky;
    public String forecastTemp;
    public String forecastWave;
    public String forecastWindDir;
    public String forecastWind;
    public String forecastLowTemp;
    public String forecastHighTemp;
    public int forecastSkyDrawableId;

    public DateTime getDateTime() {
        return DateTimeFormat.forPattern("yyyyMMdd HHmm").parseDateTime(forecastDate+" "+forecastTime);
    }

    public String getHourString() {
        return getDateTime().toString("HH시");
    }

    public String getDayString() {
        return getDateTime().toString("dd일");
    }

    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
