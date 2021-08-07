package com.jun.weather.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jun.weather.R
import com.jun.weather.ui.enums.MidLandSky
import com.jun.weather.ui.enums.MidLandSky.Companion.getEnumValByStringVal
import com.jun.weather.ui.enums.PtyState
import com.jun.weather.ui.enums.SkyState
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat

object CommonUtils {
    fun permissionGranted(context: Context?, permission: String?): Boolean {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context!!, permission!!)
    }

    fun permissionGranted(context: Context?, permissions: Array<String?>): Boolean {
        for (permission in permissions) {
            if (!permissionGranted(context, permission)) {
                return false
            }
        }
        return true
    }

    fun requestPermission(activity: Activity?, permission: String, resultCode: Int) {
        ActivityCompat.requestPermissions(activity!!, arrayOf(permission), resultCode)
    }

    fun requestPermission(activity: Activity?, permissions: Array<String?>?, resultCode: Int) {
        ActivityCompat.requestPermissions(activity!!, permissions!!, resultCode)
    }

    fun notifyPermissionRequestResult(context: Context, requestCode: Int, grantResults: IntArray) {
        if (requestCode == GPSHelper.REQUEST_GPS_PERMISSION) {
            GPSHelper.instance!!.notifyPermissionResult(context,
                    grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        }
    }

    fun getSkyIcon(sky: Int, pty: Int): Int {
        val nowHour = DateTime.now().toDateTime(DateTimeZone.forID("Asia/Seoul")).hourOfDay
        val isNight = nowHour >= 18 || nowHour <= 6
        var drawableId = -1
        if (pty == 0 || pty > 4) {
            when (sky) {
                1 -> drawableId = if (isNight) R.drawable.ic_sky_night else R.drawable.ic_sky_day
                3 -> drawableId = if (isNight) R.drawable.ic_sky_night_cloud else R.drawable.ic_sky_day_cloud
                4 -> drawableId = R.drawable.ic_sky_clouds
            }
        } else {
            //없음(0), 비(1), 비/눈(2), 눈(3), 소나기(4)
            when (pty) {
                1 -> drawableId = R.drawable.ic_sky_rain
                2 -> drawableId = R.drawable.ic_sky_snow_rain
                3 -> drawableId = R.drawable.ic_sky_snows
                4 -> drawableId = R.drawable.ic_sky_shower
            }
        }
        return drawableId
    }

    fun getSkyIcon(sky: String?): Int {
        if (sky == null) {
            return 0
        }
        val ret = getEnumValByStringVal(sky) ?: return 0
        return when (ret) {
            MidLandSky.SUNNY -> getSkyIcon(SkyState.SUNNY.value, PtyState.NONE.value)
            MidLandSky.CLOUDY -> getSkyIcon(SkyState.CLOUDY.value, PtyState.NONE.value)
            MidLandSky.DARK -> getSkyIcon(SkyState.DARK.value, PtyState.NONE.value)
            MidLandSky.CLOUDY_RAIN, MidLandSky.DARK_RAIN -> getSkyIcon(SkyState.DARK.value, PtyState.RAINY.value)
            MidLandSky.CLOUDY_RAIN_SNOW, MidLandSky.CLOUDY_SNOW_RAIN, MidLandSky.DARK_RAIN_SNOW, MidLandSky.DARK_SNOW_RAIN -> getSkyIcon(SkyState.DARK.value, PtyState.MIXED_SNOW.value)
            MidLandSky.CLOUDY_SNOW, MidLandSky.DARK_SNOW -> getSkyIcon(SkyState.DARK.value, PtyState.SNOW.value)
        }
    }

    fun getDateString(plusDay: Int, baseDate: String?): String {
        return DateTimeFormat.forPattern("yyyyMMddHHmm").parseDateTime(baseDate).plusDays(plusDay).toString("yyyyMMdd")
    }
}