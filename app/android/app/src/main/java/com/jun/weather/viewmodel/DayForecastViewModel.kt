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

    private fun getForecastRainString(value: String): String {
        /*
        1mm 미만	1mm 미만
        1mm 이상 30mm 미만	정수값
        (1mm~29 mm)
        30 mm 이상 50 mm 미만	30~50mm
        50 mm 이상	50mm 이상
         */
        if(value == "강수없음") return "0mm"

        val `val` = value.toInt()
        if (`val` < 1) {
            return "1mm미만"
        }
        if (`val` < 30) {
            return "1~29mm"
        }
        if (`val` < 50) {
            return "30~50mm"
        }
        return "50mm 이상"
    }

    private fun getForecastSnowString(value: String): String {
        /*
        1 cm 미만	1 cm 미만
        1 cm 이상 5 cm 미만	소수점 한자리 값
        (1.0 cm~4.9 cm)
        5 cm 이상	5 cm 이상
         */
        if(value == "적설없음") return "없음"

        val `val` = value.toDouble()
        if (`val` < 0.1) {
            return "없음"
        }
        if (`val` < 1) {
            return "1cm미만"
        }
        if (`val` < 5) {
            return String.format("%.2f cm",`val`)
        }
        return "5cm 이상"
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
        val baseUltraShortTime = if(dateTime.minuteOfHour < 45) {
            dateTime.minusHours(1)
        } else {
            dateTime
        }.withMinuteOfHour(30).toString("HHmm")
        CLogger.d("$baseDate,$baseTime")
        viewModelScope.launch {
            updateDayForecastData(
                    repository.getShortForecastWeather(baseDate, baseTime, nx, ny),
                    repository.getUltraShortForecastData(baseDate, baseUltraShortTime, nx, ny)
            )
        }
    }
}