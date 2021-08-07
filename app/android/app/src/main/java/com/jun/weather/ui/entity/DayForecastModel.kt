package com.jun.weather.ui.entity

import org.joda.time.DateTime
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

    val dateTime: DateTime
        get() = DateTimeFormat.forPattern("yyyyMMdd HHmm").parseDateTime("$forecastDate $forecastTime")
    val hourString: String
        get() = dateTime.toString("HH시")
    val dayString: String
        get() = dateTime.toString("dd일")
}