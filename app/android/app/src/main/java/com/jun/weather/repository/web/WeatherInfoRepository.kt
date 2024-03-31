package com.jun.weather.repository.web

import android.content.Context
import com.jun.weather.R
import com.jun.weather.repository.web.api.ApiClient
import com.jun.weather.repository.web.api.MidWeatherInfoService
import com.jun.weather.repository.web.api.ShortWeatherInfoService
import com.jun.weather.repository.web.entity.*
import com.jun.weather.repository.web.enums.Enums
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.components.SingletonComponent
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import java.util.*
import javax.inject.Inject

class WeatherInfoRepository(context: Context) {
    private val KEY_WEATHER_FORECAST = context.resources.getString(R.string.key_weather)

    companion object {
        private val map = HashMap<WeatherInfoCacheKey, RestResponse<*>>()

        fun createCacheKey(ordinal: Int, midDate: String, regionId: String): WeatherInfoCacheKey {
            return createCacheKey(ordinal, "", "", -1, -1, midDate, regionId)
        }

        fun createCacheKey(ordinal: Int, baseDate: String, baseTime: String, nx: Int, ny: Int,
                                   midDate: String = "", regionId: String = ""): WeatherInfoCacheKey {
            val key = WeatherInfoCacheKey()
            key.key = ordinal
            key.baseDate = baseDate
            key.baseTime = baseTime
            key.nx = nx
            key.ny = ny
            key.midDate = midDate
            key.regionId = regionId
            return key
        }

        fun <T> addToCacheMap(ordinal: Int, baseDate: String, baseTime: String, nx: Int, ny: Int, restResponse: RestResponse<T>) {
            map[createCacheKey(ordinal, baseDate, baseTime, nx, ny)] = restResponse
        }

        fun containsInCache(ordinal: Int, baseDate: String, baseTime: String, nx: Int, ny: Int): Boolean {
            return map[createCacheKey(ordinal, baseDate, baseTime, nx, ny)] != null
        }

        fun <T> addToCacheMap(ordinal: Int, midDate: String, regionId: String, restResponse: RestResponse<T>) {
            map[createCacheKey(ordinal, midDate, regionId)] = restResponse
        }

        fun containsInCache(ordinal: Int, midDate: String, regionId: String): Boolean {
            return map[createCacheKey(ordinal, midDate, regionId)] != null
        }
    }

    @Throws(IOException::class)
    private fun <T> isRequestSuccess(response: Response<ApiResponse<T>>): Boolean {
        return !(!response.isSuccessful || response.body()==null || !(response.body()?.response!!.header.resultCode == "00"))
    }

    private fun <T> parseFailRequest(response: Response<ApiResponse<T>>): RestResponse<T>{
        val restResponse = RestResponse<T>()
        if (!response.isSuccessful) {
            restResponse.code = response.code()
            val body: ResponseBody? = response.errorBody()
            restResponse.failMsg = body?.string() ?: ""
        }
        val body: ApiResponse<T>? = response.body()
        if (body == null) {
            restResponse.code = Enums.ResponseCode.EXCEPTION_ERROR.value
            restResponse.failMsg = "body is null"
        }
        body?.let {
            if(!it.response.header.resultCode.equals("00")) {
                restResponse.code = Enums.ResponseCode.BAD_REQUEST.value
                restResponse.failMsg = it.response.header.resultMsg
            }
        }
        return restResponse
    }



    fun getNowWeather(baseDate: String, baseTime: String, nx: Int, ny: Int): RestResponse<ObserveItem> {
        if (containsInCache(Enums.KEY.NOW.ordinal, baseDate, baseTime, nx, ny)) {
            return map[createCacheKey(Enums.KEY.NOW.ordinal, baseDate, baseTime, nx, ny)] as RestResponse<ObserveItem>
        }
        val shortWeatherInfoService = ApiClient.getClient(Enums.CLIENT_DEST.SHORT_WEATHER).create(ShortWeatherInfoService::class.java)
        val map: Map<String, String> = mapOf(
                "serviceKey" to KEY_WEATHER_FORECAST,
                "numOfRows" to "10",
                "pageNo" to "1",
                "dataType" to "JSON",
                "base_date" to baseDate,
                "base_time" to baseTime,
                "nx" to nx.toString(),
                "ny" to ny.toString()
        )

        val call = shortWeatherInfoService.getUltraSrtNcst(map)
        var restResponse = RestResponse<ObserveItem>()
        try {
            val response: Response<ApiResponse<ObserveItem>> = call.execute()
            if (isRequestSuccess(response)) {
                val body: ApiResponse<ObserveItem> = response.body() ?: throw Exception()
                restResponse.code = response.code()
                restResponse.listBody = body.response.body.items.item
                addToCacheMap(Enums.KEY.NOW.ordinal, baseDate, baseTime, nx, ny, restResponse)
            } else {
                restResponse = parseFailRequest(response)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            restResponse.code = Enums.ResponseCode.EXCEPTION_ERROR.value
            restResponse.failMsg = e.message
        }
        return restResponse
    }

    fun getDayTempData(baseDate: String, baseTime: String, nx: Int, ny: Int, isHigh: Boolean): RestResponse<ForecastItem> {
        val key: Int = if (isHigh) Enums.KEY.HIGH_TEMP.ordinal else Enums.KEY.LOW_TEMP.ordinal
        if (containsInCache(key, baseDate, baseTime, nx, ny)) {
            return map[createCacheKey(key, baseDate, baseTime, nx, ny)] as RestResponse<ForecastItem>
        }
        val shortWeatherInfoService = ApiClient.getClient(Enums.CLIENT_DEST.SHORT_WEATHER).create(ShortWeatherInfoService::class.java)
        val map: Map<String, String> = mapOf(
                "serviceKey" to KEY_WEATHER_FORECAST,
                "numOfRows" to "200",
                "pageNo" to "1",
                "dataType" to "JSON",
                "base_date" to baseDate,
                "base_time" to baseTime,
                "nx" to nx.toString(),
                "ny" to ny.toString()
        )

        val call = shortWeatherInfoService.getVilageFcst(map)
        var restResponse = RestResponse<ForecastItem>()
        try {
            val response: Response<ApiResponse<ForecastItem>> = call.execute()
            if (isRequestSuccess(response)) {
                val body: ApiResponse<ForecastItem> = response.body() ?: throw Exception()
                restResponse.code = response.code()
                restResponse.listBody = body.response.body.items.item
                addToCacheMap(key, baseDate, baseTime, nx, ny, restResponse)
            } else {
                restResponse = parseFailRequest(response)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            restResponse.code = Enums.ResponseCode.EXCEPTION_ERROR.value
            restResponse.failMsg = e.message
        }
        return restResponse
    }

    private val MAX_ITEM_CNT = 1000
    fun getShortForecast(baseDate: String, baseTime: String, nx: Int, ny: Int): RestResponse<ForecastItem>? {
        if (containsInCache(Enums.KEY.SHORT_FORECAST.ordinal, baseDate, baseTime, nx, ny)) {
            return map[createCacheKey(Enums.KEY.SHORT_FORECAST.ordinal, baseDate, baseTime, nx, ny)] as RestResponse<ForecastItem>?
        }
        val shortWeatherInfoService: ShortWeatherInfoService = ApiClient.getClient(Enums.CLIENT_DEST.SHORT_WEATHER).create(ShortWeatherInfoService::class.java)

        val map: Map<String, String> = mapOf(
                "serviceKey" to KEY_WEATHER_FORECAST,
                "numOfRows" to MAX_ITEM_CNT.toString(),
                "pageNo" to "1",
                "dataType" to "JSON",
                "base_date" to baseDate,
                "base_time" to baseTime,
                "nx" to nx.toString(),
                "ny" to ny.toString()
        )

        val call = shortWeatherInfoService.getVilageFcst(map)
        var restResponse = RestResponse<ForecastItem>()
        try {
            val response: Response<ApiResponse<ForecastItem>> = call.execute()
            if (isRequestSuccess(response)) {
                val body: ApiResponse<ForecastItem> = response.body() ?: throw Exception()
                restResponse.code = response.code()
                restResponse.listBody = body.response.body.items.item
                addToCacheMap(Enums.KEY.SHORT_FORECAST.ordinal, baseDate, baseTime, nx, ny, restResponse)
            } else {
                restResponse = parseFailRequest(response)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            restResponse.code = Enums.ResponseCode.EXCEPTION_ERROR.value
            restResponse.failMsg = e.message
        }
        return restResponse
    }

    fun getUltraShortForecast(baseDate: String, baseTime: String, nx: Int, ny: Int): RestResponse<ForecastItem> {
        if (containsInCache(Enums.KEY.ULTRA_SHORT_FORECAST.ordinal, baseDate, baseTime, nx, ny)) {
            return map[createCacheKey(Enums.KEY.ULTRA_SHORT_FORECAST.ordinal, baseDate, baseTime, nx, ny)] as RestResponse<ForecastItem>
        }
        val shortWeatherInfoService = ApiClient.getClient(Enums.CLIENT_DEST.SHORT_WEATHER).create(ShortWeatherInfoService::class.java)
        val map: Map<String, String> = mapOf(
                "serviceKey" to KEY_WEATHER_FORECAST,
                "numOfRows" to MAX_ITEM_CNT.toString(),
                "pageNo" to "1",
                "dataType" to "JSON",
                "base_date" to baseDate,
                "base_time" to baseTime,
                "nx" to nx.toString(),
                "ny" to ny.toString(),
        )

        val call = shortWeatherInfoService.getUltraSrtFcst(map)
        var restResponse = RestResponse<ForecastItem>()
        try {
            val response: Response<ApiResponse<ForecastItem>> = call.execute()
            if (isRequestSuccess(response)) {
                val body = response.body() ?: throw Exception()
                restResponse.code = response.code()
                restResponse.listBody = body.response.body.items.item
                addToCacheMap(Enums.KEY.ULTRA_SHORT_FORECAST.ordinal, baseDate, baseTime, nx, ny, restResponse)
            } else {
                restResponse = parseFailRequest(response)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            restResponse.code = Enums.ResponseCode.EXCEPTION_ERROR.value
            restResponse.failMsg = e.message
        }
        return restResponse
    }

    fun getMidLandForecast(date: String, regionId: String): RestResponse<MidLandItem> {
        if (containsInCache(Enums.KEY.MID_LAND.ordinal, date, regionId)) {
            return map[createCacheKey(Enums.KEY.MID_LAND.ordinal, date, regionId)] as RestResponse<MidLandItem>
        }
        val shortWeatherInfoService = ApiClient.getClient(Enums.CLIENT_DEST.MID_WEATHER).create(MidWeatherInfoService::class.java)
        val map: Map<String, String> = mapOf(
                "serviceKey" to KEY_WEATHER_FORECAST,
                "numOfRows" to "10",
                "pageNo" to "1",
                "dataType" to "JSON",
                "regId" to regionId,
                "tmFc" to date,
        )

        val call: Call<ApiResponse<MidLandItem>> = shortWeatherInfoService.getMidLandFcst(map)
        var restResponse = RestResponse<MidLandItem>()
        try {
            val response: Response<ApiResponse<MidLandItem>> = call.execute()
            if (isRequestSuccess(response)) {
                val body: ApiResponse<MidLandItem> = response.body() ?: throw Exception()
                restResponse.code = response.code()
                restResponse.listBody = body.response.body.items.item
                addToCacheMap(Enums.KEY.MID_LAND.ordinal, date, regionId, restResponse)
            } else {
                restResponse = parseFailRequest(response)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            restResponse.code = Enums.ResponseCode.EXCEPTION_ERROR.value
            restResponse.failMsg = e.message
        }
        return restResponse
    }

    fun getMidTempForecast(date: String, regionId: String): RestResponse<MidTempItem> {
        if (containsInCache(Enums.KEY.MID_TEMP.ordinal, date, regionId)) {
            return map[createCacheKey(Enums.KEY.MID_TEMP.ordinal, date, regionId)] as RestResponse<MidTempItem>
        }
        val shortWeatherInfoService = ApiClient.getClient(Enums.CLIENT_DEST.MID_WEATHER).create(MidWeatherInfoService::class.java)
        val map: Map<String, String> = mapOf(
                "serviceKey" to KEY_WEATHER_FORECAST,
                "numOfRows" to "10",
                "pageNo" to "1",
                "dataType" to "JSON",
                "regId" to regionId,
                "tmFc" to date,
        )

        val call = shortWeatherInfoService.getMidTa(map)
        var restResponse = RestResponse<MidTempItem>()
        try {
            val response: Response<ApiResponse<MidTempItem>> = call.execute()
            if (isRequestSuccess(response)) {
                val body: ApiResponse<MidTempItem> = response.body() ?: throw Exception()
                restResponse.code = response.code()
                restResponse.listBody = body.response.body.items.item
                addToCacheMap(Enums.KEY.MID_TEMP.ordinal, date, regionId, restResponse)
            } else {
                restResponse = parseFailRequest(response)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            restResponse.code = Enums.ResponseCode.EXCEPTION_ERROR.value
            restResponse.failMsg = e.message
        }
        return restResponse
    }
}