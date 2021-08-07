package com.jun.weather.ui.entity

import com.google.gson.Gson

/*
T1H	기온	℃	10
RN1	1시간 강수량	mm	8
UUU	동서바람성분	m/s	12
VVV	남북바람성분	m/s	12
REH	습도	%	8
PTY	강수형태	코드값	4
VEC	풍향	0	10
WSD	풍속	1	10

현재 하늘상태는 초단기예보로 가져와야 하나?
 */
data class NowWeatherModel(
    var baseDate: String = "",
    var baseTime: String = "",
    var nx: String = "",
    var ny: String = "",
    var nowTemp: String = "",
    var nowRain: String = "",
    var nowHumidity: String = "",
    var rainState: String = "",
    var windDir: String = "",
    var wind: String = "",
    var nowWind: String = "",
    var nowBaseTime: String = "",
    var lowTemp: String = "",
    var highTemp: String = "",
    var nowSkyDrawableId: Int? = null,
)