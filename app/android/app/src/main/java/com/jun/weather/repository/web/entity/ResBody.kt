package com.jun.weather.repository.web.entity

data class ResBody<T>(
        val dataType: String,
        val items: ResItems<T>,
        val pageNo: Int,
        val numOfRows: Int,
        val totalCount: Int
)