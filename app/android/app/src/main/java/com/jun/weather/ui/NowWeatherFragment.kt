package com.jun.weather.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.jun.weather.BaseApplication
import com.jun.weather.R
import com.jun.weather.databinding.FragmentDayWeatherBinding
import com.jun.weather.ui.entity.DayForecastModel
import com.jun.weather.ui.entity.FailRestResponse
import com.jun.weather.ui.entity.NowWeatherModel
import com.jun.weather.ui.entity.WeatherPoint
import com.jun.weather.util.CLogger
import com.jun.weather.viewmodel.CustomViewModelProvider
import com.jun.weather.viewmodel.DayForecastViewModel
import com.jun.weather.viewmodel.NowWeatherViewModel
import com.jun.weather.viewmodel.WeatherPointViewModel
import java.lang.reflect.Field
import java.util.*

class NowWeatherFragment : Fragment() {
    private lateinit var weatherViewModel: NowWeatherViewModel
    private lateinit var dayForecastViewModel: DayForecastViewModel
    private lateinit var weatherPointViewModel: WeatherPointViewModel
    private lateinit var binding: FragmentDayWeatherBinding
    private var pointModel: WeatherPoint? = null

    internal enum class SelectedItem {
        EMPTY, TEMP, HUMIDITY, RAIN
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_day_weather, container, false)

        val application = requireActivity().application as BaseApplication
        weatherViewModel = CustomViewModelProvider(application.repository)
                .getViewModel((requireActivity() as AppCompatActivity), NowWeatherViewModel::class.java)
        dayForecastViewModel = CustomViewModelProvider(application.repository)
                .getViewModel((requireActivity() as AppCompatActivity), DayForecastViewModel::class.java)
        weatherPointViewModel = CustomViewModelProvider(application.repository)
                .getViewModel((requireActivity() as AppCompatActivity), WeatherPointViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initObserver()
        changeGraphData(SelectedItem.TEMP)
    }

    private fun initView() {
        binding.lineChart.setNoDataText(resources.getString(R.string.no_location_selected))
        binding.btnRain.tag = SelectedItem.RAIN
        binding.btnRain.setOnClickListener { v: View -> changeGraphData(v.tag) }
        binding.btnHumidity.tag = SelectedItem.HUMIDITY
        binding.btnHumidity.setOnClickListener { v: View -> changeGraphData(v.tag) }
        binding.btnTemp.tag = SelectedItem.TEMP
        binding.btnTemp.setOnClickListener { v: View -> changeGraphData(v.tag) }
    }

    private fun initObserver() {
        weatherViewModel.data.observe(viewLifecycleOwner, { nowWeatherModel ->
            nowWeatherModel?.let {
                (requireActivity() as MainActivity).setLoading(false)
                binding.nowWeather = nowWeatherModel
            }
        })

        dayForecastViewModel.data.observe(viewLifecycleOwner, { dayForecastModels ->
            updateDayForecastData(dayForecastModels) }
        )

        dayForecastViewModel.updateFailData.observe(viewLifecycleOwner, { failRestResponse ->
            dayForecastDateFailProcess(failRestResponse) }
        )
        weatherViewModel.updateFailData.observe(viewLifecycleOwner, { failRestResponse ->
            nowWeatherDataFailProcess(failRestResponse) }
        )

        weatherViewModel.isLoading.observe(viewLifecycleOwner, { aBoolean ->
            aBoolean?.let {
                (requireActivity() as MainActivity).setLoading(it)
            }
        })
        dayForecastViewModel.isLoading.observe(viewLifecycleOwner, { aBoolean ->
            aBoolean?.let {
                (requireActivity() as MainActivity).setLoading(it)
            }
        })
        weatherPointViewModel.selectedPoint.observe(viewLifecycleOwner, {
            it?.let {
                updateWeather(it)
            }
        })
    }

    private var selectedItem = SelectedItem.EMPTY
    private fun changeGraphData(tag: Any) {
        if (tag !is SelectedItem || tag == selectedItem) {
            return
        }
        when (selectedItem) {
            SelectedItem.RAIN -> binding.btnRain.setImageResource(R.drawable.ic_rain_normal)
            SelectedItem.TEMP -> binding.btnTemp.setImageResource(R.drawable.ic_temperature_normal)
            SelectedItem.HUMIDITY -> binding.btnHumidity.setImageResource(R.drawable.ic_thermometer_normal)
            SelectedItem.EMPTY -> {}
        }
        selectedItem = tag
        when (selectedItem) {
            SelectedItem.RAIN -> binding.btnRain.setImageResource(R.drawable.ic_rain_accent)
            SelectedItem.TEMP -> binding.btnTemp.setImageResource(R.drawable.ic_temperature_accent)
            SelectedItem.HUMIDITY -> binding.btnHumidity.setImageResource(R.drawable.ic_thermometer_accent)
            SelectedItem.EMPTY -> {}
        }

        dayForecastViewModel.data.value?.let {
            updateDayForecastData(it)
        }

    }

    private fun updateWeather(point: WeatherPoint) {
        pointModel = point
        (requireActivity() as MainActivity).setLoading(true)
        weatherViewModel.updateNowWeather(point.x, point.y)
        dayForecastViewModel.updateShortForecastData(point.x, point.y)
    }

    private var failCnt = 0
    private fun nowWeatherDataFailProcess(failRestResponse: FailRestResponse) {
        //TODO retry in this?
        CLogger.d(failRestResponse.code.toString() + ">>" + failRestResponse.failMsg)
        if (failCnt < 5) {
            failCnt++
            weatherViewModel.updateNowWeather(pointModel!!.x, pointModel!!.y)
        } else {
            failCnt = 0
            (requireActivity() as MainActivity).setUpdateFailUi()
        }
    }

    private fun dayForecastDateFailProcess(failRestResponse: FailRestResponse) {
        CLogger.d(failRestResponse.code.toString() + ">>" + failRestResponse.failMsg)
        if (failCnt < 5) {
            failCnt++
            dayForecastViewModel.updateShortForecastData(pointModel!!.x, pointModel!!.y)
        } else {
            failCnt = 0
            (requireActivity() as MainActivity).setUpdateFailUi()
        }
    }

    private val rainStrValArr = arrayOf("없음", "1mm미만", "1~4mm", "5~9mm", "10~19mm", "20~39mm", "40~69mm", "70mm 이상")
    private val rainValArr = floatArrayOf(0f, 1f, 4f, 9f, 19f, 39f, 60f, 70f)
    private fun rainStringToValue(str: String): Float {
        return if(str == "강수없음") return 0f else str.substring(0, str.indexOf("mm")).toFloat()
    }

    private fun rainValueToString(value: Float): String {
        return value.toString()+"mm"
    }

    private fun updateDayForecastData(dayForecastModels: List<DayForecastModel>) {
        //TODO this method move to viewModel
        if (!isVisible) {
            return
        }
        val values = ArrayList<Entry>()
        val dateList = ArrayList<String>()
        val dayIndexMap = HashMap<String, Int?>()
        val tmp = ArrayList<BarEntry>()
        try {
            var befRainVal = 0f
            dayForecastModels.forEachIndexed { i, dayForecastModel ->
                dateList.add(dayForecastModel.hourString)
                if (dayIndexMap[dayForecastModel.dayString] == null) {
                    dayIndexMap[dayForecastModel.dayString] = i
                }
                CLogger.d(dayForecastModel.toString())

                val entry = Entry()
                entry.x = i.toFloat()
                when (selectedItem) {
                    SelectedItem.TEMP -> entry.y = dayForecastModel.forecastTemp?.toFloat() ?: 0f
                    SelectedItem.HUMIDITY -> entry.y = dayForecastModel.forecastHumidity?.toFloat() ?: 0f
                    SelectedItem.RAIN -> {
                        entry.y = dayForecastModel.forecastRainPercentage?.toFloat() ?: 0f
                        if (dayForecastModel.forecastRain != null) {
                            val tmpVal = rainStringToValue(dayForecastModel.forecastRain!!)
                            tmp.add(BarEntry(i.toFloat(), tmpVal))
                            befRainVal = tmpVal
                        } else {
                            tmp.add(BarEntry(i.toFloat(), befRainVal))
                        }
                    }
                }
                values.add(entry)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        CLogger.d(values)
        val set1 = LineDataSet(values, "DataSet 1")
        set1.lineWidth = 2f
        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(set1)
        // add the data sets
        // create a data object with the data sets
        val lineData = LineData(dataSets)
        lineData.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                var `val` = String.format(Locale.getDefault(), "%.0f", value)
                `val` += if (selectedItem == SelectedItem.TEMP) {
                    "\u2103"
                } else {
                    "%"
                }
                return `val`
            }
        })
        lineData.setValueTextSize(18f)
        val combinedChart = binding.lineChart
        combinedChart.clear()
        combinedChart.data = null
        val combinedData = CombinedData()
        combinedData.setData(lineData)
        if (selectedItem == SelectedItem.RAIN) {
            val barDataSet = BarDataSet(tmp, "bar")
            barDataSet.color = Color.GRAY
            barDataSet.valueTextSize = 10f
            barDataSet.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val tmp = rainValueToString(value)
                    return if (tmp == rainStrValArr[0]) "" else tmp
                }
            }
            combinedData.setData(BarData(barDataSet))
        }
        combinedChart.data = combinedData
        combinedChart.xAxis.isGranularityEnabled = true
        combinedChart.xAxis.granularity = 1f
        combinedChart.xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
        combinedChart.xAxis.textSize = 18f
        combinedChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axisBase: AxisBase): String {
                return dateList[value.toInt()]
            }
        }

        //add limitLine to separate day
        for (day in dayIndexMap.keys) {
            val `val` = dayIndexMap[day] ?: continue
            val limitLine = LimitLine(`val`.toFloat(), day)
            limitLine.textSize = 18f
            limitLine.lineColor = Color.RED
            limitLine.lineWidth = 2f
            combinedChart.xAxis.addLimitLine(limitLine)
        }
        combinedChart.axisLeft.setDrawGridLines(false)
        combinedChart.axisLeft.setDrawLabels(false)
        combinedChart.axisRight.setDrawGridLines(false)
        combinedChart.axisRight.setDrawLabels(false)
        combinedChart.legend.isEnabled = false
        combinedChart.description = null
        combinedChart.isDoubleTapToZoomEnabled = false
        combinedChart.setVisibleXRangeMaximum(5f)
        combinedChart.invalidate()
    }
}