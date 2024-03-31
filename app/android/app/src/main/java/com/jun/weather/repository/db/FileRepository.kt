package com.jun.weather.repository.db

import android.content.res.Resources
import com.jun.weather.R
import com.jun.weather.util.CLogger
import com.opencsv.CSVReader
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

class FileRepository {
    data class ShortWeatherPoint(
            val idx: Int,
            val deg1: String,
            val deg2: String,
            val deg3: String,
            val x: Int,
            val y: Int,
            val midTempCode: String,
            val midWeatherCode: String
            )

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
                        ShortWeatherPoint(
                                it[0].toInt(), it[1], it[2], it[3], it[4].toInt(), it[5].toInt(), it[6], it[7])
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return shortWeatherPointList.sortedBy { it.idx }
    }
}