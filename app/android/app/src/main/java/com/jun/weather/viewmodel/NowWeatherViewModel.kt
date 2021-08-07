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
import com.jun.weather.ui.enums.*
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
        CLogger.d("${nowWeatherData == null} ${lowTempData == null} ${highTempData == null} ${shortForecastData == null}")
        if (nowWeatherData == null || lowTempData == null || highTempData == null || shortForecastData == null) {
            return
        }
        isLoading.postValue(false)

        CLogger.d("${nowWeatherData.code} ${lowTempData.code} ${highTempData.code} ${shortForecastData.code}")

        if (nowWeatherData.code != Enums.ResponseCode.OK.value) {
            updateFailData.postValue(
                    FailRestResponse(nowWeatherData.code, nowWeatherData.failMsg)
            )
            return
        }
        if (lowTempData.code != Enums.ResponseCode.OK.value) {
            updateFailData.postValue(
                    FailRestResponse(lowTempData.code, lowTempData.failMsg)
            )
            return
        }
        if (highTempData.code != Enums.ResponseCode.OK.value) {
            updateFailData.postValue(
                    FailRestResponse(highTempData.code, highTempData.failMsg)
            )
            return
        }
        if (shortForecastData.code != Enums.ResponseCode.OK.value) {
            updateFailData.postValue(
                    FailRestResponse(shortForecastData.code, shortForecastData.failMsg)
            )
            return
        }

        val nowWeatherModel = NowWeatherModel()
        nowWeatherModel.baseDate = nowWeatherData.listBody!![0].baseDate
        nowWeatherModel.baseTime = nowWeatherData.listBody!![0].baseTime
        nowWeatherModel.nowBaseTime = "기준시간: " + nowWeatherModel.baseDate + " " + nowWeatherModel.baseTime

        for (item in nowWeatherData.listBody!!) {
            when (item.category) {
                UltraSrtNcstField.T1H.name -> nowWeatherModel.nowTemp = item.obsrValue.toDouble().toInt().toString() + "\u2103"
                UltraSrtNcstField.RN1.name -> nowWeatherModel.nowRain = item.obsrValue
                UltraSrtNcstField.REH.name -> nowWeatherModel.nowHumidity = "${item.obsrValue}%"
                UltraSrtNcstField.PTY.name -> nowWeatherModel.rainState = PtyState.getStringValByVal(item.obsrValue.toInt())
                UltraSrtNcstField.VEC.name -> nowWeatherModel.windDir = WindDirection.getStringValByVal(item.obsrValue.toInt())
                UltraSrtNcstField.WSD.name -> nowWeatherModel.wind = item.obsrValue
            }
        }
        nowWeatherModel.nowWind = nowWeatherModel.windDir + " " + nowWeatherModel.wind
        for (item in lowTempData.listBody!!) {
            if (item.category == VilageFcstField.TMN.name) {
                nowWeatherModel.lowTemp = item.fcstValue.split("\\.").toTypedArray()[0] + "\u2103"
                break
            }
        }
        for (item in highTempData.listBody!!) {
            if (item.category == VilageFcstField.TMX.name) {
                nowWeatherModel.highTemp = item.fcstValue.split("\\.").toTypedArray()[0] + "\u2103"
                break
            }
        }
        val dateTime = ultraShortForecastDateTime.plusMinutes(30)
        val baseDate = dateTime.toString("yyyyMMdd")
        val baseTime = dateTime.toString("HHmm")
        var sky = -1
        var pty = -1
        for (item in shortForecastData.listBody!!) {
            if (item.fcstDate == baseDate && item.fcstTime == baseTime) {
                if (item.category == UltraSrtFcstField.PTY.name) {
                    pty = item.fcstValue.toInt()
                }
                if (item.category == UltraSrtFcstField.SKY.name) {
                    sky = item.fcstValue.toInt()
                }
            }
        }
        nowWeatherModel.nowSkyDrawableId = CommonUtils.getSkyIcon(sky, pty)
        _data.postValue(nowWeatherModel)
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

        isLoading.postValue(true)

        viewModelScope.launch {
            updateNowWeatherData(
                    repository.getNowWeather(baseDate, baseTime, nx, ny),
                    repository.getLowTempData(lowTempDateTime.toString("yyyyMMdd"), lowTempDateTime.toString("HHmm"), nx, ny),
                    repository.getHighTempData(highTempDateTime.toString("yyyyMMdd"), highTempDateTime.toString("HHmm"), nx, ny),
                    repository.getUltraShortForecastData(ultraShortForecastDateTime.toString("yyyyMMdd"), ultraShortForecastDateTime.toString("HHmm"), nx, ny)
            )
        }
    }

    //최저기온은 금일 02시, 아닐경우 전일23시
    private val lowTempDateTime: DateTime
        get() {
            //최저기온은 금일 02시, 아닐경우 전일23시
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

    //최고기온은 금일 11시, 아닐경우 3시간전씩 돌림
    private val highTempDateTime: DateTime
        get() {
            //최고기온은 금일 11시, 아닐경우 3시간전씩 돌림
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
            return dateTime.withMinuteOfHour(30)
        }
}