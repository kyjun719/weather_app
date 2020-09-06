package com.jun.weather.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.jun.weather.repository.AppRepository;

class CustomViewModelFactory<T extends CustomViewModel<?>> implements ViewModelProvider.Factory {
    private Class<T> viewModelClass;
    private Application application;
    private AppRepository appRepository;

    public CustomViewModelFactory(Class<T> viewModelClass, Application application, AppRepository appRepository) {
        this.viewModelClass = viewModelClass;
        this.application = application;
        this.appRepository = appRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if(modelClass.isAssignableFrom(viewModelClass)) {
            try {
                return modelClass.getConstructor(Application.class, AppRepository.class)
                        .newInstance(application, appRepository);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //return (T) viewModel;
        }
        throw new IllegalArgumentException();
    }
}
