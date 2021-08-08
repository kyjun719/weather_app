package com.jun.weather.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.jun.weather.repository.AppRepository
import com.jun.weather.ui.entity.WeatherPoint
import com.jun.weather.ui.entity.WeatherPointModel
import com.jun.weather.util.PreferenceUtils
import kotlinx.coroutines.launch

class WeatherPointViewModel(repository: AppRepository) : BaseViewModel(repository) {
    private val _data = MutableLiveData<WeatherPointModel>()
    val data: LiveData<WeatherPointModel> = _data

    private val _selectedPoint = MutableLiveData<WeatherPoint>()
    val selectedPoint = _selectedPoint

    val pointArr = Array(3) {
        MutableLiveData<WeatherPoint>()
    }

    init {
        updatePointArr()
    }

    private fun updatePointArr() {
        PreferenceUtils.getInstance().pointList.forEachIndexed { index, weatherPoint ->
            pointArr[index].postValue(weatherPoint)
        }
    }

    fun updatePointData(context: Context) {
        viewModelScope.launch {
            val value = repository.getShortWeatherPointList(context)

            val weatherPointModel = WeatherPointModel()
            var befKey = ""
            var startIdx = -1
            var endIdx = -1
            value.forEachIndexed { index, model ->
                weatherPointModel.weatherPointList.add(
                        WeatherPoint(
                                model.deg1,
                                model.deg2,
                                model.deg3,
                                model.x,
                                model.y,
                                model.midTempCode,
                                model.midWeatherCode,
                        )
                )
                if (befKey == "${model.deg1} ${model.deg2}") {
                    endIdx++
                } else {
                    if (!befKey.isEmpty()) {
                        weatherPointModel.indexMap[befKey] = arrayOf(startIdx, endIdx)
                    }
                    startIdx = index
                    endIdx = index
                    befKey = "${model.deg1} ${model.deg2}"
                }
            }
            if (startIdx != -1 && startIdx == endIdx) {
                weatherPointModel.indexMap[befKey] = arrayOf(startIdx, value.size - 1)
            }
            _data.postValue(weatherPointModel)
        }
    }

    fun postSelectedPoint(point : WeatherPoint) {
        PreferenceUtils.getInstance().addPoint(point)
        _selectedPoint.postValue(point)
        updatePointArr()
    }
}