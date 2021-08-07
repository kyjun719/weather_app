package com.jun.weather.ui.enums

enum class PtyState(val value: Int, val stringVal: String) {
    NONE(0,"없음"),RAINY(1, "비"),
    MIXED_SNOW(2, "비 또는 눈"),SNOW(3, "눈"),SHOWER(4,"소나기");

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