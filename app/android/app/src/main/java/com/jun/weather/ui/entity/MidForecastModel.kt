package com.jun.weather.ui.entity

data class MidForecastModel(
    var lowTemp: String = "",
    var highTemp: String = "",
    var amRainPercentage: String = "",
    var pmRainPercentage: String = "",
    var dayRainPercentage: String = "",
    var amSky: Int = 0,
    var pmSky: Int = 0,
    var daySky: Int = 0,
    var baseDate: String = ""
)