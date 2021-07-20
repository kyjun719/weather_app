package com.jun.weather.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.jun.weather.repository.AppRepository
import com.jun.weather.ui.entity.WeatherPoint
import com.jun.weather.ui.entity.WeatherPointModel
import kotlinx.coroutines.launch

class WeatherPointViewModel(repository: AppRepository) : BaseViewModel(repository) {
    private val _data = MutableLiveData<WeatherPointModel>()
    val data: LiveData<WeatherPointModel> = _data


    fun updatePointData(context: Context) {
        viewModelScope.launch {
            val value = repository.getShortWeatherPointList(context)

            val weatherPointModel = WeatherPointModel()
            var befKey = ""
            var startIdx = -1
            var endIdx = -1
            for (i in value.indices) {
                val (deg1, deg2, deg3, x, y, midTempCode, midWeatherCode) = value[i]
                val model = WeatherPoint()
                model.deg_1 = deg1
                model.deg_2 = deg2
                model.deg_3 = deg3
                model.midTempCode = midTempCode
                model.midWeatherCode = midWeatherCode
                model.x = x
                model.y = y
                weatherPointModel.weatherPointList.add(model)
                if (befKey == "$deg1 $deg2") {
                    endIdx++
                } else {
                    if (!befKey.isEmpty()) {
                        weatherPointModel.indexMap[befKey] = arrayOf(startIdx, endIdx)
                    }
                    startIdx = i
                    endIdx = i
                    befKey = "$deg1 $deg2"
                }
            }
            if (startIdx != -1 && startIdx == endIdx) {
                weatherPointModel.indexMap[befKey] = arrayOf(startIdx, value.size - 1)
            }
            _data.postValue(weatherPointModel)
        }
    }
}