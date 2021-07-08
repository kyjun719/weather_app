package com.jun.weather.repository.web;

import android.content.Context;

import com.jun.weather.R;
import com.jun.weather.repository.web.api.APIClient;
import com.jun.weather.repository.web.api.KakaoLocalService;
import com.jun.weather.repository.web.api.KakaoRegionCodeRes;
import com.jun.weather.repository.web.entity.Enum;
import com.jun.weather.repository.web.entity.KakaoLocationInfoCacheKey;
import com.jun.weather.repository.web.entity.RestResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class KakaoLocationRepository {
    private static KakaoLocationRepository instance;
    private static String AUTHORIZATION_KAKAO;

    public static KakaoLocationRepository getInstance(Context context) {
        if(instance == null) {
            instance = new KakaoLocationRepository();
            AUTHORIZATION_KAKAO = context.getResources().getString(R.string.key_kakao_auth);
        }
        return instance;
    }

    private HashMap<KakaoLocationInfoCacheKey, RestResponse<?>> map;
    private KakaoLocationRepository() {
        map = new HashMap<>();
    }

    private <T>boolean isRequestSuccess(RestResponse<T> restResponse,
                                        Response<T> response)
            throws IOException {
        if(!response.isSuccessful()) {
            restResponse.code = response.code();
            ResponseBody body = response.errorBody();
            restResponse.failMsg = body==null?"":body.string();
            return false;
        }

        T body = response.body();
        if(body == null) {
            restResponse.code = Enum.ResponseCode.EXCEPTION_ERROR.getValue();
            restResponse.failMsg = "body is null";
            return false;
        }

        return true;
    }

    private KakaoLocationInfoCacheKey createCacheKey(String path, double x, double y) {
        KakaoLocationInfoCacheKey key = new KakaoLocationInfoCacheKey();
        key.urlPath = path;
        key.x = x;
        key.y = y;
        return key;
    }

    private <T> void addToCacheMap(String path, double x, double y, RestResponse<T> restResponse) {
        map.put(createCacheKey(path, x, y), restResponse);
    }

    private boolean containsInCache(String path, double x, double y) {
        return map.get(createCacheKey(path, x, y)) != null;
    }

    @SuppressWarnings("unchecked")
    public RestResponse<KakaoRegionCodeRes> getLocationStringAddress(double lng, double lat) {
        KakaoLocalService kakaoLocalService = APIClient.getClient(APIClient.CLIENT_DEST.KAKAO_LOCAL).create(KakaoLocalService.class);
        Map<String, Object> query = new HashMap<>();
        query.put("x", ""+lng);
        query.put("y", ""+lat);
        Call<KakaoRegionCodeRes> call = kakaoLocalService.getReverseGeocodeAddress(AUTHORIZATION_KAKAO, query);
        String urlPath  = call.request().url().encodedPath();

        if(containsInCache(urlPath, lng, lat)) {
            return (RestResponse<KakaoRegionCodeRes>) map.get(createCacheKey(urlPath, lng, lat));
        }

        RestResponse<KakaoRegionCodeRes> restResponse = new RestResponse<>();

        try {
            Response<KakaoRegionCodeRes> response = call.execute();

            if(isRequestSuccess(restResponse, response)) {
                KakaoRegionCodeRes body = response.body();
                if(body == null) {
                    throw new Exception();
                }
                restResponse.code = response.code();
                restResponse.singleBody = body;

                addToCacheMap(urlPath, lng, lat, restResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();

            restResponse.code = Enum.ResponseCode.EXCEPTION_ERROR.getValue();
            restResponse.failMsg = e.getMessage();
        }
        return restResponse;
    }
}
