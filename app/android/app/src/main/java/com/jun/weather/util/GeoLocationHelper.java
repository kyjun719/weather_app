package com.jun.weather.util;

import android.app.Activity;
import android.content.Context;
import android.location.Location;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.jun.weather.BaseApplication;
import com.jun.weather.repository.AppRepository;
import com.jun.weather.repository.web.entity.Enum;
import com.jun.weather.viewmodel.CustomViewModelProvider;
import com.jun.weather.viewmodel.NowLocationViewModel;
import com.jun.weather.viewmodel.WeatherPointViewModel;
import com.jun.weather.viewmodel.entity.FailRestResponse;
import com.jun.weather.viewmodel.entity.NowLocationModel;
import com.jun.weather.viewmodel.entity.WeatherPointModel;

import java.util.Arrays;
import java.util.HashMap;

public class GeoLocationHelper {
    public interface GeoLocationResultListener {
        void onSuccess(WeatherPointModel.WeatherPoint locationPoint);
        void onFail(FailRestResponse failRestResponse);
    }

    private GeoLocationResultListener geoLocationResultListener;
    private static GeoLocationHelper instance;
    private NowLocationViewModel nowLocationViewModel;
    private WeatherPointViewModel weatherPointViewModel;
    private boolean isInitialized = false;

    public static GeoLocationHelper getInstance() {
        if(instance == null) {
            instance = new GeoLocationHelper();
        }
        return instance;
    }

    private GeoLocationHelper() {}

    private GPSHelper.LocationResultListener locationResultListener = new GPSHelper.LocationResultListener() {
        @Override
        public void onSuccess(Location location) {
            requestGeocodeLocation(location);
        }

        @Override
        public void onCanceled(Enum.ResponseCode code, String msg) {
            FailRestResponse failRestResponse = new FailRestResponse();
            failRestResponse.code = code.getValue();
            failRestResponse.failMsg = msg;
            geoLocationResultListener.onFail(failRestResponse);
        }

        @Override
        public void onFail(Enum.ResponseCode code, String msg) {
            FailRestResponse failRestResponse = new FailRestResponse();
            failRestResponse.code = code.getValue();
            failRestResponse.failMsg = msg;
            geoLocationResultListener.onFail(failRestResponse);
        }
    };

    public void init(Context context, LifecycleOwner lifecycleOwner) {
        if(isInitialized) {
            return;
        }
        isInitialized = true;

        BaseApplication application = (BaseApplication)((Activity)context).getApplication();
        AppRepository appRepository = application.getRepository();

        nowLocationViewModel = new CustomViewModelProvider(
                ((ComponentActivity)context).getViewModelStore()).getNowLocationViewModel(application, appRepository);
        weatherPointViewModel = new CustomViewModelProvider(
                ((ComponentActivity)context).getViewModelStore()).getWeatherPointViewModel(application, appRepository);

        LiveData<WeatherPointModel> weatherPointModelList = weatherPointViewModel.getData();
        LiveData<NowLocationModel> nowLocationModelLiveData = nowLocationViewModel.getData();

        nowLocationViewModel.getData().observe(lifecycleOwner, data -> parseData(data, weatherPointModelList.getValue()));
        nowLocationViewModel.getUpdateFailData().observe(lifecycleOwner, failRestResponse -> geoLocationResultListener.onFail(failRestResponse));

        weatherPointViewModel.getData().observe(lifecycleOwner, data -> {
            parseData(nowLocationModelLiveData.getValue(), data);
            if(initPointListener != null) {
                notifyInitPoint(data);
            }
        });
        weatherPointViewModel.getUpdateFailData().observe(lifecycleOwner, failRestResponse -> geoLocationResultListener.onFail(failRestResponse));
        weatherPointViewModel.updatePointData(context);
    }

    private void parseData(NowLocationModel nowLocationModel, WeatherPointModel weatherPointModel) {
        if(nowLocationModel == null || weatherPointModel == null) {
            return;
        }

        WeatherPointModel.WeatherPoint weatherPoint = getPoint(nowLocationModel, weatherPointModel);

        if(weatherPoint == null) {
            FailRestResponse failRestResponse = new FailRestResponse();
            failRestResponse.code = Enum.ResponseCode.EXCEPTION_ERROR.getValue();
            failRestResponse.failMsg = "fail to find weather point";
            geoLocationResultListener.onFail(failRestResponse);
        } else {
            geoLocationResultListener.onSuccess(weatherPoint);
            GPSHelper.getInstance().stopGPSLocationUpdate();
        }
    }

    @Nullable
    private WeatherPointModel.WeatherPoint getPoint(NowLocationModel nowLocationModel, WeatherPointModel weatherPointModel) {
        WeatherPointModel.WeatherPoint weatherPoint = null;
        HashMap<String, Integer[]> indexMap = weatherPointModel.indexMap;
        for(NowLocationModel.NowLocation nowLocation : nowLocationModel.addressList) {
            String deg1 = nowLocation.deg1;
            String deg2 = nowLocation.deg2;

            Integer[] rangeIdx = {0, weatherPointModel.weatherPointList.size()};
            if(indexMap.get(deg1+" "+deg2) != null) {
                rangeIdx = indexMap.get(deg1+" "+deg2);
            } else if(indexMap.get(deg1+" ") != null) {
                rangeIdx = indexMap.get(deg1+" ");
            }
            CLogger.d(nowLocation+">>"+ Arrays.toString(rangeIdx));

            assert rangeIdx != null;
            for(int i = rangeIdx[0]; i <= rangeIdx[1]; i++) {
                WeatherPointModel.WeatherPoint weatherPoint1 = weatherPointModel.weatherPointList.get(i);
                if(weatherPoint1.deg_1.equals(nowLocation.deg1) &&
                        weatherPoint1.deg_2.equals(nowLocation.deg2) &&
                        weatherPoint1.deg_3.equals(nowLocation.deg3)) {
                    weatherPoint = weatherPoint1;
                    break;
                }
            }
        }

        return weatherPoint;
    }

    public void getNowLocation(Context context, GeoLocationResultListener geoLocationListener) {
        this.geoLocationResultListener = geoLocationListener;
        GPSHelper.getInstance().init(locationResultListener);
        GPSHelper.getInstance().getNowLocation(context);
    }

    private void requestGeocodeLocation(Location location) {
        double lng = location.getLongitude();
        double lat = location.getLatitude();
        CLogger.d("location::"+lng+","+lat);

        nowLocationViewModel.updateNowLocation(lng, lat);
    }

    public interface InitPointListener {
        void onFinish(@Nullable WeatherPointModel.WeatherPoint point);
    }
    private InitPointListener initPointListener;
    public void registerInitPointListener(InitPointListener listener) {
        initPointListener = listener;
        CLogger.d("weatherPointViewModel is "+(weatherPointViewModel.getData().getValue()==null?"null":"not null"));
        WeatherPointModel weatherPointModel = weatherPointViewModel.getData().getValue();
        if(weatherPointModel == null) {
            return;
        }

        notifyInitPoint(weatherPointModel);
    }

    private void notifyInitPoint(WeatherPointModel weatherPointModel) {
        NowLocationModel locationModel = new NowLocationModel();
        NowLocationModel.NowLocation nowLocation = new NowLocationModel.NowLocation();
        nowLocation.x = 126.98581171546633;
        nowLocation.y = 37.56004798513031;
        nowLocation.code = "1114055000";
        nowLocation.deg1 = "서울특별시";
        nowLocation.deg2 = "중구";
        nowLocation.deg3 = "명동";
        nowLocation.address = "서울특별시 중구 명동";
        locationModel.addressList.add(nowLocation);

        initPointListener.onFinish(getPoint(locationModel, weatherPointModel));
    }
}
