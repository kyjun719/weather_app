package com.jun.weather.repository

import android.content.Context
import com.jun.weather.repository.db.FileRepository
import com.jun.weather.repository.web.KakaoLocationRepository
import com.jun.weather.repository.web.WeatherInfoRepository
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AppRepository @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val weatherInfoRepository = WeatherInfoRepository(context)
    private val kakaoLocationRepository = KakaoLocationRepository(context)
    private val fileRepository: FileRepository = FileRepository()
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO

    companion object {
        private var instance: AppRepository? = null
//        fun getInstance(context: Context): AppRepository {
//            if (instance == null) {
//                instance = AppRepository(context)
//            }
//            return instance!!
//        }

//        fun getInstance(context: Context, defaultDispatcher: CoroutineDispatcher): AppRepository? {
//            if (instance == null) {
//                instance = AppRepository(context, defaultDispatcher)
//            }
//            return instance
//        }
    }

    suspend fun getNowWeather(baseDate: String?, baseTime: String?, nx: Int, ny: Int) =
            withContext(defaultDispatcher) {
                weatherInfoRepository.getNowWeather(baseDate!!, baseTime!!, nx, ny)
            }

    suspend fun getShortForecastWeather(baseDate: String?, baseTime: String?, nx: Int, ny: Int) =
            withContext(defaultDispatcher) {
                weatherInfoRepository.getShortForecast(baseDate!!, baseTime!!, nx, ny)
            }

    suspend fun getMidLandData(date: String?, regionId: String?) =
            withContext(defaultDispatcher) {
                weatherInfoRepository.getMidLandForecast(date!!, regionId!!)
            }

    suspend fun getMidTempData(date: String?, regionId: String?) =
            withContext(defaultDispatcher) {
                weatherInfoRepository.getMidTempForecast(date!!, regionId!!)
            }

    suspend fun getShortWeatherPointList(context: Context) =
            withContext(defaultDispatcher) {
                //TODO file보다는 엑셀을 room에 넣는건...?
                fileRepository.getShortWeatherPoint(context.resources)
            }

    suspend fun getLowTempData(baseDate: String?, baseTime: String?, nx: Int, ny: Int) =
            withContext(defaultDispatcher) {
                weatherInfoRepository.getDayTempData(baseDate!!, baseTime!!, nx, ny, false)
            }

    suspend fun getHighTempData(baseDate: String?, baseTime: String?, nx: Int, ny: Int) =
            withContext(defaultDispatcher) {
                weatherInfoRepository.getDayTempData(baseDate!!, baseTime!!, nx, ny, true)
            }

    suspend fun getUltraShortForecastData(baseDate: String?, baseTime: String?, nx: Int, ny: Int) =
            withContext(defaultDispatcher) {
                weatherInfoRepository.getUltraShortForecast(baseDate!!, baseTime!!, nx, ny)
            }

    suspend fun getKakaoRegionCodeRes(lng: Double, lat: Double) =
            withContext(defaultDispatcher) {
                kakaoLocationRepository.getLocationStringAddress(lng, lat)
            }
}