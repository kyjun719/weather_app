package com.jun.weather.repository.web.api

import com.jun.weather.repository.web.entity.ApiResponse
import com.jun.weather.repository.web.entity.MidLandItem
import com.jun.weather.repository.web.entity.MidTempItem
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface MidWeatherInfoService {
    @GET("getMidLandFcst")
    fun getMidLandFcst(@QueryMap query: Map<String, String>): Call<ApiResponse<MidLandItem>>

    @GET("getMidTa")
    fun getMidTa(@QueryMap query: Map<String, String>): Call<ApiResponse<MidTempItem>>
}