package com.jun.weather.repository.web.api;

import com.jun.weather.repository.web.api.entity.ApiResponse;
import com.jun.weather.repository.web.api.entity.ForecastItem;
import com.jun.weather.repository.web.api.entity.MidLandItem;
import com.jun.weather.repository.web.api.entity.MidTempItem;
import com.jun.weather.repository.web.api.entity.ObserveItem;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface ShortWeatherInfoService {
    @GET("getUltraSrtNcst")
    Call<ApiResponse<ObserveItem>> getUltraSrtNcst(@QueryMap Map<String, Object> query);

    @GET("getVilageFcst")
    Call<ApiResponse<ForecastItem>> getVilageFcst(@QueryMap Map<String, Object> query);

    @GET("getUltraSrtFcst")
    Call<ApiResponse<ForecastItem>> getUltraSrtFcst(@QueryMap Map<String, Object> query);
}
