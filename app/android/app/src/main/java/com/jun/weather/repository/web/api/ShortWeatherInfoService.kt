package com.jun.weather.repository.web.api

import com.jun.weather.repository.web.entity.ApiResponse
import com.jun.weather.repository.web.entity.ForecastItem
import com.jun.weather.repository.web.entity.ObserveItem
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface ShortWeatherInfoService {
    @GET("getUltraSrtNcst")
    fun getUltraSrtNcst(@QueryMap query: Map<String, String>): Call<ApiResponse<ObserveItem>>

    @GET("getVilageFcst")
    fun getVilageFcst(@QueryMap query: Map<String, String>): Call<ApiResponse<ForecastItem>>

    @GET("getUltraSrtFcst")
    fun getUltraSrtFcst(@QueryMap query: Map<String, String>): Call<ApiResponse<ForecastItem>>
}