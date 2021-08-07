package com.jun.weather.repository.web.api

import com.jun.weather.BaseApplication
import com.jun.weather.repository.web.enums.Enums
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val baseShortURL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/"
    private const val baseMidURL = "http://apis.data.go.kr/1360000/MidFcstInfoService/"
    private const val baseKakaoLocalURL = "https://dapi.kakao.com/"

    fun getClient(dest: Enums.CLIENT_DEST): Retrofit {
        val baseUrl = when (dest) {
            Enums.CLIENT_DEST.SHORT_WEATHER -> baseShortURL
            Enums.CLIENT_DEST.MID_WEATHER -> baseMidURL
            Enums.CLIENT_DEST.KAKAO_LOCAL -> baseKakaoLocalURL
        }
        val httpClientBuilder = OkHttpClient.Builder()
        if (BaseApplication.isDebug) {
            val interceptor = HttpLoggingInterceptor()
            //interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS)
            httpClientBuilder.addInterceptor(interceptor)
        }
        val client = httpClientBuilder.build()
        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
    }
}