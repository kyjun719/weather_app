package com.jun.weather.repository.web;

import android.content.Context;

import com.jun.weather.R;
import com.jun.weather.repository.web.api.APIClient;
import com.jun.weather.repository.web.api.ApiResponse;
import com.jun.weather.repository.web.api.ForecastItem;
import com.jun.weather.repository.web.api.MidLandItem;
import com.jun.weather.repository.web.api.MidTempItem;
import com.jun.weather.repository.web.api.MidWeatherInfoService;
import com.jun.weather.repository.web.api.ObserveItem;
import com.jun.weather.repository.web.api.ShortWeatherInfoService;
import com.jun.weather.repository.web.entity.WeatherInfoCacheKey;
import com.jun.weather.repository.web.entity.Enum;
import com.jun.weather.repository.web.entity.RestResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class WeatherInfoRepository {
    private static WeatherInfoRepository instance;
    private static String KEY_WEATHER_FORECAST;

    public static WeatherInfoRepository getInstance(Context context) {
        if(instance == null) {
            instance = new WeatherInfoRepository();
            KEY_WEATHER_FORECAST = context.getResources().getString(R.string.key_weather);
        }
        return instance;
    }

    private HashMap<WeatherInfoCacheKey, RestResponse<?>> map;
    private WeatherInfoRepository() {
        map = new HashMap<>();
    }

    private <T>boolean isRequestSuccess(RestResponse<T> restResponse,
                                        Response<ApiResponse<T>> response)
            throws IOException {
        if(!response.isSuccessful()) {
            restResponse.code = response.code();
            ResponseBody body = response.errorBody();
            restResponse.failMsg = body==null?"":body.string();
            return false;
        }

        ApiResponse<T> body = response.body();
        if(body == null) {
            restResponse.code = Enum.ResponseCode.EXCEPTION_ERROR.getValue();
            restResponse.failMsg = "body is null";
            return false;
        }

        if(!body.getResponse().getHeader().getResultCode().equals("00")) {
            restResponse.code = Enum.ResponseCode.BAD_REQUEST.getValue();
            restResponse.failMsg = body.getResponse().getHeader().getResultMsg();
            return false;
        }

        return true;
    }

    private WeatherInfoCacheKey createCacheKey(int ordinal, String baseDate, String baseTime, int nx, int ny) {
        return createCacheKey(ordinal, baseDate, baseTime, nx, ny, "", "");
    }

    private WeatherInfoCacheKey createCacheKey(int ordinal, String midDate, String regionId) {
        return createCacheKey(ordinal, "", "", -1, -1, midDate, regionId);
    }

    private WeatherInfoCacheKey createCacheKey(int ordinal, String baseDate, String baseTime, int nx, int ny,
                                               String midDate, String regionId) {
        WeatherInfoCacheKey key = new WeatherInfoCacheKey();
        key.key = ordinal;
        key.baseDate = baseDate;
        key.baseTime = baseTime;
        key.nx = nx;
        key.ny = ny;
        key.midDate = midDate;
        key.regionId = regionId;
        return key;
    }

    private <T> void addToCacheMap(int ordinal, String baseDate, String baseTime, int nx, int ny, RestResponse<T> restResponse) {
        map.put(createCacheKey(ordinal, baseDate, baseTime, nx, ny), restResponse);
    }

    private boolean containsInCache(int ordinal, String baseDate, String baseTime, int nx, int ny) {
        return map.get(createCacheKey(ordinal, baseDate, baseTime, nx, ny)) != null;
    }

    private <T> void addToCacheMap(int ordinal, String midDate, String regionId, RestResponse<T> restResponse) {
        map.put(createCacheKey(ordinal, midDate, regionId), restResponse);
    }

    private boolean containsInCache(int ordinal, String midDate, String regionId) {
        return map.get(createCacheKey(ordinal, midDate, regionId)) != null;
    }

    @SuppressWarnings("unchecked")
    public RestResponse<ObserveItem> getNowWeather(String baseDate, String baseTime, int nx, int ny) {
        if(containsInCache(Enum.KEY.NOW.ordinal(), baseDate, baseTime, nx, ny)) {
            return (RestResponse<ObserveItem>) map.get(createCacheKey(Enum.KEY.NOW.ordinal(), baseDate, baseTime, nx, ny));
        }

        ShortWeatherInfoService shortWeatherInfoService = APIClient.getClient(APIClient.CLIENT_DEST.SHORT_WEATHER).create(ShortWeatherInfoService.class);
        Map<String, Object> map = new HashMap<>();
        map.put("serviceKey", KEY_WEATHER_FORECAST);
        map.put("numOfRows", 10);
        map.put("pageNo", 1);
        map.put("dataType", "JSON");
        map.put("base_date", baseDate);
        map.put("base_time", baseTime);
        map.put("nx", nx);
        map.put("ny", ny);

        Call<ApiResponse<ObserveItem>> call = shortWeatherInfoService.getUltraSrtNcst(map);
        RestResponse<ObserveItem> restResponse = new RestResponse<>();

        try {
            Response<ApiResponse<ObserveItem>> response = call.execute();

            if(isRequestSuccess(restResponse, response)) {
                ApiResponse<ObserveItem> body = response.body();
                if(body == null) {
                    throw new Exception();
                }
                restResponse.code = response.code();
                restResponse.listBody = body.getResponse().getBody().getItems().getItem();
                addToCacheMap(Enum.KEY.NOW.ordinal(), baseDate, baseTime, nx, ny, restResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();

            restResponse.code = Enum.ResponseCode.EXCEPTION_ERROR.getValue();
            restResponse.failMsg = e.getMessage();
        }
        return restResponse;
    }

    @SuppressWarnings("unchecked")
    public RestResponse<ForecastItem> getDayTempData(String baseDate, String baseTime, int nx, int ny, boolean isHigh) {
        int val = isHigh?Enum.KEY.HIGH_TEMP.ordinal():Enum.KEY.LOW_TEMP.ordinal();
        if(containsInCache(val, baseDate, baseTime, nx, ny)) {
            return (RestResponse<ForecastItem>) map.get(createCacheKey(val, baseDate, baseTime, nx, ny));
        }

        ShortWeatherInfoService shortWeatherInfoService = APIClient.getClient(APIClient.CLIENT_DEST.SHORT_WEATHER).create(ShortWeatherInfoService.class);
        Map<String, Object> map = new HashMap<>();
        map.put("serviceKey", KEY_WEATHER_FORECAST);
        map.put("numOfRows", 100);
        map.put("pageNo", 1);
        map.put("dataType", "JSON");
        map.put("base_date", baseDate);
        map.put("base_time", baseTime);
        map.put("nx", nx);
        map.put("ny", ny);

        Call<ApiResponse<ForecastItem>> call = shortWeatherInfoService.getVilageFcst(map);
        RestResponse<ForecastItem> restResponse = new RestResponse<>();

        try {
            Response<ApiResponse<ForecastItem>> response = call.execute();

            if(isRequestSuccess(restResponse, response)) {
                ApiResponse<ForecastItem> body = response.body();
                if(body == null) {
                    throw new Exception();
                }
                restResponse.code = response.code();
                restResponse.listBody = body.getResponse().getBody().getItems().getItem();

                addToCacheMap(val, baseDate, baseTime, nx, ny, restResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();

            restResponse.code = Enum.ResponseCode.EXCEPTION_ERROR.getValue();
            restResponse.failMsg = e.getMessage();
        }
        return restResponse;
    }

    private int MAX_ITEM_CNT = 300;
    @SuppressWarnings("unchecked")
    public RestResponse<ForecastItem> getShortForecast(String baseDate, String baseTime, int nx, int ny) {
        if(containsInCache(Enum.KEY.SHORT_FORECAST.ordinal(), baseDate, baseTime, nx, ny)) {
            return (RestResponse<ForecastItem>) map.get(createCacheKey(Enum.KEY.SHORT_FORECAST.ordinal(), baseDate, baseTime, nx, ny));
        }

        ShortWeatherInfoService shortWeatherInfoService = APIClient.getClient(APIClient.CLIENT_DEST.SHORT_WEATHER).create(ShortWeatherInfoService.class);
        Map<String, Object> map = new HashMap<>();
        map.put("serviceKey", KEY_WEATHER_FORECAST);
        map.put("numOfRows", MAX_ITEM_CNT);
        map.put("pageNo", 1);
        map.put("dataType", "JSON");
        map.put("base_date", baseDate);
        map.put("base_time", baseTime);
        map.put("nx", nx);
        map.put("ny", ny);

        Call<ApiResponse<ForecastItem>> call = shortWeatherInfoService.getVilageFcst(map);
        RestResponse<ForecastItem> restResponse = new RestResponse<>();

        try {
            Response<ApiResponse<ForecastItem>> response = call.execute();

            if(isRequestSuccess(restResponse, response)) {
                ApiResponse<ForecastItem> body = response.body();
                if(body == null) {
                    throw new Exception();
                }
                restResponse.code = response.code();
                restResponse.listBody = body.getResponse().getBody().getItems().getItem();

                addToCacheMap(Enum.KEY.SHORT_FORECAST.ordinal(), baseDate, baseTime, nx, ny, restResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();

            restResponse.code = Enum.ResponseCode.EXCEPTION_ERROR.getValue();
            restResponse.failMsg = e.getMessage();
        }
        return restResponse;
    }

    @SuppressWarnings("unchecked")
    public RestResponse<ForecastItem> getUltraShortForecast(String baseDate, String baseTime, int nx, int ny) {
        if(containsInCache(Enum.KEY.ULTRA_SHORT_FORECAST.ordinal(), baseDate, baseTime, nx, ny)) {
            return (RestResponse<ForecastItem>) map.get(createCacheKey(Enum.KEY.ULTRA_SHORT_FORECAST.ordinal(), baseDate, baseTime, nx, ny));
        }

        ShortWeatherInfoService shortWeatherInfoService = APIClient.getClient(APIClient.CLIENT_DEST.SHORT_WEATHER).create(ShortWeatherInfoService.class);
        Map<String, Object> map = new HashMap<>();
        map.put("serviceKey", KEY_WEATHER_FORECAST);
        map.put("numOfRows", MAX_ITEM_CNT);
        map.put("pageNo", 1);
        map.put("dataType", "JSON");
        map.put("base_date", baseDate);
        map.put("base_time", baseTime);
        map.put("nx", nx);
        map.put("ny", ny);

        Call<ApiResponse<ForecastItem>> call = shortWeatherInfoService.getUltraSrtFcst(map);
        RestResponse<ForecastItem> restResponse = new RestResponse<>();

        try {
            Response<ApiResponse<ForecastItem>> response = call.execute();

            if(isRequestSuccess(restResponse, response)) {
                ApiResponse<ForecastItem> body = response.body();
                if(body == null) {
                    throw new Exception();
                }
                restResponse.code = response.code();
                restResponse.listBody = body.getResponse().getBody().getItems().getItem();

                addToCacheMap(Enum.KEY.ULTRA_SHORT_FORECAST.ordinal(), baseDate, baseTime, nx, ny, restResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();

            restResponse.code = Enum.ResponseCode.EXCEPTION_ERROR.getValue();
            restResponse.failMsg = e.getMessage();
        }
        return restResponse;
    }

    @SuppressWarnings("unchecked")
    public RestResponse<MidLandItem> getMidLandForecast(String date, String regionId) {
        if(containsInCache(Enum.KEY.MID_LAND.ordinal(), date, regionId)) {
            return (RestResponse<MidLandItem>) map.get(createCacheKey(Enum.KEY.MID_LAND.ordinal(), date, regionId));
        }

        MidWeatherInfoService shortWeatherInfoService = APIClient.getClient(APIClient.CLIENT_DEST.MID_WEATHER).create(MidWeatherInfoService.class);
        Map<String, Object> map = new HashMap<>();
        map.put("serviceKey", KEY_WEATHER_FORECAST);
        map.put("numOfRows", 10);
        map.put("pageNo", 1);
        map.put("dataType", "JSON");
        map.put("regId", regionId);
        map.put("tmFc", date);

        Call<ApiResponse<MidLandItem>> call = shortWeatherInfoService.getMidLandFcst(map);
        RestResponse<MidLandItem> restResponse = new RestResponse<>();

        try {
            Response<ApiResponse<MidLandItem>> response = call.execute();

            if(isRequestSuccess(restResponse, response)) {
                ApiResponse<MidLandItem> body = response.body();
                if(body == null) {
                    throw new Exception();
                }
                restResponse.code = response.code();
                restResponse.listBody = body.getResponse().getBody().getItems().getItem();

                addToCacheMap(Enum.KEY.MID_LAND.ordinal(), date, regionId, restResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();

            restResponse.code = Enum.ResponseCode.EXCEPTION_ERROR.getValue();
            restResponse.failMsg = e.getMessage();
        }
        return restResponse;
    }

    @SuppressWarnings("unchecked")
    public RestResponse<MidTempItem> getMidTempForecast(String date, String regionId) {
        if(containsInCache(Enum.KEY.MID_TEMP.ordinal(), date, regionId)) {
            return (RestResponse<MidTempItem>) map.get(createCacheKey(Enum.KEY.MID_TEMP.ordinal(), date, regionId));
        }

        MidWeatherInfoService shortWeatherInfoService = APIClient.getClient(APIClient.CLIENT_DEST.MID_WEATHER).create(MidWeatherInfoService.class);
        Map<String, Object> map = new HashMap<>();
        map.put("serviceKey", KEY_WEATHER_FORECAST);
        map.put("numOfRows", 10);
        map.put("pageNo", 1);
        map.put("dataType", "JSON");
        map.put("regId", regionId);
        map.put("tmFc", date);

        Call<ApiResponse<MidTempItem>> call = shortWeatherInfoService.getMidTa(map);
        RestResponse<MidTempItem> restResponse = new RestResponse<>();

        try {
            Response<ApiResponse<MidTempItem>> response = call.execute();

            if(isRequestSuccess(restResponse, response)) {
                ApiResponse<MidTempItem> body = response.body();
                if(body == null) {
                    throw new Exception();
                }
                restResponse.code = response.code();
                restResponse.listBody = body.getResponse().getBody().getItems().getItem();

                addToCacheMap(Enum.KEY.MID_TEMP.ordinal(), date, regionId, restResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();

            restResponse.code = Enum.ResponseCode.EXCEPTION_ERROR.getValue();
            restResponse.failMsg = e.getMessage();
        }
        return restResponse;
    }
}
