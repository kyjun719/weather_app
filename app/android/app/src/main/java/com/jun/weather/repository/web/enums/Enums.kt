package com.jun.weather.repository.web.enums

import java.net.HttpURLConnection

class Enums {
    enum class KEY {
        NOW,
        ULTRA_SHORT_FORECAST,
        SHORT_FORECAST,
        MID_LAND,
        MID_TEMP,
        HIGH_TEMP,
        LOW_TEMP
    }

    enum class ResponseCode(val value: Int) {
        OK(HttpURLConnection.HTTP_OK),
        BAD_REQUEST(HttpURLConnection.HTTP_BAD_REQUEST),
        PERMISSION_NOT_GRANTED(9997),
        SETTING_NOT_GRANTED(9998),
        EXCEPTION_ERROR(9999);
    }

    enum class CLIENT_DEST {
        SHORT_WEATHER,
        MID_WEATHER,
        KAKAO_LOCAL
    }
}