package com.jun.weather.repository.web.entity

data class ObserveItem(
        val baseDate: String,
        val baseTime: String,
        val category: String,
        val nx: Int,
        val ny: Int,
        val obsrValue: String
)
