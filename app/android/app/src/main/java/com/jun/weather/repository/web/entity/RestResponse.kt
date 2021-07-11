package com.jun.weather.repository.web.entity

class RestResponse<T> {
    var code: Int = 0
    var failMsg: String? = null
    var listBody: List<T>? = null
    var singleBody: T? = null
}