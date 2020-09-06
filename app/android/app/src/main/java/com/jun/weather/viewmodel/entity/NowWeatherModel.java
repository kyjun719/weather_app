package com.jun.weather.viewmodel.entity;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

public class NowWeatherModel {
    /*
    T1H	기온	℃	10
	RN1	1시간 강수량	mm	8
	UUU	동서바람성분	m/s	12
	VVV	남북바람성분	m/s	12
	REH	습도	%	8
	PTY	강수형태	코드값	4
	VEC	풍향	0	10
	WSD	풍속	1	10

    현재 하늘상태는 초단기예보로 가져와야 하나?
     */
    public String baseDate;
    public String baseTime;
    public String nx, ny;
    public String nowTemp;
    public String nowRain;
    public String nowHumidity;
    public String rainState;
    public String windDir;
    public String wind;
    public String nowWind;
    public String nowBaseTime;

    public String lowTemp, highTemp;
    public int nowSkyDrawableId;

    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
