package com.jun.weather.repository.web.entity

data class ForecastItem(
        val baseDate: String,
        val baseTime: String,
        val category: String,
        val nx: Int,
        val ny: Int,
        val fcstDate: String,
        val fcstTime: String,
        val fcstValue: String
)