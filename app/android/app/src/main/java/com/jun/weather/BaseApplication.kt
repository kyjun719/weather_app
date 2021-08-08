package com.jun.weather

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.jun.weather.repository.AppRepository
import com.jun.weather.repository.AppRepository.Companion.getInstance
import com.jun.weather.util.CLogger.Companion.init
import com.jun.weather.util.PreferenceUtils.Companion.getInstance

class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setDebug()
        init()
        getInstance().init(baseContext)
    }

    val repository = getInstance(baseContext)

    private fun setDebug() {
        val pm = baseContext.packageManager
        try {
            //패키지명으로 ApplicationInfo 검색
            val appInfo = pm.getApplicationInfo(baseContext.packageName, 0)
            //ApplicationInfo 의 플래그중 ApplicationInfo.FLAG_DEBUGGABLE set 여부
            isDebug = 0 != appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    companion object {
        var isDebug = false
            private set
    }
}