package com.jun.weather.ui.entity

import java.util.*

data class WeatherPointModel(
    var indexMap: HashMap<String, Array<Int>> = HashMap<String, Array<Int>>(),
    var weatherPointList: MutableList<WeatherPoint> = ArrayList(),
)

data class WeatherPoint(
    var deg_1: String = "",
    var deg_2: String = "",
    var deg_3: String = "",
    var x: Int = 0,
    var y: Int = 0,
    //temp
    var midTempCode: String? = null,

    //weather
    var midWeatherCode: String? = null,
)