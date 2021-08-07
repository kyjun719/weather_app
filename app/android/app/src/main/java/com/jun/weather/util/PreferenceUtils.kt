package com.jun.weather.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.jun.weather.ui.entity.WeatherPoint
import java.util.*

class PreferenceUtils private constructor() {
    private lateinit var sharedPreferences: SharedPreferences
    fun init(context: Context) {
        context.getSharedPreferences("weatherApp", Context.MODE_PRIVATE).edit().clear().apply()
        sharedPreferences = context.getSharedPreferences("weatherApp2", Context.MODE_PRIVATE)
    }

    fun addPoint(point: WeatherPoint) {
        val editor = sharedPreferences.edit()
        var startIdx = 0
        var endIdx = pointList.size
        if(pointList.contains(point)) {
            endIdx = pointList.indexOf(point)
        }

        for(i in startIdx until endIdx) {
            editor.putString("point${i+1}", Gson().toJson(pointList[i]))
        }
        editor.putString("point0", Gson().toJson(point))
        editor.commit()
    }

    val pointList: List<WeatherPoint>
        get() {
            val list: MutableList<WeatherPoint> = ArrayList()
            for (i in 0 until LOC_POINT_NUM) {
                if (hasPointValue(i)) {
                    list.add(getPoint(i))
                } else {
                    break
                }
            }
            return list
        }

    private fun hasPointValue(idx: Int): Boolean {
        return sharedPreferences.getString("point$idx", null) != null
    }

    private fun getPoint(idx: Int): WeatherPoint {
        return Gson().fromJson(sharedPreferences.getString("point$idx", ""), WeatherPoint::class.java)
    }

    companion object {
        private var instance: PreferenceUtils? = null

        //맨 나중 선택한 항목이 왼쪽
        private const val LOC_POINT_NUM = 3

        @JvmStatic fun getInstance(): PreferenceUtils {
            return instance ?: PreferenceUtils().also { instance = it }
        }
    }
}