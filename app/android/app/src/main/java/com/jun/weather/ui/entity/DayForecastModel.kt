package com.jun.weather.ui.entity

import org.joda.time.format.DateTimeFormat

data class DayForecastModel(var forecastDate: String = "",
                            var forecastTime: String = "",
                            var nx: String = "",
                            var ny: String = "",
                            var forecastRainPercentage: String? = null,
                            var forecastRainState: String? = null,
                            var forecastRain: String? = null,
                            var forecastHumidity: String? = null,
                            var forecastSnow: String? = null,
                            var forecastSky: String? = null,
                            var forecastTemp: String? = null,
                            var forecastWave: String? = null,
                            var forecastWindDir: String? = null,
                            var forecastWind: String? = null,
                            var forecastLowTemp: String? = null,
                            var forecastHighTemp: String? = null,
                            var forecastSkyDrawableId: Int = 0) {

    val dateTime = DateTimeFormat.forPattern("yyyyMMdd HHmm").parseDateTime("$forecastDate $forecastTime")
    val hourString = dateTime.toString("HH시")
    val dayString = dateTime.toString("dd일")
}