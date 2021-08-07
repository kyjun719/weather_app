package com.jun.weather.ui.enums

enum class SkyState(val value: Int, val stringVal: String) {
    SUNNY(1,"맑음"),CLOUDY(3, "구름 많음"),DARK(4, "흐림");

    companion object{
        fun getStringValByVal(value: Int): String {
            for (e1 in values()) {
                if (e1.value == value) {
                    return e1.stringVal
                }
            }
            return ""
        }

        fun getValByStringVal(value: String?): Int {
            for (e1 in values()) {
                if (e1.stringVal == value) {
                    return e1.value
                }
            }
            return -1
        }
    }
}