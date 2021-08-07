package com.jun.weather.repository.web

import android.content.Context
import com.google.android.gms.location.ActivityRecognition.getClient
import com.jun.weather.R
import com.jun.weather.repository.web.api.ApiClient
import com.jun.weather.repository.web.api.KakaoLocalService
import com.jun.weather.repository.web.entity.KakaoLocationInfoCacheKey
import com.jun.weather.repository.web.entity.KakaoRegionCodeRes
import com.jun.weather.repository.web.entity.RestResponse
import com.jun.weather.repository.web.enums.Enums
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import java.util.*

class KakaoLocationRepository(context: Context) {

    private var AUTHORIZATION_KAKAO: String = context.resources.getString(R.string.key_kakao_auth)

    companion object{
        val map: HashMap<KakaoLocationInfoCacheKey, RestResponse<*>> = HashMap<KakaoLocationInfoCacheKey, RestResponse<*>>()

        fun <T> addToCacheMap(path: String, x: Double, y: Double, restResponse: RestResponse<T>) {
            map[createCacheKey(path, x, y)] = restResponse
        }

        fun containsInCache(path: String, x: Double, y: Double): Boolean {
            return map[createCacheKey(path, x, y)] != null
        }

        fun createCacheKey(path: String, x: Double, y: Double): KakaoLocationInfoCacheKey {
            val key = KakaoLocationInfoCacheKey()
            key.urlPath = path
            key.x = x
            key.y = y
            return key
        }
    }

    @Throws(IOException::class)
    private fun <T> isRequestSuccess(response: Response<T>): Boolean {
        return !(!response.isSuccessful || response.body()==null)
    }

    private fun <T> parseFailRequest(response: Response<T>): RestResponse<T>{
        val restResponse = RestResponse<T>()
        if (!response.isSuccessful) {
            restResponse.code = response.code()
            val body: ResponseBody? = response.errorBody()
            restResponse.failMsg = if (body == null) "" else body.string()
        }
        val body = response.body()
        if (body == null) {
            restResponse.code = Enums.ResponseCode.EXCEPTION_ERROR.value
            restResponse.failMsg = "body is null"
        }
        return restResponse
    }

    fun getLocationStringAddress(lng: Double, lat: Double): RestResponse<KakaoRegionCodeRes> {
        val kakaoLocalService = ApiClient.getClient(Enums.CLIENT_DEST.KAKAO_LOCAL).create(KakaoLocalService::class.java)
        val query: MutableMap<String, String> = HashMap()
        query["x"] = lng.toString()
        query["y"] = lat.toString()
        val call: Call<KakaoRegionCodeRes> = kakaoLocalService.getReverseGeocodeAddress(AUTHORIZATION_KAKAO, query)
        val urlPath = call.request().url.encodedPath
        if (containsInCache(urlPath, lng, lat)) {
            return map[createCacheKey(urlPath, lng, lat)] as RestResponse<KakaoRegionCodeRes>
        }
        var restResponse: RestResponse<KakaoRegionCodeRes> = RestResponse()
        try {
            val response: Response<KakaoRegionCodeRes> = call.execute()
            if (isRequestSuccess(response)) {
                val body: KakaoRegionCodeRes = response.body() ?: throw Exception()
                restResponse.code = response.code()
                restResponse.singleBody = body
                addToCacheMap(urlPath, lng, lat, restResponse)
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