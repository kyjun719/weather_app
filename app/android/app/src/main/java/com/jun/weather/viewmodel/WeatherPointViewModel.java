package com.jun.weather.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.jun.weather.repository.AppRepository;
import com.jun.weather.repository.db.FileRepository;
import com.jun.weather.util.CLogger;
import com.jun.weather.viewmodel.entity.WeatherPointModel;

import java.util.List;

public class WeatherPointViewModel extends CustomViewModel<WeatherPointModel> {
    public WeatherPointViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);

        LiveData<List<FileRepository.ShortWeatherPoint>> shortWeatherPointList = repository.getShortWeatherPointList();
        shortWeatherPointList.observeForever((value) -> {
            WeatherPointModel weatherPointModel = new WeatherPointModel();
            String befKey = "";
            int startIdx = -1, endIdx = -1;
            for(int i = 0; i < value.size(); i++) {
                FileRepository.ShortWeatherPoint point = value.get(i);
                WeatherPointModel.WeatherPoint model = new WeatherPointModel.WeatherPoint();
                model.deg_1 = point.getDeg1();
                model.deg_2 = point.getDeg2();
                model.deg_3 = point.getDeg3();
                model.midTempCode = point.getMidTempCode();
                model.midWeatherCode = point.getMidWeatherCode();
                model.x = point.getX();
                model.y = point.getY();
                weatherPointModel.weatherPointList.add(model);

                if(befKey.equals(point.getDeg1()+" "+point.getDeg2())) {
                    endIdx++;
                } else {
                    if(!befKey.isEmpty()) {
                        weatherPointModel.indexMap.put(befKey, new Integer[]{startIdx, endIdx});
                    }
                    startIdx = i;
                    endIdx = i;
                    befKey = point.getDeg1()+" "+point.getDeg2();
                }
            }

            if(startIdx != -1 && startIdx == endIdx) {
                weatherPointModel.indexMap.put(befKey, new Integer[]{startIdx, value.size()-1});
            }
            appExecutor.runInUIIO(() -> data.setValue(weatherPointModel));
        });
    }

    public void updatePointData(Context context) {
        appExecutor.runInRepositoryIO(() -> repository.loadShortWeatherPointList(context));
    }
}
