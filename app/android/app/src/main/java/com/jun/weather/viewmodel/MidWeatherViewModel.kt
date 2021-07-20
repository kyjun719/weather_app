package com.jun.weather.viewmodel

import android.util.SparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.jun.weather.repository.AppRepository
import com.jun.weather.repository.web.entity.MidLandItem
import com.jun.weather.repository.web.entity.MidTempItem
import com.jun.weather.repository.web.entity.RestResponse
import com.jun.weather.repository.web.enums.Enums
import com.jun.weather.ui.entity.FailRestResponse
import com.jun.weather.ui.entity.MidForecastModel
import com.jun.weather.util.CLogger
import com.jun.weather.util.CommonUtils
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

class MidWeatherViewModel(repository: AppRepository) : BaseViewModel(repository) {
    private val _data = MutableLiveData<SparseArray<MidForecastModel>>()
    val data: LiveData<SparseArray<MidForecastModel>> = _data

    private fun updateMidForecastModel(data1: RestResponse<MidLandItem>?, data2: RestResponse<MidTempItem>?) {
        if (data1 == null || data2 == null) {
            return
        }
        if (data1.code != Enums.ResponseCode.OK.value) {
            updateFailData.postValue(
                    FailRestResponse(data1.code, "land forecast fail," + data1.failMsg))
        } else if (data2.code != Enums.ResponseCode.OK.value) {
            updateFailData.postValue(
                    FailRestResponse(data2.code, "land forecast fail," + data2.failMsg))
        } else if (data1.listBody!!.isEmpty() || data2.listBody!!.size == 0) {
            updateFailData.postValue(
                    FailRestResponse(Enums.ResponseCode.EXCEPTION_ERROR.value, "data is empty")
            )
        } else {
            if (baseDate == null) {
                setBaseDate()
            }
            val sparseArray = SparseArray<MidForecastModel>()
            parseData(sparseArray, data1, data2)
            _data.postValue(sparseArray)
        }
    }

    private fun parseData(sparseArray: SparseArray<MidForecastModel>, data1: RestResponse<MidLandItem>,
                          data2: RestResponse<MidTempItem>) {
        val midLandItem = data1.listBody!![0]
        val midTempItem = data2.listBody!![0]
        var keyIdx = if (isYesterdayBase) 3 else 4
        for (i in 3..10) {
            if (i < 8) {
                sparseArray.put(keyIdx++, getUniModel(i, midLandItem, midTempItem))
            } else {
                sparseArray.put(keyIdx++, getMonoModel(i, midLandItem, midTempItem))
            }
        }
    }

    private fun getUniModel(i: Int, midLandItem: MidLandItem, midTempItem: MidTempItem): MidForecastModel {
        return try {
            val amRainPercentage = midLandItem.javaClass.getDeclaredField("rnSt" + i + "Am")[midLandItem]
            val pmRainPercentage = midLandItem.javaClass.getDeclaredField("rnSt" + i + "Pm")[midLandItem]
            val amSky = midLandItem.javaClass.getDeclaredField("wf" + i + "Am")[midLandItem]
            val pmSky = midLandItem.javaClass.getDeclaredField("wf" + i + "Pm")[midLandItem]
            val highTemp = midTempItem.javaClass.getDeclaredField("taMax$i")[midTempItem]
            val highTempMax = midTempItem.javaClass.getDeclaredField("taMax" + i + "High")[midTempItem]
            val highTempMin = midTempItem.javaClass.getDeclaredField("taMax" + i + "Low")[midTempItem]
            val lowTemp = midTempItem.javaClass.getDeclaredField("taMin$i")[midTempItem]
            val lowTempMax = midTempItem.javaClass.getDeclaredField("taMin" + i + "High")[midTempItem]
            val lowTempMin = midTempItem.javaClass.getDeclaredField("taMin" + i + "Low")[midTempItem]

            MidForecastModel(
                    lowTemp = getTempVal((lowTemp ?: 0) as Int, (lowTempMax ?: 0) as Int, (lowTempMin ?: 0) as Int),
                    highTemp = getTempVal((highTemp ?: 0) as Int, (highTempMax ?: 0) as Int, (highTempMin ?: 0) as Int),
                    amRainPercentage = amRainPercentage?.toString() ?: "",
                    pmRainPercentage = pmRainPercentage?.toString() ?: "",
                    amSky = CommonUtils.getSkyIcon(amSky?.toString() ?: ""),
                    pmSky = CommonUtils.getSkyIcon(pmSky?.toString() ?: ""),
                    baseDate = CommonUtils.getDateString(i, baseDate)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            MidForecastModel()
        }
    }

    private fun getMonoModel(i: Int, midLandItem: MidLandItem, midTempItem: MidTempItem): MidForecastModel {
        return try {
            val rainPercentage = midLandItem.javaClass.getDeclaredField("rnSt$i")[midLandItem]
            val sky = midLandItem.javaClass.getDeclaredField("wf$i")[midLandItem]
            val highTemp = midTempItem.javaClass.getDeclaredField("taMax$i")[midTempItem]
            val highTempMax = midTempItem.javaClass.getDeclaredField("taMax" + i + "High")[midTempItem]
            val highTempMin = midTempItem.javaClass.getDeclaredField("taMax" + i + "Low")[midTempItem]
            val lowTemp = midTempItem.javaClass.getDeclaredField("taMin$i")[midTempItem]
            val lowTempMax = midTempItem.javaClass.getDeclaredField("taMin" + i + "High")[midTempItem]
            val lowTempMin = midTempItem.javaClass.getDeclaredField("taMin" + i + "Low")[midTempItem]

            MidForecastModel(
                    lowTemp = getTempVal((lowTemp ?: 0) as Int, (lowTempMax ?: 0) as Int, (lowTempMin ?: 0) as Int),
                    highTemp = getTempVal((highTemp ?: 0) as Int, (highTempMax ?: 0) as Int, (highTempMin ?: 0) as Int),
                    dayRainPercentage = rainPercentage?.toString() ?: "",
                    daySky = CommonUtils.getSkyIcon(sky?.toString() ?: ""),
                    baseDate = CommonUtils.getDateString(i, baseDate)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            MidForecastModel()
        }
    }

    private fun getTempVal(`val`: Int, high: Int, low: Int): String {
        try {
            return "" + (`val` + (high - low) / 2)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private var isYesterdayBase = false
    private var baseDate: String? = null
    fun updateMidWeather(landRegionId: String?, tempRegionId: String?) {
        setBaseDate()
        viewModelScope.launch {
            updateMidForecastModel(
                    repository.getMidLandData(baseDate, landRegionId),
                    repository.getMidTempData(baseDate, landRegionId)
            )
        }
    }

    private fun setBaseDate() {
        //중기예보는 6시, 18시에 업데이트됨
        //단기예보는 17시10이후부터 +3일까지 데이터 제공. 이전은 +2일까지 제공
        //중기는 6시의 경우 +3, 18시의 경우 +4 ~ 10일
        var dateTime = DateTime.now().toDateTime(DateTimeZone.forID("Asia/Seoul"))
        if (dateTime.hourOfDay < 6) {
            //전날 18시 예보 요청
            dateTime = dateTime.minusDays(1).withHourOfDay(18)
            isYesterdayBase = true
        } else if (dateTime.hourOfDay < 18) {
            //당일 6시 예보 요청
            dateTime = dateTime.withHourOfDay(6)
        } else {
            //당일 18시 예보 요청
            dateTime = dateTime.withHourOfDay(18)
        }
        dateTime = dateTime.withMinuteOfHour(0)
        baseDate = dateTime.toString("yyyyMMddHHmm")
        CLogger.d(baseDate)
    }
}