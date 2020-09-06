package com.jun.weather.viewmodel;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;

import com.jun.weather.BaseApplication;
import com.jun.weather.repository.AppRepository;

public class CustomViewModelProvider {
    private ViewModelStore viewModelStore;

    public CustomViewModelProvider(ViewModelStore viewModelStore) {
        this.viewModelStore = viewModelStore;
    }

    public DayForecastViewModel getDayForecastViewModel(BaseApplication application,
                                                        AppRepository appRepository) {
        CustomViewModelFactory<DayForecastViewModel> factory = new CustomViewModelFactory<>(
                DayForecastViewModel.class, application, appRepository
        );
        return new ViewModelProvider(viewModelStore, factory).get(DayForecastViewModel.class);
    }

    public MidWeatherViewModel getMidWeatherViewModel(BaseApplication application,
                                                        AppRepository appRepository) {
        CustomViewModelFactory<MidWeatherViewModel> factory = new CustomViewModelFactory<>(
                MidWeatherViewModel.class, application, appRepository
        );
        return new ViewModelProvider(viewModelStore, factory).get(MidWeatherViewModel.class);
    }

    public NowLocationViewModel getNowLocationViewModel(BaseApplication application,
                                                        AppRepository appRepository) {
        CustomViewModelFactory<NowLocationViewModel> factory = new CustomViewModelFactory<>(
                NowLocationViewModel.class, application, appRepository
        );
        return new ViewModelProvider(viewModelStore, factory).get(NowLocationViewModel.class);
    }

    public NowWeatherViewModel getNowWeatherViewModel(BaseApplication application,
                                                        AppRepository appRepository) {
        CustomViewModelFactory<NowWeatherViewModel> factory = new CustomViewModelFactory<>(
                NowWeatherViewModel.class, application, appRepository
        );
        return new ViewModelProvider(viewModelStore, factory).get(NowWeatherViewModel.class);
    }

    public WeatherPointViewModel getWeatherPointViewModel(BaseApplication application,
                                                        AppRepository appRepository) {
        CustomViewModelFactory<WeatherPointViewModel> factory = new CustomViewModelFactory<>(
                WeatherPointViewModel.class, application, appRepository
        );
        return new ViewModelProvider(viewModelStore, factory).get(WeatherPointViewModel.class);
    }
}
