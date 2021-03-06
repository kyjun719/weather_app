package com.jun.weather.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.jun.weather.repository.AppRepository
import com.jun.weather.repository.web.entity.ForecastItem
import com.jun.weather.repository.web.entity.RestResponse
import com.jun.weather.repository.web.enums.Enums
import com.jun.weather.util.CLogger
import com.jun.weather.util.CommonUtils
import com.jun.weather.ui.entity.DayForecastModel
import com.jun.weather.ui.entity.FailRestResponse
import com.jun.weather.ui.enums.PtyState
import com.jun.weather.ui.enums.SkyState
import com.jun.weather.ui.enums.WindDirection
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.*

class DayForecastViewModel(repository: AppRepository) : BaseViewModel(repository) {
    private val _data = MutableLiveData<List<DayForecastModel>>()
    val data: LiveData<List<DayForecastModel>> = _data

    private fun updateDayForecastData(
            shortForecastData: RestResponse<ForecastItem>?,
            ultraShortForecastData: RestResponse<ForecastItem>?
    ) {
        if (shortForecastData == null || ultraShortForecastData == null) {
            return
        }

        if (shortForecastData.code != Enums.ResponseCode.OK.value) {
            updateFailData.postValue(
                    FailRestResponse(shortForecastData.code,
                            shortForecastData.failMsg))
        } else if (ultraShortForecastData.code != Enums.ResponseCode.OK.value) {
            updateFailData.postValue(
                    FailRestResponse(ultraShortForecastData.code,
                            ultraShortForecastData.failMsg))
        } else {
            val itemList: MutableList<ForecastItem> = ArrayList(shortForecastData.listBody)
            itemList.addAll(ultraShortForecastData.listBody!!)
            val list = mutableListOf<DayForecastModel>()
            itemList.groupBy { it.fcstDate + it.fcstTime }.forEach{
                val item = parseData(it.value)
                setSkyIcon(item)
                list.add(item)
            }

            list.sortBy { it.dateTime.millis }
            _data.postValue(list)
        }
    }

    private fun parseData(items: List<ForecastItem>) : DayForecastModel {
        /*
        ????????????
        POP	????????????	%	8
        PTY	????????????	?????????	4
        R06	6?????? ?????????	?????? (1 mm)	8
        REH	??????	%	8
        S06	6?????? ?????????	??????(1 cm)	8
        SKY	????????????	?????????	4
        T3H	3?????? ??????	???	10
        WAV	??????	M	8
        VEC	??????	m/s	10
        WSD	??????	1	10
         */

        /*
        ???????????????
        T1H	??????	???	10
        RN1	1?????? ?????????	mm	8
        SKY	????????????	?????????	4
        UUU	??????????????????	m/s	12
        VVV	??????????????????	m/s	12
        REH	??????	%	8
        PTY	????????????	?????????	4
        LGT	??????	?????????	4
        VEC	??????	0	10
        WSD	??????	1	10
         */
        val dayForecastModel = DayForecastModel()

        if(!items.isEmpty()) {
            dayForecastModel.forecastDate = items[0].fcstDate
            dayForecastModel.forecastTime = items[0].fcstTime
        }
        items.forEach {
            when (it.category) {
                "POP" -> dayForecastModel.forecastRainPercentage = it.fcstValue
                "PTY" -> dayForecastModel.forecastRainState = PtyState.getStringValByVal(it.fcstValue.toInt())
                "R06" -> dayForecastModel.forecastRain = getForecastRainString(it.fcstValue.toDouble())
                "RN1" -> dayForecastModel.forecastRain = it.fcstValue
                "REH" -> dayForecastModel.forecastHumidity = it.fcstValue
                "S06" -> dayForecastModel.forecastSnow = getForecastSnowString(it.fcstValue.toDouble())
                "SKY" -> dayForecastModel.forecastSky = SkyState.getStringValByVal(it.fcstValue.toInt())
                "T3H", "T1H" -> dayForecastModel.forecastTemp = it.fcstValue
                "TMN" -> dayForecastModel.forecastLowTemp = it.fcstValue
                "TMX" -> dayForecastModel.forecastHighTemp = it.fcstValue
                "WAV" -> dayForecastModel.forecastWave = it.fcstValue
                "VEC" -> dayForecastModel.forecastWindDir = WindDirection.getStringValByVal(it.fcstValue.toInt())
                "WSD" -> dayForecastModel.forecastWind = it.fcstValue
            }
        }
        return dayForecastModel
    }

    private fun getForecastRainString(`val`: Double): String {
        /*
        0.1mm ??????	0mm ?????? ??????	0
        0.1mm ?????? 1mm ??????	1mm ??????	1
        1 mm ?????? 5 mm ??????	1~4mm	5
        5 mm ?????? 10 mm ??????	5~9mm	10
        10 mm ?????? 20 mm ??????	10~19mm	20
        20 mm ?????? 40 mm ??????	20~39mm	40
        40 mm ?????? 70 mm ??????	40~69mm	70
        70 mm ??????	70mm ??????	100
         */
        if (`val` < 0.1) {
            return "??????"
        }
        if (`val` < 1) {
            return "1mm??????"
        }
        if (`val` < 5) {
            return "1~4mm"
        }
        if (`val` < 10) {
            return "5~9mm"
        }
        if (`val` < 20) {
            return "10~19mm"
        }
        if (`val` < 40) {
            return "20~39mm"
        }
        return if (`val` < 70) {
            "40~69mm"
        } else "70mm ??????"
    }

    private fun getForecastSnowString(`val`: Double): String {
        /*
        ??????	???????????????	GRIB ?????????
        0.1 cm ??????	0cm ?????? ??????	0
        0.1 cm ?????? 1 cm ??????	1cm ??????	1
        1 cm ?????? 5 cm ??????	1~4cm	5
        5 cm ?????? 10 cm ??????	5~9cm	10
        10 cm ?????? 20 cm ??????	10~19cm	20
        20 cm ??????	20cm ??????	100
         */
        if (`val` < 0.1) {
            return "??????"
        }
        if (`val` < 1) {
            return "1cm??????"
        }
        if (`val` < 5) {
            return "1~4cm"
        }
        if (`val` < 10) {
            return "5~9cm"
        }
        return if (`val` < 20) {
            "10~19cm"
        } else "20cm ??????"
    }

    private fun setSkyIcon(dayForecastModel: DayForecastModel) {
        val valPty = PtyState.getValByStringVal(dayForecastModel.forecastRainState)
        val valSky = SkyState.getValByStringVal(dayForecastModel.forecastSky)
        dayForecastModel.forecastSkyDrawableId = CommonUtils.getSkyIcon(valSky, valPty)
    }

    fun updateShortForecastData(nx: Int, ny: Int) {
        var dateTime = DateTime.now().toDateTime(DateTimeZone.forID("Asia/Seoul"))
        val arr = intArrayOf(-1, 2, 5, 8, 11, 14, 17, 20, 23, 26)
        val nowHour = dateTime.hourOfDay
        var isOClock = false
        for (i in 1 until arr.size - 1) {
            if (nowHour == arr[i] && dateTime.minuteOfHour > 10) {
                isOClock = true
                break
            }
        }
        if (!isOClock) {
            for (i in 1 until arr.size) {
                if (arr[i - 1] <= nowHour && arr[i] >= nowHour) {
                    CLogger.d(nowHour.toString() + ">>" + arr[i - 1] + "~" + arr[i])
                    dateTime = dateTime.minusHours(nowHour - arr[i - 1])
                    break
                }
            }
        }

        /*
        String baseDate = ""+dateTime.getYear();
        baseDate += dateTime.getMonthOfYear()>9?dateTime.getMonthOfYear():"0"+dateTime.getMonthOfYear();
        baseDate += dateTime.getDayOfMonth()>9?dateTime.getDayOfMonth():"0"+dateTime.getDayOfMonth();

        String baseTime = dateTime.getHourOfDay()>9?""+dateTime.getHourOfDay():"0"+dateTime.getHourOfDay();
        baseTime += "00";
         */
        dateTime = dateTime.minusMinutes(dateTime.minuteOfHour)
        val baseDate = dateTime.toString("yyyyMMdd")
        val baseTime = dateTime.toString("HHmm")
        CLogger.d("$baseDate,$baseTime")
        viewModelScope.launch {
            updateDayForecastData(
                    repository.getShortForecastWeather(baseDate, baseTime, nx, ny),
                    repository.getUltraShortForecastData(baseDate, baseTime, nx, ny)
            )
        }
    }
}