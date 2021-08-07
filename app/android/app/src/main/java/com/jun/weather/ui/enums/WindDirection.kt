package com.jun.weather.ui.enums

enum class WindDirection(val value: Int, val stringVal: String) {
    /*
    (풍향값+45*0.5)/45
    변환값	16방위
    0	N
    1	NE
    2	E
    3	SE
    4	S
    5	SW
    6	W
    7	NW
    8	N
     */
    N(0,"남"),NE(1, "남서"),
    E(2, "서"),SE(3, "북서"),
    S(4,"북"),SW(5,"북동"),
    W(6,"동"),NW(7,"남동");

    companion object{
        fun getStringValByVal(value: Int): String {
            //(풍향값+45*0.5)/45
            val dir = (((value + 45 * 0.5) / 45).toInt() % values().size)
            for (e1 in values()) {
                if (e1.value == dir) {
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