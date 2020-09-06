package com.jun.weather.viewmodel.entity;

import java.util.Arrays;

public class Enum {
    public enum SKY {
        SUNNY(1,"맑음"),CLOUDY(3, "구름 많음"),DARK(4, "흐림");

        private int val;
        private String stringVal;
        SKY(int val, String stringVal) {
            this.val = val;
            this.stringVal = stringVal;
        }

        public int getVal() {
            return val;
        }

        public String getStringVal() {
            return this.stringVal;
        }

        public static String getStringValByVal(int val) {
            for(SKY e1 : SKY.values()) {
                if(e1.val == val) {
                    return e1.stringVal;
                }
            }
            return "";
        }

        public static int getValByStringVal(String val) {
            for(SKY e1 : SKY.values()) {
                if(e1.stringVal.equals(val)) {
                    return e1.val;
                }
            }
            return -1;
        }
    }

    public enum PTY {
        //없음(0), 비(1), 비/눈(2), 눈(3), 소나기(4)
        NONE(0,"없음"),RAINY(1, "비"),
        MIXED_SNOW(2, "비 또는 눈"),SNOW(3, "눈"),SHOWER(4,"소나기");

        private int val;
        private String stringVal;
        PTY(int val, String stringVal) {
            this.val = val;
            this.stringVal = stringVal;
        }

        public int getVal() {
            return val;
        }

        public String getStringVal() {
            return this.stringVal;
        }

        public static String getStringValByVal(int val) {
            for(PTY e1 : PTY.values()) {
                if(e1.val == val) {
                    return e1.stringVal;
                }
            }
            return "";
        }

        public static int getValByStringVal(String val) {
            for(PTY e1 : PTY.values()) {
                if(e1.stringVal.equals(val)) {
                    return e1.val;
                }
            }
            return -1;
        }
    }

    public enum WIND_DIR {
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

        private int val;
        private String stringVal;
        WIND_DIR(int val, String stringVal) {
            this.val = val;
            this.stringVal = stringVal;
        }

        public int getVal() {
            return val;
        }

        public String getStringVal() {
            return this.stringVal;
        }

        public static String getStringValByVal(int val) {
            //(풍향값+45*0.5)/45
            val = (int)(val+45*0.5)/45;
            for(WIND_DIR e1 : WIND_DIR.values()) {
                if(e1.val == val) {
                    return e1.stringVal;
                }
            }
            return "";
        }

        public static int getValByStringVal(String stringVal) {
            for(WIND_DIR e1 : WIND_DIR.values()) {
                if(e1.stringVal.equals(stringVal)) {
                    return e1.val;
                }
            }
            return -1;
        }
    }

    public enum MID_LAND_SKY {
        SUNNY("맑음"),
        CLOUDY("구름많음"), CLOUDY_RAIN("구름많고 비"), CLOUDY_SNOW("구름많고 눈"), CLOUDY_RAIN_SNOW("구름많고 비/눈"),
        CLOUDY_SNOW_RAIN("구름많고 눈/비"),
        DARK("흐림"), DARK_RAIN("흐리고 비"), DARK_SNOW("흐리고 눈"), DARK_RAIN_SNOW("흐리고 비/눈"),
        DARK_SNOW_RAIN("흐리고 눈/비");

        private String stringVal;
        MID_LAND_SKY(String stringVal) {
            this.stringVal = stringVal;
        }

        public static int getValByStringVal(String stringVal) {
            for(MID_LAND_SKY e1 : MID_LAND_SKY.values()) {
                if(e1.stringVal.equals(stringVal)) {
                    return e1.ordinal();
                }
            }
            return -1;
        }

        public static MID_LAND_SKY getEnumValByStringVal(String stringVal) {
            for(MID_LAND_SKY e1 : MID_LAND_SKY.values()) {
                if(e1.stringVal.equals(stringVal)) {
                    return e1;
                }
            }
            return null;
        }
    }

    public enum LGT {
        //확률없음(0), 낮음(1), 보통(2), 높음(3)
        ZERO(0,"없음"),LOW(1, "낮음"),MID(2, "보통"),HIGH(3, "높음");

        private int val;
        private String stringVal;
        LGT(int val, String stringVal) {
            this.val = val;
            this.stringVal = stringVal;
        }

        public int getVal() {
            return val;
        }

        public String getStringVal() {
            return this.stringVal;
        }

        public static String getStringValByVal(int val) {
            for(LGT e1 : LGT.values()) {
                if(e1.val == val) {
                    return e1.stringVal;
                }
            }
            return null;
        }
    }
}
