package com.jun.weather.ui.entity

import com.google.gson.Gson
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/*
POP	강수확률	%	8
PTY	강수형태	코드값	4
R06	6시간 강수량	범주 (1 mm)	8
REH	습도	%	8
S06	6시간 신적설	범주(1 cm)	8
SKY	하늘상태	코드값	4
T3H	3시간 기온	℃	10
WAV	파고	M	8
VEC	풍향	m/s	10
WSD	풍속	1	10
 */
data class DayForecastModel(var forecastDate: String = "",
                            var forecastTime: String = "",
                            var nx: String = "",
                            var ny: String = "",
                            var forecastRainPercentage: String = "",
                            var forecastRainState: String = "",
                            var forecastRain: String = "",
                            var forecastHumidity: String = "",
                            var forecastSnow: String = "",
                            var forecastSky: String = "",
                            var forecastTemp: String = "",
                            var forecastWave: String = "",
                            var forecastWindDir: String = "",
                            var forecastWind: String = "",
                            var forecastLowTemp: String = "",
                            var forecastHighTemp: String = "",
                            var forecastSkyDrawableId: Int = 0) {

    val dateTime: DateTime
        get() = DateTimeFormat.forPattern("yyyyMMdd HHmm").parseDateTime("$forecastDate $forecastTime")
    val hourString: String
        get() = dateTime.toString("HH시")
    val dayString: String
        get() = dateTime.toString("dd일")
}