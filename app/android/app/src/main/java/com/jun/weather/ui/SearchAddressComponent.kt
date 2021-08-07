package com.jun.weather.ui

import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.jun.weather.BaseApplication
import com.jun.weather.R
import com.jun.weather.databinding.ComponentSearchAddressBinding
import com.jun.weather.repository.web.enums.Enums
import com.jun.weather.ui.entity.FailRestResponse
import com.jun.weather.ui.entity.WeatherPoint
import com.jun.weather.util.CLogger
import com.jun.weather.util.GeoLocationHelper
import com.jun.weather.util.PreferenceUtils
import com.jun.weather.viewmodel.CustomViewModelProvider
import com.jun.weather.viewmodel.WeatherPointViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList

class SearchAddressComponent(val activity: AppCompatActivity, val binding: ComponentSearchAddressBinding) {
    private var weatherPointViewModel: WeatherPointViewModel
    private lateinit var adapter: ArrayAdapter<WeatherPoint>

    init {
        val application = activity.application as BaseApplication
        weatherPointViewModel = CustomViewModelProvider(application.repository!!)
                .getViewModel(activity, WeatherPointViewModel::class.java)

        binding.lifecycleOwner = activity
        binding.viewModel = weatherPointViewModel

        initView()
        initObserver()

        setInitLocation()
    }

    private fun initView() {
        adapter = ArrayAdapter(activity, android.R.layout.simple_dropdown_item_1line, ArrayList<WeatherPoint>())
        binding.autoTextAddress.setAdapter(adapter)
        binding.autoTextAddress.onItemClickListener = AdapterView.OnItemClickListener { parent: AdapterView<*>, view: View?, position: Int, id: Long ->
            (activity as MainActivity).hideKeyboard(binding.autoTextAddress)

            CLogger.d("itemClicked::" + position + "," + parent.getItemAtPosition(position))
            val point = parent.getItemAtPosition(position) as WeatherPoint
            CLogger.d(point.toString() + ">>" + point.x + "," + point.y + "," + point.midWeatherCode + "," + point.midTempCode)

            updateGPSButtonIcon(false)
            weatherPointViewModel.postSelectedPoint(point)
        }

        binding.recentPoint1.setOnClickListener {
            weatherPointViewModel.pointArr[0].value?.let { point ->
                updateWeatherFromPoint(point)
            }
        }
        binding.recentPoint2.setOnClickListener { v: View? ->
            weatherPointViewModel.pointArr[1].value?.let { point ->
                updateWeatherFromPoint(point)
            }
        }
        binding.recentPoint3.setOnClickListener { v: View? ->
            weatherPointViewModel.pointArr[2].value?.let { point ->
                updateWeatherFromPoint(point)
            }
        }

        binding.btnGps.setOnClickListener { v: View? ->
            GeoLocationHelper.instance!!.getNowLocation(activity, object : GeoLocationHelper.GeoLocationResultListener {
                override fun onSuccess(locationPoint: WeatherPoint) {
                    CLogger.d("find Point::$locationPoint")
                    updateWeatherFromPoint(locationPoint)
                    updateGPSButtonIcon(true)
                }

                override fun onFail(failRestResponse: FailRestResponse) {
                    CLogger.d(failRestResponse.code.toString() + ">>" + failRestResponse.failMsg)
                    if (failRestResponse.code == Enums.ResponseCode.PERMISSION_NOT_GRANTED.value ||
                            failRestResponse.code == Enums.ResponseCode.SETTING_NOT_GRANTED.value) {
                                GlobalScope.launch(Dispatchers.Main) {
                                    Toast.makeText(activity, failRestResponse.failMsg, Toast.LENGTH_SHORT).show()
                                }
                    } else {
                        GlobalScope.launch(Dispatchers.Main) {
                            Toast.makeText(activity, "위치정보를 가져오는데 실패하였습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    updateGPSButtonIcon(false)
                }
            })
        }
    }

    private fun initObserver() {
        weatherPointViewModel.data.observe(activity, {
            it?.let {
                adapter.addAll(it.weatherPointList)
                adapter.notifyDataSetChanged()
            }
        })
    }

    private fun updateGPSButtonIcon(b: Boolean) {
        GlobalScope.launch(Dispatchers.Main) {
            binding.btnGps.also {
                if(b) it.setImageResource(R.drawable.ic_baseline_gps_accent)
                else it.setImageResource(R.drawable.ic_baseline_gps_black)
            }
        }
    }

    private fun setInitLocation() {
        if (PreferenceUtils.getInstance().pointList.isNotEmpty()) {
            updateWeatherFromPoint(PreferenceUtils.getInstance().pointList[0])
        } else {
            GeoLocationHelper.instance!!.registerInitPointListener(object : GeoLocationHelper.InitPointListener {
                override fun onFinish(point: WeatherPoint?) {
                    point?.let { updateWeatherFromPoint(it) }
                }
            })
        }
    }

    private fun updateWeatherFromPoint(locationPoint: WeatherPoint) {
        weatherPointViewModel.postSelectedPoint(locationPoint)
        binding.autoTextAddress.clearFocus()
    }
}