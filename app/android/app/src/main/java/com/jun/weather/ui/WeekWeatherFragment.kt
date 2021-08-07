package com.jun.weather.ui

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.jun.weather.BaseApplication
import com.jun.weather.R
import com.jun.weather.databinding.FragmentWeekWeatherBinding
import com.jun.weather.ui.component.WeeklyItemLayout
import com.jun.weather.ui.component.WeeklyItemView
import com.jun.weather.ui.entity.*
import com.jun.weather.util.CLogger
import com.jun.weather.util.CLogger.Companion.d
import com.jun.weather.util.CommonUtils.getDateString
import com.jun.weather.viewmodel.*
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.DateTimeZone

class WeekWeatherFragment : Fragment() {
    private lateinit var mActivity: Activity
    private lateinit var midWeatherViewModel: MidWeatherViewModel
    private lateinit var weatherViewModel: NowWeatherViewModel
    private lateinit var dayForecastViewModel: DayForecastViewModel
    private lateinit var weatherPointViewModel: WeatherPointViewModel

    private lateinit var binding: FragmentWeekWeatherBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_week_weather, container, false)
        val application = requireActivity().application as BaseApplication
        midWeatherViewModel = CustomViewModelProvider(application.repository!!)
                .getViewModel((requireActivity() as AppCompatActivity), MidWeatherViewModel::class.java)
        weatherViewModel = CustomViewModelProvider(application.repository!!)
                .getViewModel(this, NowWeatherViewModel::class.java)
        dayForecastViewModel = CustomViewModelProvider(application.repository!!)
                .getViewModel((requireActivity() as AppCompatActivity), DayForecastViewModel::class.java)
        weatherPointViewModel = CustomViewModelProvider(application.repository!!).getViewModel((requireActivity() as AppCompatActivity), WeatherPointViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mActivity = requireActivity()

        binding.dataSet = false
        initView()
        initObserver()
    }

    private fun initView() {}
    private fun initObserver() {
        weatherPointViewModel.selectedPoint.observe(viewLifecycleOwner, {
            it?.let { updateWeather(it) }
        })

        midWeatherViewModel.updateFailData.observe(viewLifecycleOwner, { failRestResponse: FailRestResponse -> midWeatherDataFailProcess(failRestResponse) })
        val data1 = weatherViewModel.data
        val data2 = dayForecastViewModel.data
        val data3 = midWeatherViewModel.data

        data1.observe(viewLifecycleOwner, { data: NowWeatherModel? -> updateMidWeatherData(data, data2.value, data3.value) })
        data2.observe(viewLifecycleOwner, { data: List<DayForecastModel>? -> updateMidWeatherData(data1.value, data, data3.value) })
        data3.observe(viewLifecycleOwner, { data: SparseArray<MidForecastModel>? -> updateMidWeatherData(data1.value, data2.value, data) })
    }

    private fun updateMidWeatherData(nowWeatherModel: NowWeatherModel?, dayForecastModels: List<DayForecastModel>?,
                                     midForecastModelSparseArray: SparseArray<MidForecastModel>?) {
        if (nowWeatherModel == null || dayForecastModels == null || midForecastModelSparseArray == null) {
            return
        }
        if (!isVisible) {
            return
        }
        binding.dataSet = true
        (mActivity as MainActivity?)!!.setLoading(false)

        val shortForecastSparseArray = getShortForecastArray(nowWeatherModel, dayForecastModels)
        for (i in 0 until shortForecastSparseArray.size()) {
            val key = shortForecastSparseArray.keyAt(i)
            midForecastModelSparseArray.put(key, shortForecastSparseArray[key])
        }
        val dateTime = DateTime.now().toDateTime(DateTimeZone.forID("Asia/Seoul"))

        //init calendar UI
        val nowDay = dateTime.dayOfWeek
        if (nowDay < DateTimeConstants.SUNDAY) {
            for (i in 0 until nowDay) {
                binding.weeklyRow1.setWeeklyItemVisibility(i + 1, View.GONE)
            }
        }
        if (nowDay == DateTimeConstants.SUNDAY || nowDay < DateTimeConstants.THURSDAY) {
            binding.weeklyRow3.visibility = View.GONE
        }

        CLogger.d(midForecastModelSparseArray)
        var startIdx = nowDay % 7 + 1
        var idx = startIdx
        var arrayIdx = 1
        while (idx <= 7) {
            val model = midForecastModelSparseArray[arrayIdx]
            d(dateTime.plusDays(arrayIdx - 1).toString() + ">>" + model)
            if (model == null) {
                arrayIdx++
                idx++
                continue
            }
            binding.weeklyRow1.setDateText(idx, dateTime.plusDays(arrayIdx - 1).toString("MM/dd"))
            setDataIntoRow(binding.weeklyRow1, model, idx)
            arrayIdx++
            idx++
        }


        run {
            var i = 1
            while (i <= 7) {
                val model = midForecastModelSparseArray[arrayIdx]
                d(dateTime.plusDays(arrayIdx - 1).toString() + ">>" + model)
                if (model == null) {
                    i++
                    arrayIdx++
                    continue
                }
                binding.weeklyRow2.setDateText(i, dateTime.plusDays(arrayIdx - 1).toString("MM/dd"))
                setDataIntoRow(binding.weeklyRow2, model, i)
                i++
                arrayIdx++
            }
        }

        if (arrayIdx <= 10) {
            val endIdx = 10 - arrayIdx + 1
            var i = 1
            while (i <= endIdx) {
                val model = midForecastModelSparseArray[arrayIdx]
                d(dateTime.plusDays(arrayIdx - 1).toString() + ">>" + model)
                if (model == null) {
                    i++
                    arrayIdx++
                    continue
                }
                binding.weeklyRow3.setDateText(i, dateTime.plusDays(arrayIdx - 1).toString("MM/dd"))
                setDataIntoRow(binding.weeklyRow3, model, i)
                i++
                arrayIdx++
            }
            for(j in (endIdx+1)..7) {
                binding.weeklyRow3.setWeeklyItemVisibility(i, View.GONE)
            }
        } else {
            for(i in (startIdx+3)..7) {
                binding.weeklyRow2.setWeeklyItemVisibility(i, View.GONE)
            }
        }


        for (i in 0 until midForecastModelSparseArray.size()) {
            //Log.d(TAG, dateTime.plusDays(i+3).toString()+">>"+midForecastModelSparseArray.keyAt(i)+","+midForecastModelSparseArray.get(midForecastModelSparseArray.keyAt(i)));
            //CLogger.d(dateTime.plusDays(i).toString()+">>"+midForecastModelSparseArray.keyAt(i)+","+midForecastModelSparseArray.get(midForecastModelSparseArray.keyAt(i)));
            d(midForecastModelSparseArray.keyAt(i).toString() + "," + midForecastModelSparseArray[midForecastModelSparseArray.keyAt(i)])
        }
    }

    private fun setDataIntoRow(
            row: WeeklyItemLayout,
            model: MidForecastModel,
            i: Int) {
        row.setTemp(i, model.lowTemp, model.highTemp)
        if (model.daySky == 0) {
            row.setSkyImage(i, model.amSky, model.pmSky)
        } else {
            row.setSkyImage(i, model.daySky, model.daySky)
        }
    }

    private fun getShortForecastArray(nowWeatherModel: NowWeatherModel,
                                      dayForecastModels: List<DayForecastModel>): SparseArray<MidForecastModel> {
        val sparseArray = SparseArray<MidForecastModel>()
        sparseArray.put(1, getNowForecastModel(nowWeatherModel))
        for (dayForecastModel in dayForecastModels) {
            dayForecastModel.forecastLowTemp?.let {
                val idx = getIndexFromDate(dayForecastModel.forecastDate)
                val model = sparseArray[idx, MidForecastModel()]
                model.lowTemp = dayForecastModel.forecastLowTemp!!.toDouble().toInt().toString()
                model.amRainPercentage = dayForecastModel.forecastRainPercentage!!
                model.amSky = dayForecastModel.forecastSkyDrawableId
                model.baseDate = dayForecastModel.forecastDate
                sparseArray.put(idx, model)
            }

            dayForecastModel.forecastHighTemp?.let {
                val idx = getIndexFromDate(dayForecastModel.forecastDate)
                val model = sparseArray[idx, MidForecastModel()]
                model.highTemp = dayForecastModel.forecastHighTemp!!.toDouble().toInt().toString()
                model.pmRainPercentage = dayForecastModel.forecastRainPercentage!!
                model.pmSky = dayForecastModel.forecastSkyDrawableId
                sparseArray.put(idx, model)
            }
        }
        return sparseArray
    }

    private fun getIndexFromDate(date: String): Int {
        val dateTime = DateTime.now().toDateTime(DateTimeZone.forID("Asia/Seoul"))
        for (i in 0..2) {
            if (dateTime.plusDays(i).toString("yyyyMMdd") == date) {
                return i + 1
            }
        }
        return 0
    }

    private fun getNowForecastModel(nowWeatherModel: NowWeatherModel): MidForecastModel {
        return MidForecastModel(
                lowTemp = nowWeatherModel.lowTemp.replace("\u2103", ""),
                highTemp = nowWeatherModel.highTemp.replace("\u2103", ""),
                daySky = nowWeatherModel.nowSkyDrawableId!!,
                baseDate = getDateString(0, nowWeatherModel.baseDate + nowWeatherModel.baseTime),
        )
    }

    private var failCnt = 0
    private var pointModel: WeatherPoint? = null
    private fun midWeatherDataFailProcess(failRestResponse: FailRestResponse) {
        d(failRestResponse.code.toString() + ">>" + failRestResponse.failMsg)
        if (failCnt < 5) {
            failCnt++
            midWeatherViewModel.updateMidWeather(pointModel!!.midWeatherCode, pointModel!!.midTempCode)
        } else {
            failCnt = 0
            (mActivity as MainActivity?)!!.setUpdateFailUi()
        }
    }

    private fun updateWeather(point: WeatherPoint) {
        pointModel = point
        (mActivity as MainActivity).setLoading(true)
        midWeatherViewModel.updateMidWeather(point.midWeatherCode, point.midTempCode)
        weatherViewModel.updateNowWeather(point.x, point.y)
        dayForecastViewModel.updateShortForecastData(point.x, point.y)
    }
}