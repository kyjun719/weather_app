package com.jun.weather.repository.web.entity

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