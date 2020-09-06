package com.jun.weather.repository.web.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.jun.weather.BaseApplication.isDebug;

public class APIClient {
    private static Retrofit retrofit = null;
    private static final String baseShortURL = "http://apis.data.go.kr/1360000/VilageFcstInfoService/";
    private static final String baseMidURL = "http://apis.data.go.kr/1360000/MidFcstInfoService/";
    private static final String baseKakaoLocalURL = "https://dapi.kakao.com/";

    public enum CLIENT_DEST {
        SHORT_WEATHER, MID_WEATHER, KAKAO_LOCAL;
    }

    public static Retrofit getClient(CLIENT_DEST val) {
        String baseUrl = "";
        switch (val) {
            case SHORT_WEATHER:
                baseUrl = baseShortURL;
                break;
            case MID_WEATHER:
                baseUrl = baseMidURL;
                break;
            case KAKAO_LOCAL:
                baseUrl = baseKakaoLocalURL;
                break;
        }

        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        if(isDebug()) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            //interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClientBuilder.addInterceptor(interceptor);
        }

        OkHttpClient client = httpClientBuilder.build();

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit;
    }
}
