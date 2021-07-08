package com.jun.weather.repository.web.api;

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
