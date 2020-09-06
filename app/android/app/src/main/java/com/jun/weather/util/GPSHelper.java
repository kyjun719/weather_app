package com.jun.weather.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.location.Location;

import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.jun.weather.repository.web.entity.Enum;

public class GPSHelper {
    private static GPSHelper instance;
    private static String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
    private static boolean isInitialized = false;
    public static int REQUEST_GPS_PERMISSION = 1001;
    public static int REQUEST_CHECK_GPS_SETTINGS = 1002;
    private LocationResultListener locationResultListener;
    private FusedLocationProviderClient providerClient;

    public static GPSHelper getInstance() {
        if(instance == null) {
            instance = new GPSHelper();
        }
        return instance;
    }

    private GPSHelper() {}

    public void init(LocationResultListener listener) {
        isInitialized = true;
        locationResultListener = listener;
    }

    private boolean checkNotInit() {
        if(!isInitialized) {
            locationResultListener.onFail(Enum.ResponseCode.EXCEPTION_ERROR, "GPSHelper not initialized");
            return true;
        }

        return false;
    }

    private boolean canUseGPS(Context context) {
        if(checkNotInit()) {
            return false;
        }

        return CommonUtils.permissionGranted(context, permissions);
    }

    private void requestGPSPermission(Context context) {
        if(checkNotInit()) {
            return;
        }

        CommonUtils.requestPermission((Activity) context, permissions, REQUEST_GPS_PERMISSION);
    }

    public void notifyPermissionResult(Context context, boolean granted) {
        if(granted) {
            getNowLocation(context);
        } else {
            locationResultListener.onFail(Enum.ResponseCode.PERMISSION_NOT_GRANTED, "현재위치를 확인하기 위해서 권한을 허용해주시기 바랍니다.");
        }
    }

    public void getNowLocation(Context context) {
        if(checkNotInit()) {
            return;
        }

        if(!canUseGPS(context)) {
            requestGPSPermission(context);
            return;
        }

        providerClient = LocationServices.getFusedLocationProviderClient(context);

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient settingsClient = LocationServices.getSettingsClient(context);
        Task<LocationSettingsResponse> locationSettingsResponseTask =
                settingsClient.checkLocationSettings(builder.build());
        locationSettingsResponseTask.addOnSuccessListener(locationSettingsResponse -> requestLocationUpdate(context, locationRequest));

        locationSettingsResponseTask.addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult((Activity) context,
                            REQUEST_CHECK_GPS_SETTINGS);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            } else {
                e.printStackTrace();
            }
        });

    }

    private void requestLocationUpdate(Context context, LocationRequest locationRequest) {
        for(String permission : permissions) {
            ContextCompat.checkSelfPermission(context, permission);
        }

        providerClient.requestLocationUpdates(locationRequest, locationCallback, null);

        providerClient.getLastLocation().addOnCompleteListener((Activity) context, task -> {
            if(task.isCanceled()) {
                locationResultListener.onCanceled(Enum.ResponseCode.EXCEPTION_ERROR, "취소하였습니다.");
            } else if(!task.isSuccessful()) {
                locationResultListener.onFail(Enum.ResponseCode.EXCEPTION_ERROR,
                        task.getException()==null?"에러발생":task.getException().getLocalizedMessage());
            }
        });
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if(locationResult == null) {
                return;
            }

            float accuracy = -1;
            Location bestLocation = null;
            for(Location location : locationResult.getLocations()) {
                CLogger.d(location.getLatitude()+","+location.getLongitude());
                if(accuracy < location.getAccuracy()) {
                    bestLocation = location;
                }
            }

            if(bestLocation != null) {
                locationResultListener.onSuccess(bestLocation);
            }
        }
    };

    public void stopGPSLocationUpdate() {
        if(providerClient != null) {
            providerClient.removeLocationUpdates(locationCallback);
        }
    }

    public void checkGPSSettingResult(Context context, int resultCode) {
        if(resultCode == Activity.RESULT_OK) {
            getNowLocation(context);
        } else if(resultCode == Activity.RESULT_CANCELED) {
            locationResultListener.onCanceled(Enum.ResponseCode.SETTING_NOT_GRANTED, "설정에서 GPS를 켜주시기 바랍니다.");
        } else {
            locationResultListener.onFail(Enum.ResponseCode.EXCEPTION_ERROR, "에러발생");
        }
    }

    public interface LocationResultListener {
        void onSuccess(Location location);
        void onCanceled(Enum.ResponseCode responseCode, String msg);
        void onFail(Enum.ResponseCode responseCode, String msg);
    }
}
