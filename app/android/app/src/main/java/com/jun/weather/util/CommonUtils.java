package com.jun.weather.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.jun.weather.R;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

public class CommonUtils {
    public static boolean permissionGranted(Context context, String permission) {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, permission);
    }

    public static boolean permissionGranted(Context context, String[] permissions) {
        for(String permission : permissions) {
            if(!permissionGranted(context, permission)) {
                return false;
            }
        }
        return true;
    }

    public static void requestPermission(Activity activity, String permission, int resultCode) {
        ActivityCompat.requestPermissions(activity, new String[]{permission}, resultCode);
    }

    public static void requestPermission(Activity activity, String[] permissions, int resultCode) {
        ActivityCompat.requestPermissions(activity, permissions, resultCode);
    }

    public static void notifyPermissionRequestResult(Context context, int requestCode, int[] grantResults) {
        if(requestCode == GPSHelper.REQUEST_GPS_PERMISSION) {
            GPSHelper.getInstance().notifyPermissionResult(context,
                    grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
        }
    }

    public static int getSkyIcon(int sky, int pty) {
        int nowHour = DateTime.now().toDateTime(DateTimeZone.forID("Asia/Seoul")).getHourOfDay();
        boolean isNight = (nowHour >= 18) || (nowHour <= 6);
        int drawableId = -1;

        if(pty == 0 || pty > 4) {
            switch (sky) {
                case 1:
                    drawableId = isNight? R.drawable.ic_sky_night:R.drawable.ic_sky_day;
                    break;
                case 3:
                    drawableId = isNight?R.drawable.ic_sky_night_cloud:R.drawable.ic_sky_day_cloud;
                    break;
                case 4:
                    drawableId = R.drawable.ic_sky_clouds;
                    break;
            }
        } else {
            //없음(0), 비(1), 비/눈(2), 눈(3), 소나기(4)
            switch (pty) {
                case 1:
                    drawableId = R.drawable.ic_sky_rain;
                    break;
                case 2:
                    drawableId = R.drawable.ic_sky_snow_rain;
                    break;
                case 3:
                    drawableId = R.drawable.ic_sky_snows;
                    break;
                case 4:
                    drawableId = R.drawable.ic_sky_shower;
                    break;
            }
        }

        return drawableId;
    }

    public static int getSkyIcon(String sky) {
        if(sky == null) {
            return 0;
        }
        Enum.MID_LAND_SKY ret =
                Enum.MID_LAND_SKY.getEnumValByStringVal(sky);
        if(ret == null) {
            return 0;
        }

        switch (ret) {
            case SUNNY:
                return getSkyIcon(Enum.SKY.SUNNY.getVal(), Enum.PTY.NONE.getVal());
            case CLOUDY:
                return getSkyIcon(Enum.SKY.CLOUDY.getVal(), Enum.PTY.NONE.getVal());
            case DARK:
                return getSkyIcon(Enum.SKY.DARK.getVal(),Enum.PTY.NONE.getVal());
            case CLOUDY_RAIN:
            case DARK_RAIN:
                return getSkyIcon(Enum.SKY.DARK.getVal(), Enum.PTY.RAINY.getVal());
            case CLOUDY_RAIN_SNOW:
            case CLOUDY_SNOW_RAIN:
            case DARK_RAIN_SNOW:
            case DARK_SNOW_RAIN:
                return getSkyIcon(Enum.SKY.DARK.getVal(), Enum.PTY.MIXED_SNOW.getVal());
            case CLOUDY_SNOW:
            case DARK_SNOW:
                return getSkyIcon(Enum.SKY.DARK.getVal(), Enum.PTY.SNOW.getVal());
        }

        return 0;
    }

    public static String getDateString(int plusDay, String baseDate) {
        return DateTimeFormat.forPattern("yyyyMMddHHmm").parseDateTime(baseDate).plusDays(plusDay).toString("yyyyMMdd");
    }
}
