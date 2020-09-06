package com.jun.weather.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jun.weather.AppExecutor;
import com.jun.weather.BaseApplication;
import com.jun.weather.repository.AppRepository;
import com.jun.weather.viewmodel.entity.FailRestResponse;
import com.jun.weather.viewmodel.entity.NowWeatherModel;

class CustomViewModel<T> extends AndroidViewModel {
    protected MutableLiveData<FailRestResponse> updateFailData;
    protected MutableLiveData<T> data;
    protected AppRepository repository;
    protected AppExecutor appExecutor;

    public CustomViewModel(@NonNull Application application, AppRepository repository) {
        super(application);
        this.repository = repository;
        appExecutor = ((BaseApplication)application).getAppExecutor();
    }

    public LiveData<T> getData() {
        if(data == null) {
            data = new MutableLiveData<>();
        }
        return data;
    }

    public LiveData<FailRestResponse> getUpdateFailData() {
        if(updateFailData == null) {
            updateFailData = new MutableLiveData<>();
        }
        return updateFailData;
    }
}
