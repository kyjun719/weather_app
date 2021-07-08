package com.jun.weather.repository.db

import android.content.res.Resources
import com.jun.weather.R
import com.opencsv.CSVReader
import java.io.BufferedReader
import java.io.InputStreamReader

class FileRepository {
    data class ShortWeatherPoint(val deg1: String, val deg2: String, val deg3: String, val x: Int, val y: Int, val midTempCode: String, val midWeatherCode: String)

    private var shortWeatherPointList: MutableList<ShortWeatherPoint> = mutableListOf()
    fun getShortWeatherPoint(resources: Resources): List<ShortWeatherPoint> {
        if (shortWeatherPointList.isNotEmpty()) {
            return shortWeatherPointList
        }
        shortWeatherPointList = mutableListOf()
        try {
            val br = BufferedReader(InputStreamReader(resources.openRawResource(R.raw.code_short_weather)))
            val read = CSVReader(br)
            read.readNext()

            read.readAll().forEach {
                shortWeatherPointList.add(
                        ShortWeatherPoint(it[0], it[1], it[2], it[3].toInt(), it[4].toInt(), it[5], it[6])
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return shortWeatherPointList
    }
}