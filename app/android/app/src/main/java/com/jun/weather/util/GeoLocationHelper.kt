package com.jun.weather.util

import android.app.Activity
import android.content.Context
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import com.jun.weather.BaseApplication
import com.jun.weather.repository.web.enums.Enums.ResponseCode
import com.jun.weather.ui.entity.FailRestResponse
import com.jun.weather.ui.entity.NowLocation
import com.jun.weather.ui.entity.WeatherPoint
import com.jun.weather.ui.entity.WeatherPointModel
import com.jun.weather.util.GPSHelper.LocationResultListener
import com.jun.weather.viewmodel.CustomViewModelProvider
import com.jun.weather.viewmodel.NowLocationViewModel
import com.jun.weather.viewmodel.WeatherPointViewModel
import java.util.*

class GeoLocationHelper private constructor() {
    interface GeoLocationResultListener {
        fun onSuccess(locationPoint: WeatherPoint)
        fun onFail(failRestResponse: FailRestResponse)
    }

    interface InitPointListener {
        fun onFinish(point: WeatherPoint?)
    }

    private var geoLocationResultListener: GeoLocationResultListener? = null
    private var nowLocationViewModel: NowLocationViewModel? = null
    private var weatherPointViewModel: WeatherPointViewModel? = null
    private var isInitialized = false
    private var initPointListener: InitPointListener? = null

    private val locationResultListener: LocationResultListener = object : LocationResultListener {
        override fun onSuccess(location: Location) {
            requestGeocodeLocation(location)
        }

        override fun onCanceled(code: ResponseCode, msg: String) {
            geoLocationResultListener!!.onFail(FailRestResponse(code.value, msg))
        }

        override fun onFail(code: ResponseCode, msg: String) {
            geoLocationResultListener!!.onFail(FailRestResponse(code.value, msg))
        }
    }

    fun init(context: Context, lifecycleOwner: LifecycleOwner?) {
        if (isInitialized) {
            return
        }
        isInitialized = true
        val application = (context as Activity).application as BaseApplication
        val appRepository = application.repository
        nowLocationViewModel = CustomViewModelProvider(appRepository).getViewModel(
                (context as AppCompatActivity), NowLocationViewModel::class.java)
        weatherPointViewModel = CustomViewModelProvider(appRepository).getViewModel(
                context, WeatherPointViewModel::class.java)
        val weatherPointModelList = weatherPointViewModel!!.data
        val nowLocationModelLiveData = nowLocationViewModel!!.data
        nowLocationViewModel!!.data.observe(lifecycleOwner!!, { data: List<NowLocation>? -> parseData(data, weatherPointModelList.value) })
        nowLocationViewModel!!.updateFailData.observe(lifecycleOwner, { failRestResponse -> geoLocationResultListener!!.onFail(failRestResponse) })
        weatherPointViewModel!!.data.observe(lifecycleOwner, { data: WeatherPointModel ->
            parseData(nowLocationModelLiveData.value, data)
            if (initPointListener != null) {
                notifyInitPoint(data)
            }
        })
        weatherPointViewModel!!.updateFailData.observe(lifecycleOwner, { failRestResponse -> geoLocationResultListener!!.onFail(failRestResponse) })
        weatherPointViewModel!!.updatePointData(context)
    }

    private fun parseData(nowLocationList: List<NowLocation>?, weatherPointModel: WeatherPointModel?) {
        if (nowLocationList == null || weatherPointModel == null) {
            return
        }
        val weatherPoint = getPoint(nowLocationList, weatherPointModel)
        if (weatherPoint == null) {
            geoLocationResultListener!!.onFail(FailRestResponse(ResponseCode.EXCEPTION_ERROR.value, "fail to find weather point"))
        } else {
            geoLocationResultListener!!.onSuccess(weatherPoint)
            GPSHelper.instance!!.stopGPSLocationUpdate()
        }
    }

    private fun getPoint(nowLocationList: List<NowLocation>, weatherPointModel: WeatherPointModel): WeatherPoint? {
        var weatherPoint: WeatherPoint? = null
        val indexMap: HashMap<String, Array<Int>> = weatherPointModel.indexMap
        for (nowLocation in nowLocationList) {
            val deg1 = nowLocation.deg1
            val deg2 = nowLocation.deg2
            var rangeIdx: Array<Int>? = arrayOf(0, weatherPointModel.weatherPointList.size)
            if (indexMap["$deg1 $deg2"] != null) {
                rangeIdx = indexMap["$deg1 $deg2"]
            } else if (indexMap["$deg1 "] != null) {
                rangeIdx = indexMap["$deg1 "]
            }
            CLogger.d(nowLocation.toString() + ">>" + Arrays.toString(rangeIdx))
            assert(rangeIdx != null)
            for (i in rangeIdx!![0]..rangeIdx[1]) {
                val weatherPoint1 = weatherPointModel.weatherPointList[i]
                if (weatherPoint1.deg_1 == nowLocation.deg1 && weatherPoint1.deg_2 == nowLocation.deg2 && weatherPoint1.deg_3 == nowLocation.deg3) {
                    weatherPoint = weatherPoint1
                    break
                }
            }
        }
        return weatherPoint
    }

    fun getNowLocation(context: Context, geoLocationListener: GeoLocationResultListener?) {
        geoLocationResultListener = geoLocationListener
        GPSHelper.instance!!.init(locationResultListener)
        GPSHelper.instance!!.getNowLocation(context)
    }

    private fun requestGeocodeLocation(location: Location) {
        val lng = location.longitude
        val lat = location.latitude
        CLogger.d("location::$lng,$lat")
        nowLocationViewModel!!.updateNowLocation(lng, lat)
    }

    fun registerInitPointListener(listener: InitPointListener?) {
        initPointListener = listener
        CLogger.d("weatherPointViewModel is " + if (weatherPointViewModel!!.data.value == null) "null" else "not null")
        val weatherPointModel = weatherPointViewModel!!.data.value ?: return
        notifyInitPoint(weatherPointModel)
    }

    private fun notifyInitPoint(weatherPointModel: WeatherPointModel) {
        val list: MutableList<NowLocation> = ArrayList()
        list.add(NowLocation(
                "서울특별시 중구 명동",
                "1114055000",
                "서울특별시",
                "중구",
                "명동",
                126.98581171546633,
                37.56004798513031
        )
        )
        initPointListener!!.onFinish(getPoint(list, weatherPointModel))
    }

    companion object {
        var instance: GeoLocationHelper? = null
            get() {
                if (field == null) {
                    field = GeoLocationHelper()
                }
                return field
            }
            private set
    }
}