package com.jun.weather.repository.web.api

import com.jun.weather.BaseApplication
import com.jun.weather.repository.web.enums.Enums
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private var retrofit: Retrofit? = null
    private val baseShortURL = "http://apis.data.go.kr/1360000/VilageFcstInfoService/"
    private val baseMidURL = "http://apis.data.go.kr/1360000/MidFcstInfoService/"
    private val baseKakaoLocalURL = "https://dapi.kakao.com/"

    fun getClient(`val`: Enums.CLIENT_DEST): Retrofit {
        var baseUrl = ""
        when (`val`) {
            Enums.CLIENT_DEST.SHORT_WEATHER -> baseUrl = baseShortURL
            Enums.CLIENT_DEST.MID_WEATHER -> baseUrl = baseMidURL
            Enums.CLIENT_DEST.KAKAO_LOCAL -> baseUrl = baseKakaoLocalURL
        }
        val httpClientBuilder = OkHttpClient.Builder()
        if (BaseApplication.isDebug()) {
            val interceptor = HttpLoggingInterceptor()
            //interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClientBuilder.addInterceptor(interceptor)
        }
        val client = httpClientBuilder.build()
        retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        return retrofit!!
    }
}