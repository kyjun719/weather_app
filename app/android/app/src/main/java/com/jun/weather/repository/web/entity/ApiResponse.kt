package com.jun.weather.repository.web.entity

data class ApiResponse<T>(
        val response: Response<T>
)

data class Response<T>(
        val header: ResHeader,
        val body: ResBody<T>
)