package com.jun.weather.ui.enums

import com.google.gson.annotations.SerializedName

enum class VilageFcstField {
    POP, //강수확률	%	8
    PTY, //강수형태	코드값	4
    PCP, //1시간 강수량	범주 (1 mm)	8
    REH, //습도	%	8
    SNO, //1시간 신적설	범주(1 cm)	8
    SKY, //하늘상태	코드값	4
    TMP, //1시간 기온	℃	10
    TMN, //일 최저기온	℃	10
    TMX, //일 최고기온	℃	10
    UUU, //풍속(동서성분)	m/s	12
    VVV, //풍속(남북성분)	m/s	12
    WAV, //파고	M	8
    VEC, //풍향	deg	10
    WSD, //풍속	m/s	10
}