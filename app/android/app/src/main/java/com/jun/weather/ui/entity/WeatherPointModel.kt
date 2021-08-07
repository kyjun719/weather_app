package com.jun.weather.ui.entity

import java.lang.StringBuilder
import java.util.*

data class WeatherPointModel(
        var indexMap: HashMap<String, Array<Int>> = HashMap<String, Array<Int>>(),
        var weatherPointList: MutableList<WeatherPoint> = mutableListOf(),
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
) {
    override fun toString(): String {
        val sb = StringBuilder(deg_1)
        if(deg_2.isNotEmpty()) {
            sb.append(" $deg_2")
        }
        if(deg_3.isNotEmpty()) {
            sb.append(" $deg_3")
        }
        return sb.toString()
    }
}