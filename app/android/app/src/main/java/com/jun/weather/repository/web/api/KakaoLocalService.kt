package com.jun.weather.repository.web.api

import com.jun.weather.repository.web.entity.KakaoRegionCodeRes
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.QueryMap

interface KakaoLocalService {
    @GET("/v2/local/geo/coord2regioncode.json")
    fun getReverseGeocodeAddress(@Header("Authorization") authorization: String, @QueryMap query: Map<String, String>?): Call<KakaoRegionCodeRes>
}