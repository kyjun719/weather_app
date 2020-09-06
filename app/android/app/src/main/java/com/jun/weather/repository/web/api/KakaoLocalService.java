package com.jun.weather.repository.web.api;

import com.jun.weather.repository.web.api.entity.KakaoRegionCodeRes;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.QueryMap;

public interface KakaoLocalService {
    @GET("/v2/local/geo/coord2regioncode.json")
    Call<KakaoRegionCodeRes> getReverseGeocodeAddress(@Header("Authorization") String authorization, @QueryMap Map<String, Object> query);
}
