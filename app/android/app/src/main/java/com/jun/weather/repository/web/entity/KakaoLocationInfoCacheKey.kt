package com.jun.weather.repository.web.entity

class KakaoLocationInfoCacheKey {
    var urlPath: String? = null
    var x = 0.0
    var y = 0.0

    override fun hashCode(): Int {
        var ret = 17
        ret = ret * 31 + urlPath.hashCode()
        ret = ret * 31 + java.lang.Double.valueOf(x).hashCode()
        ret = ret * 31 + java.lang.Double.valueOf(y).hashCode()
        return ret
    }

    override fun equals(other: Any?): Boolean {
        if (other is KakaoLocationInfoCacheKey) {
            val key = other
            return key.urlPath == urlPath && key.x == x && key.y == y
        }
        return false
    }
}