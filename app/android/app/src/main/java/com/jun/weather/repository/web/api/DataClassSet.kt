package com.jun.weather.repository.web.api

data class ApiResponse<T>(val response: Response<T>)

data class Response<T>(val header: ResHeader, val body: ResBody<T>)

data class ResHeader(val resultCode: String, val resultMsg: String)

data class ResBody<T>(val dataType: String, val items: ResItems<T>, val pageNo: Int, val numOfRows: Int, val totalCount: Int)

data class ResItems<T>(val item: MutableList<T>)

data class ObserveItem(val baseDate: String, val baseTime: String, val category: String, val nx: Int, val ny: Int, val obsrValue: String)

data class ForecastItem(val baseDate: String, val baseTime: String, val category: String, val nx: Int, val ny: Int, val fcstDate: String, val fcstTime: String, val fcstValue: String)

data class MidLandItem(
    val regId: String,
    val rnSt3Am: String,
    val rnSt3Pm: String,
    val rnSt4Am: String,
    val rnSt4Pm: String,
    val rnSt5Am: String,
    val rnSt5Pm: String,
    val rnSt6Am: String,
    val rnSt6Pm: String,
    val rnSt7Am: String,
    val rnSt7Pm: String,
    val rnSt8: String,
    val rnSt9: String,
    val rnSt10: String,

    val wf3Am: String,
    val wf3Pm: String,
    val wf4Am: String,
    val wf4Pm: String,
    val wf5Am: String,
    val wf5Pm: String,
    val wf6Am: String,
    val wf6Pm: String,
    val wf7Am: String,
    val wf7Pm: String,
    val wf8: String,
    val wf9: String,
    val wf10: String
)

data class MidTempItem(
    val regId: String,

    val taMin3: String,
    val taMin3Low: String,
    val taMin3High: String,
    val taMax3: String,
    val taMax3Low: String,
    val taMax3High: String,

    val taMin4: String,
    val taMin4Low: String,
    val taMin4High: String,
    val taMax4: String,
    val taMax4Low: String,
    val taMax4High: String,

    val taMin5: String,
    val taMin5Low: String,
    val taMin5High: String,
    val taMax5: String,
    val taMax5Low: String,
    val taMax5High: String,

    val taMin6: String,
    val taMin6Low: String,
    val taMin6High: String,
    val taMax6: String,
    val taMax6Low: String,
    val taMax6High: String,

    val taMin7: String,
    val taMin7Low: String,
    val taMin7High: String,
    val taMax7: String,
    val taMax7Low: String,
    val taMax7High: String,

    val taMin8: String,
    val taMin8Low: String,
    val taMin8High: String,
    val taMax8: String,
    val taMax8Low: String,
    val taMax8High: String,

    val taMin9: String,
    val taMin9Low: String,
    val taMin9High: String,
    val taMax9: String,
    val taMax9Low: String,
    val taMax9High: String,

    val taMin10: String,
    val taMin10Low: String,
    val taMin10High: String,
    val taMax10: String,
    val taMax10Low: String,
    val taMax10High: String
)

data class KakaoRegionCodeRes(val meta: Meta, val documents: MutableList<RegionItem>)

data class Meta(val total_count: Int)

data class RegionItem(
        val region_type: String,
        val code: String,
        val address_name: String,
        val region_1depth_name: String,
        val region_2depth_name: String,
        val region_3depth_name: String,
        val region_4depth_name: String,
        val x: Double,
        val y: Double
)