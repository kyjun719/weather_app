package com.jun.weather.ui.enums

enum class LgtState(val value: Int, val stringVal: String) {
    ZERO(0,"없음"),LOW(1, "낮음"),MID(2, "보통"),HIGH(3, "높음");

    companion object{
        fun getStringValByVal(value: Int): String {
            for (e1 in values()) {
                if (e1.value == value) {
                    return e1.stringVal
                }
            }
            return ""
        }

        fun getValByStringVal(value: String): Int {
            for (e1 in values()) {
                if (e1.stringVal == value) {
                    return e1.value
                }
            }
            return -1
        }
    }
}