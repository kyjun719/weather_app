package com.jun.weather.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.jun.weather.repository.AppRepository
import com.jun.weather.repository.web.entity.ForecastItem
import com.jun.weather.repository.web.entity.RestResponse
import com.jun.weather.repository.web.enums.Enums
import com.jun.weather.ui.entity.DayForecastModel
import com.jun.weather.ui.entity.FailRestResponse
import com.jun.weather.ui.enums.*
import com.jun.weather.util.CLogger
import com.jun.weather.util.CommonUtils
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
        val dayForecastModel = DayForecastModel()

        if(!items.isEmpty()) {
            dayForecastModel.forecastDate = items[0].fcstDate
            dayForecastModel.forecastTime = items[0].fcstTime
        }
        items.forEach {
            when (it.category) {
                VilageFcstField.POP.name -> dayForecastModel.forecastRainPercentage = it.fcstValue
                VilageFcstField.PTY.name, UltraSrtFcstField.PTY.name -> dayForecastModel.forecastRainState = PtyState.getStringValByVal(it.fcstValue.toInt())
                //VilageFcstField.PCP.name -> dayForecastModel.forecastRain = getForecastRainString(it.fcstValue)
                VilageFcstField.PCP.name -> dayForecastModel.forecastRain = it.fcstValue
                VilageFcstField.REH.name, UltraSrtFcstField.REH.name -> dayForecastModel.forecastHumidity = it.fcstValue
                //VilageFcstField.SNO.name -> dayForecastModel.forecastSnow = getForecastSnowString(it.fcstValue)
                VilageFcstField.SNO.name -> dayForecastModel.forecastSnow = it.fcstValue
                VilageFcstField.SKY.name -> dayForecastModel.forecastSky = SkyState.getStringValByVal(it.fcstValue.toInt())
                VilageFcstField.TMN.name -> dayForecastModel.forecastLowTemp = it.fcstValue
                VilageFcstField.TMX.name -> dayForecastModel.forecastHighTemp = it.fcstValue
                VilageFcstField.WAV.name -> dayForecastModel.forecastWave = it.fcstValue
                VilageFcstField.VEC.name, UltraSrtFcstField.VEC.name -> dayForecastModel.forecastWindDir = WindDirection.getStringValByVal(it.fcstValue.toInt())
                VilageFcstField.WSD.name, UltraSrtFcstField.WSD.name -> dayForecastModel.forecastWind = it.fcstValue
                UltraSrtFcstField.RN1.name -> dayForecastModel.forecastRain = it.fcstValue
                VilageFcstField.TMP.name, UltraSrtFcstField.T1H.name -> dayForecastModel.forecastTemp = it.fcstValue
            }
        }
        return dayForecastModel
    }

    private fun setSkyIcon(dayForecastModel: DayForecastModel) {
        dayForecastModel.forecastSkyDrawableId = CommonUtils.getSkyIcon(
                SkyState.getValByStringVal(dayForecastModel.forecastSky),
                PtyState.getValByStringVal(dayForecastModel.forecastRainState)
        )
    }

    fun updateShortForecastData(nx: Int, ny: Int) {
        val baseDate = shortForecastDateTime.toString("yyyyMMdd")
        val baseTime = shortForecastDateTime.toString("HHmm")
        val baseUltraShortTime = if(shortForecastDateTime.minuteOfHour < 45) {
            shortForecastDateTime.minusHours(1)
        } else {
            shortForecastDateTime
        }.withMinuteOfHour(30).toString("HHmm")
        CLogger.d("$baseDate,$baseTime")
        viewModelScope.launch {
            updateDayForecastData(
                    repository.getShortForecastWeather(baseDate, baseTime, nx, ny),
                    repository.getUltraShortForecastData(baseDate, baseUltraShortTime, nx, ny)
            )
        }
    }

    private val shortForecastDateTime: DateTime
        get() {
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

            return dateTime.minusMinutes(dateTime.minuteOfHour)
        }
}