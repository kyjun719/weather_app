package com.jun.weather.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.jun.weather.repository.AppRepository
import com.jun.weather.repository.web.entity.ForecastItem
import com.jun.weather.repository.web.entity.ObserveItem
import com.jun.weather.repository.web.entity.RestResponse
import com.jun.weather.repository.web.enums.Enums
import com.jun.weather.ui.entity.FailRestResponse
import com.jun.weather.ui.entity.NowWeatherModel
import com.jun.weather.ui.enums.PtyState
import com.jun.weather.ui.enums.WindDirection
import com.jun.weather.util.CLogger
import com.jun.weather.util.CommonUtils
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

class NowWeatherViewModel(repository: AppRepository) : BaseViewModel(repository) {
    private val _data = MutableLiveData<NowWeatherModel>()
    val data: LiveData<NowWeatherModel> = _data

    private fun updateNowWeatherData(nowWeatherData: RestResponse<ObserveItem>?,
                                     lowTempData: RestResponse<ForecastItem>?,
                                     highTempData: RestResponse<ForecastItem>?,
                                     shortForecastData: RestResponse<ForecastItem>?) {
        if (nowWeatherData == null || lowTempData == null || highTempData == null || shortForecastData == null) {
            return
        }

        if (nowWeatherData.code != Enums.ResponseCode.OK.value) {
            updateFailData.postValue(
                    FailRestResponse(nowWeatherData.code, nowWeatherData.failMsg)
            )
        } else if (lowTempData.code != Enums.ResponseCode.OK.value) {
            updateFailData.postValue(
                    FailRestResponse(lowTempData.code, lowTempData.failMsg)
            )
        } else if (highTempData.code != Enums.ResponseCode.OK.value) {
            updateFailData.postValue(
                    FailRestResponse(highTempData.code, highTempData.failMsg)
            )
        } else if (shortForecastData.code != Enums.ResponseCode.OK.value) {
            updateFailData.postValue(
                    FailRestResponse(shortForecastData.code, shortForecastData.failMsg)
            )
        } else {
            val nowWeatherModel = NowWeatherModel()
            nowWeatherModel.baseDate = nowWeatherData.listBody!![0].baseDate
            nowWeatherModel.baseTime = nowWeatherData.listBody!![0].baseTime
            nowWeatherModel.nowBaseTime = "????????????: " + nowWeatherModel.baseDate + " " + nowWeatherModel.baseTime

            /*
            T1H	??????	???	10
            RN1	1?????? ?????????	mm	8
            UUU	??????????????????	m/s	12
            VVV	??????????????????	m/s	12
            REH	??????	%	8
            PTY	????????????	?????????	4
            VEC	??????	0	10
            WSD	??????	1	10
             */for (item in nowWeatherData.listBody!!) {
                when (item.category) {
                    "T1H" -> nowWeatherModel.nowTemp = item.obsrValue.split("\\.").toTypedArray()[0] + "\u2103"
                    "RN1" -> nowWeatherModel.nowRain = item.obsrValue
                    "REH" -> nowWeatherModel.nowHumidity = "${item.obsrValue}%"
                    "PTY" -> nowWeatherModel.rainState = PtyState.getStringValByVal(item.obsrValue.toInt())
                    "VEC" -> nowWeatherModel.windDir = WindDirection.getStringValByVal(item.obsrValue.toInt())
                    "WSD" -> nowWeatherModel.wind = item.obsrValue
                }
            }
            nowWeatherModel.nowWind = nowWeatherModel.windDir + " " + nowWeatherModel.wind
            for (item in lowTempData.listBody!!) {
                if (item.category == "TMN") {
                    nowWeatherModel.lowTemp = item.fcstValue.split("\\.").toTypedArray()[0] + "\u2103"
                    break
                }
            }
            for (item in highTempData.listBody!!) {
                if (item.category == "TMX") {
                    nowWeatherModel.highTemp = item.fcstValue.split("\\.").toTypedArray()[0] + "\u2103"
                    break
                }
            }
            val dateTime = ultraShortForecastDateTime.plusMinutes(30)
            val baseDate = dateTime.toString("yyyyMMdd")
            val baseTime = dateTime.toString("HHmm")
            var sky = -1
            var pty = -1
            for ((_, _, category, _, _, fcstDate, fcstTime, fcstValue) in shortForecastData.listBody!!) {
                if (fcstDate == baseDate && fcstTime == baseTime) {
                    if (category == "PTY") {
                        pty = fcstValue.toInt()
                    }
                    if (category == "SKY") {
                        sky = fcstValue.toInt()
                    }
                }
            }
            nowWeatherModel.nowSkyDrawableId = CommonUtils.getSkyIcon(sky, pty)
            _data.postValue(nowWeatherModel)
        }
    }

    fun updateNowWeather(nx: Int, ny: Int) {
        var dateTime = DateTime.now().toDateTime(DateTimeZone.forID("Asia/Seoul"))
        if (dateTime.minuteOfHour < 40) {
            dateTime = dateTime.minusHours(1)
        }
        dateTime = dateTime.minusMinutes(dateTime.minuteOfHour)
        val baseDate = dateTime.toString("yyyyMMdd")
        val baseTime = dateTime.toString("HHmm")
        CLogger.d("$baseDate,$baseTime")

        viewModelScope.launch {
            updateNowWeatherData(
                    repository.getNowWeather(baseDate, baseTime, nx, ny),
                    repository.getLowTempData(lowTempDateTime.toString("yyyyMMdd"), lowTempDateTime.toString("HHmm"), nx, ny),
                    repository.getHighTempData(highTempDateTime.toString("yyyyMMdd"), highTempDateTime.toString("HHmm"), nx, ny),
                    repository.getUltraShortForecastData(ultraShortForecastDateTime.toString("yyyyMMdd"), ultraShortForecastDateTime.toString("HHmm"), nx, ny)
            )
        }
    }

    //??????????????? ?????? 02???, ???????????? ??????23???
    private val lowTempDateTime: DateTime
        get() {
            //??????????????? ?????? 02???, ???????????? ??????23???
            var dateTime = DateTime.now().toDateTime(DateTimeZone.forID("Asia/Seoul"))
            val nowHour = dateTime.hourOfDay
            val nowMinute = dateTime.minuteOfHour
            dateTime = if (nowHour < 2 || nowHour == 2 && nowMinute < 10) {
                dateTime.minusHours(nowHour + 1)
            } else {
                dateTime.minusHours(nowHour - 2)
            }
            dateTime = dateTime.minusMinutes(dateTime.minuteOfHour)
            return dateTime
        }

    //??????????????? ?????? 11???, ???????????? 3???????????? ??????
    private val highTempDateTime: DateTime
        get() {
            //??????????????? ?????? 11???, ???????????? 3???????????? ??????
            var dateTime = DateTime.now().toDateTime(DateTimeZone.forID("Asia/Seoul"))
            val nowHour = dateTime.hourOfDay
            val nowMinute = dateTime.minuteOfHour
            if (nowHour > 11 || nowHour == 11 && nowMinute > 10) {
                dateTime = dateTime.minusHours(nowHour - 11)
            } else {
                var i = 8
                while (i >= -1) {
                    if (nowHour > i) {
                        dateTime = dateTime.minusHours(nowHour - i)
                        CLogger.d("selected::$dateTime")
                        break
                    }
                    i -= 3
                }
            }
            CLogger.d("high::$dateTime")
            dateTime = dateTime.minusMinutes(dateTime.minuteOfHour)
            return dateTime
        }

    private val ultraShortForecastDateTime: DateTime
        get() {
            var dateTime = DateTime.now().toDateTime(DateTimeZone.forID("Asia/Seoul"))
            if (dateTime.minuteOfHour < 45) {
                dateTime = dateTime.minusHours(1)
            }
            return dateTime.minusMinutes(dateTime.minuteOfHour - 30)
        }
}