package com.jun.weather.repository.web.entity

class WeatherInfoCacheKey {
    var key = 0
    var nx = 0
    var ny = 0
    var baseDate: String? = null
    var baseTime: String? = null
    var midDate: String? = null
    var regionId: String? = null

    override fun hashCode(): Int {
        var ret = 17
        ret = ret * 31 + key
        ret = ret * 31 + nx
        ret = ret * 31 + ny
        ret = ret * 31 + baseDate.hashCode()
        ret = ret * 31 + baseTime.hashCode()
        ret = ret * 31 + midDate.hashCode()
        ret = ret * 31 + regionId.hashCode()
        return ret
    }

    override fun equals(other: Any?): Boolean {
        if (other is WeatherInfoCacheKey) {
            val o = other
            return o.baseTime == baseTime &&
                    o.baseDate == baseDate &&
                    o.nx == nx && o.ny == ny &&
                    o.key == key &&
                    o.midDate == midDate &&
                    o.regionId == regionId
        }
        return false
    }
}