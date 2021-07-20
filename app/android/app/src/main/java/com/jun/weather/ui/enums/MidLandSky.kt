package com.jun.weather.ui.enums

enum class MidLandSky(val stringVal: String) {
    SUNNY("맑음"),
    CLOUDY("구름많음"), CLOUDY_RAIN("구름많고 비"), CLOUDY_SNOW("구름많고 눈"), CLOUDY_RAIN_SNOW("구름많고 비/눈"),
    CLOUDY_SNOW_RAIN("구름많고 눈/비"),
    DARK("흐림"), DARK_RAIN("흐리고 비"), DARK_SNOW("흐리고 눈"), DARK_RAIN_SNOW("흐리고 비/눈"),
    DARK_SNOW_RAIN("흐리고 눈/비");

    companion object{
        fun getEnumValByStringVal(stringVal: String): MidLandSky? {
            for (e1 in values()) {
                if (e1.stringVal == stringVal) {
                    return e1
                }
            }
            return null
        }
    }
}