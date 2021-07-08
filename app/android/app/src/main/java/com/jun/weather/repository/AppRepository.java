package com.jun.weather.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jun.weather.AppExecutor;
import com.jun.weather.repository.db.FileRepository;
import com.jun.weather.repository.db.RoomRepository;
import com.jun.weather.repository.web.KakaoLocationRepository;
import com.jun.weather.repository.web.WeatherInfoRepository;
import com.jun.weather.repository.web.api.ForecastItem;
import com.jun.weather.repository.web.api.KakaoRegionCodeRes;
import com.jun.weather.repository.web.api.MidLandItem;
import com.jun.weather.repository.web.api.MidTempItem;
import com.jun.weather.repository.web.api.ObserveItem;
import com.jun.weather.repository.web.entity.RestResponse;

import java.util.List;

public class AppRepository {
    private static AppRepository instance;
    private AppExecutor appExecutor;
    private WeatherInfoRepository weatherInfoRepository;
    private KakaoLocationRepository kakaoLocationRepository;
    private RoomRepository roomRepository;
    private FileRepository fileRepository;

    public static AppRepository getInstance(Context context, AppExecutor executor) {
        if(instance == null) {
            instance = new AppRepository(context, executor);
        }
        return instance;
    }

    private AppRepository(Context context, AppExecutor executor) {
        appExecutor = executor;
        weatherInfoRepository = WeatherInfoRepository.getInstance(context);
        fileRepository = new FileRepository();
        kakaoLocationRepository = KakaoLocationRepository.getInstance(context);
    }

    public void init(Context context) {

    }

    private MutableLiveData<RestResponse<ObserveItem>> nowWeatherData;
    public void updateNowWeather(String baseDate, String baseTime, int nx, int ny) {
        appExecutor.runInNetworkIO(() -> {
            RestResponse<ObserveItem> restResponse = weatherInfoRepository.getNowWeather(baseDate, baseTime, nx, ny);
            appExecutor.runInRepositoryIO(() -> nowWeatherData.postValue(restResponse));
        });
    }

    public LiveData<RestResponse<ObserveItem>> getNowWeatherData() {
        if(nowWeatherData == null) {
            nowWeatherData = new MutableLiveData<>();
        }
        return nowWeatherData;
    }

    private MutableLiveData<RestResponse<ForecastItem>> shortForecastData;
    public void updateShortForecastWeather(String baseDate, String baseTime, int nx, int ny) {
        appExecutor.runInNetworkIO(() -> {
            RestResponse<ForecastItem> restResponse = weatherInfoRepository.getShortForecast(baseDate, baseTime, nx, ny);
            appExecutor.runInRepositoryIO(() -> shortForecastData.postValue(restResponse));
        });
    }

    public LiveData<RestResponse<ForecastItem>> getShortForecastData() {
        if(shortForecastData == null) {
            shortForecastData = new MutableLiveData<>();
        }
        return shortForecastData;
    }

    private MutableLiveData<RestResponse<MidLandItem>> midLandData;
    public void updateMidLandData(String date, String regionId) {
        appExecutor.runInNetworkIO(() -> {
            RestResponse<MidLandItem> restResponse = weatherInfoRepository.getMidLandForecast(date, regionId);
            appExecutor.runInRepositoryIO(() -> midLandData.postValue(restResponse));
        });
    }

    public LiveData<RestResponse<MidLandItem>> getMidLandData() {
        if(midLandData == null) {
            midLandData = new MutableLiveData<>();
        }
        return midLandData;
    }

    private MutableLiveData<RestResponse<MidTempItem>> midTempData;
    public void updateMidTempData(String date, String regionId) {
        appExecutor.runInNetworkIO(() -> {
            RestResponse<MidTempItem> restResponse = weatherInfoRepository.getMidTempForecast(date, regionId);
            appExecutor.runInRepositoryIO(() -> midTempData.postValue(restResponse));
        });
    }

    public LiveData<RestResponse<MidTempItem>> getMidTempData() {
        if(midTempData == null) {
            midTempData = new MutableLiveData<>();
        }
        return midTempData;
    }

    private MutableLiveData<List<FileRepository.ShortWeatherPoint>> shortWeatherPointList;
    public void loadShortWeatherPointList(Context context) {
        appExecutor.runInRepositoryIO(() -> shortWeatherPointList.postValue(fileRepository.getShortWeatherPoint(context.getResources())));
    }
    public LiveData<List<FileRepository.ShortWeatherPoint>> getShortWeatherPointList() {
        if(shortWeatherPointList == null) {
            shortWeatherPointList = new MutableLiveData<>();
        }
        return shortWeatherPointList;
    }

    private MutableLiveData<RestResponse<ForecastItem>> lowTempData;
    public void updateLowTempData(String baseDate, String baseTime, int nx, int ny) {
        appExecutor.runInNetworkIO(() -> {
            RestResponse<ForecastItem> restResponse = weatherInfoRepository.getDayTempData(baseDate, baseTime, nx, ny, false);
            appExecutor.runInRepositoryIO(() -> lowTempData.postValue(restResponse));
        });
    }

    public LiveData<RestResponse<ForecastItem>> getLowTempData() {
        if(lowTempData == null) {
            lowTempData = new MutableLiveData<>();
        }
        return lowTempData;
    }

    private MutableLiveData<RestResponse<ForecastItem>> highTempData;
    public void updateHighTempData(String baseDate, String baseTime, int nx, int ny) {
        appExecutor.runInNetworkIO(() -> {
            RestResponse<ForecastItem> restResponse = weatherInfoRepository.getDayTempData(baseDate, baseTime, nx, ny, true);
            appExecutor.runInRepositoryIO(() -> highTempData.postValue(restResponse));
        });
    }

    public LiveData<RestResponse<ForecastItem>> getHighTempData() {
        if(highTempData == null) {
            highTempData = new MutableLiveData<>();
        }
        return highTempData;
    }

    private MutableLiveData<RestResponse<ForecastItem>> ultraShortForecastData;
    public void updateUltraShortForecastData(String baseDate, String baseTime, int nx, int ny) {
        appExecutor.runInNetworkIO(() -> {
            RestResponse<ForecastItem> restResponse = weatherInfoRepository.getUltraShortForecast(baseDate, baseTime, nx, ny);
            appExecutor.runInRepositoryIO(() -> ultraShortForecastData.postValue(restResponse));
        });
    }
    public LiveData<RestResponse<ForecastItem>> getUltraShortForecastData() {
        if(ultraShortForecastData == null) {
            ultraShortForecastData = new MutableLiveData<>();
        }
        return ultraShortForecastData;
    }

    private MutableLiveData<RestResponse<KakaoRegionCodeRes>> kakaoRegionCodeRes;
    public void updateKakaoRegionCodeRes(double lng, double lat) {
        appExecutor.runInNetworkIO(() -> {
            RestResponse<KakaoRegionCodeRes> resRestResponse = kakaoLocationRepository.getLocationStringAddress(lng, lat);
            appExecutor.runInRepositoryIO(() -> kakaoRegionCodeRes.postValue(resRestResponse));
        });
    }
    public LiveData<RestResponse<KakaoRegionCodeRes>> getKakaoRegionCodeRes() {
        if(kakaoRegionCodeRes == null) {
            kakaoRegionCodeRes = new MutableLiveData<>();
        }
        return kakaoRegionCodeRes;
    }
}
