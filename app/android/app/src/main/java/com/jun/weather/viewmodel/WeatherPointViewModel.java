package com.jun.weather.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.jun.weather.repository.AppRepository;
import com.jun.weather.repository.db.entity.ShortWeatherPoint;
import com.jun.weather.util.CLogger;
import com.jun.weather.viewmodel.entity.WeatherPointModel;

import java.util.List;

public class WeatherPointViewModel extends CustomViewModel<WeatherPointModel> {
    public WeatherPointViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);

        LiveData<List<ShortWeatherPoint>> shortWeatherPointList = repository.getShortWeatherPointList();
        shortWeatherPointList.observeForever((value) -> {
            WeatherPointModel weatherPointModel = new WeatherPointModel();
            String befKey = "";
            int startIdx = -1, endIdx = -1;
            for(int i = 0; i < value.size(); i++) {
                ShortWeatherPoint point = value.get(i);
                WeatherPointModel.WeatherPoint model = new WeatherPointModel.WeatherPoint();
                model.deg_1 = point.deg_1;
                model.deg_2 = point.deg_2;
                model.deg_3 = point.deg_3;
                model.midTempCode = point.mid_temp_code;
                model.midWeatherCode = point.mid_weather_code;
                model.x = point.x;
                model.y = point.y;
                weatherPointModel.weatherPointList.add(model);

                if(befKey.equals(point.deg_1+" "+point.deg_2)) {
                    endIdx++;
                } else {
                    if(!befKey.isEmpty()) {
                        weatherPointModel.indexMap.put(befKey, new Integer[]{startIdx, endIdx});
                    }
                    startIdx = i;
                    endIdx = i;
                    befKey = point.deg_1+" "+point.deg_2;
                }
            }

            if(startIdx != -1 && startIdx == endIdx) {
                weatherPointModel.indexMap.put(befKey, new Integer[]{startIdx, value.size()-1});
            }
            appExecutor.runInUIIO(() -> data.setValue(weatherPointModel));
        });
    }

    public void updatePointData() {
        appExecutor.runInRepositoryIO(() -> repository.loadShortWeatherPointList());
    }
}
