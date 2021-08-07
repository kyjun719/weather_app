package com.jun.weather.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.jun.weather.repository.web.enums.Enums.ResponseCode
import com.jun.weather.util.CommonUtils.permissionGranted
import com.jun.weather.util.CommonUtils.requestPermission

class GPSHelper private constructor() {
    private var locationResultListener: LocationResultListener? = null
    private var providerClient: FusedLocationProviderClient? = null

    interface LocationResultListener {
        fun onSuccess(location: Location)
        fun onCanceled(responseCode: ResponseCode, msg: String)
        fun onFail(responseCode: ResponseCode, msg: String)
    }

    fun init(listener: LocationResultListener?) {
        isInitialized = true
        locationResultListener = listener
    }

    private fun checkNotInit(): Boolean {
        if (!isInitialized) {
            locationResultListener!!.onFail(ResponseCode.EXCEPTION_ERROR, "GPSHelper not initialized")
            return true
        }
        return false
    }

    private fun canUseGPS(context: Context): Boolean {
        return if (checkNotInit()) {
            false
        } else permissionGranted(context, permissions)
    }

    private fun requestGPSPermission(context: Context) {
        if (checkNotInit()) {
            return
        }
        requestPermission(context as Activity, permissions, REQUEST_GPS_PERMISSION)
    }

    fun notifyPermissionResult(context: Context, granted: Boolean) {
        if (granted) {
            getNowLocation(context)
        } else {
            locationResultListener!!.onFail(ResponseCode.PERMISSION_NOT_GRANTED, "현재위치를 확인하기 위해서 권한을 허용해주시기 바랍니다.")
        }
    }

    fun getNowLocation(context: Context) {
        if (checkNotInit()) {
            return
        }
        if (!canUseGPS(context)) {
            requestGPSPermission(context)
            return
        }
        providerClient = LocationServices.getFusedLocationProviderClient(context)
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            //numUpdates = 1
            fastestInterval = 0
        }
        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(context)
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnSuccessListener { locationSettingsResponse: LocationSettingsResponse? ->
            requestLocationUpdate(context, locationRequest) }
        locationSettingsResponseTask.addOnFailureListener { e: Exception ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(context as Activity,
                            REQUEST_CHECK_GPS_SETTINGS)
                } catch (e1: Exception) {
                    e1.printStackTrace()
                }
            } else {
                e.printStackTrace()
            }
        }
    }

    private fun requestLocationUpdate(context: Context?, locationRequest: LocationRequest) {
        for (permission in permissions) {
            ContextCompat.checkSelfPermission(context!!, permission!!)
        }
        providerClient!!.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        providerClient!!.lastLocation.addOnCompleteListener(context as Activity) { task: Task<Location?> ->
            if (task.isCanceled) {
                locationResultListener!!.onCanceled(ResponseCode.EXCEPTION_ERROR, "취소하였습니다.")
            } else if (!task.isSuccessful) {
                locationResultListener!!.onFail(ResponseCode.EXCEPTION_ERROR,
                        if (task.exception == null) "에러발생" else task.exception!!.localizedMessage)
            }
        }
    }

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val accuracy = -1f
            var bestLocation: Location? = null
            for (location in locationResult.locations) {
                CLogger.d(location.latitude.toString() + "," + location.longitude)
                if (accuracy < location.accuracy) {
                    bestLocation = location
                }
            }
            if (bestLocation != null) {
                locationResultListener!!.onSuccess(bestLocation)
            }
        }
    }

    fun stopGPSLocationUpdate() {
        if (providerClient != null) {
            providerClient!!.removeLocationUpdates(locationCallback)
        }
    }

    fun checkGPSSettingResult(context: Context, resultCode: Int) {
        if (resultCode == Activity.RESULT_OK) {
            getNowLocation(context)
        } else if (resultCode == Activity.RESULT_CANCELED) {
            locationResultListener!!.onCanceled(ResponseCode.SETTING_NOT_GRANTED, "설정에서 GPS를 켜주시기 바랍니다.")
        } else {
            locationResultListener!!.onFail(ResponseCode.EXCEPTION_ERROR, "에러발생")
        }
    }

    companion object {
        var instance: GPSHelper? = null
            get() {
                if (field == null) {
                    field = GPSHelper()
                }
                return field
            }
            private set
        private val permissions = arrayOf<String?>(Manifest.permission.ACCESS_FINE_LOCATION)
        private var isInitialized = false
        var REQUEST_GPS_PERMISSION = 1001
        var REQUEST_CHECK_GPS_SETTINGS = 1002
    }
}