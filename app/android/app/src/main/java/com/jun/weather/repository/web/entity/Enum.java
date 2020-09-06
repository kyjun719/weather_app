package com.jun.weather.repository.web.entity;

import java.net.HttpURLConnection;

public class Enum {
    public enum KEY {
        NOW, ULTRA_SHORT_FORECAST, SHORT_FORECAST, MID_LAND, MID_TEMP, HIGH_TEMP, LOW_TEMP
    }

    public enum ResponseCode {
        OK(HttpURLConnection.HTTP_OK), BAD_REQUEST(HttpURLConnection.HTTP_BAD_REQUEST),
        PERMISSION_NOT_GRANTED(9997), SETTING_NOT_GRANTED(9998),EXCEPTION_ERROR(9999);

        private int value;
        ResponseCode(int value) {
            this.value = value;
        }
        public int getValue() {
            return this.value;
        }
    }
}
