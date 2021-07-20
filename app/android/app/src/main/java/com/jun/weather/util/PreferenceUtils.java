package com.jun.weather.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.jun.weather.ui.entity.WeatherPointModel;

import java.util.ArrayList;
import java.util.List;

public class PreferenceUtils {
    private static PreferenceUtils instance;
    public static PreferenceUtils getInstance() {
        if(instance == null) {
            instance = new PreferenceUtils();
        }
        return instance;
    }

    private PreferenceUtils() {}

    private SharedPreferences sharedPreferences;
    public void init(Context context) {
        sharedPreferences = context.getSharedPreferences("weatherApp", Context.MODE_PRIVATE);
    }

    //맨 나중 선택한 항목이 왼쪽
    private static int LOC_POINT_NUM=3;
    public void addPoint(WeatherPointModel.WeatherPoint point) {
        int duplicatedIdx = removeDuplicateItem(point);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        String address = point.deg_1+"/"+point.deg_2+"/"+point.deg_3;
        String pointStr = point.x+"/"+point.y+"/"+point.midTempCode+"/"+point.midWeatherCode;
        /*
        새로운 항목이 추가된 경우 : 모든 항목을 뒤로 이동
        첫번째 항목을 누른 경우 : 변경없음
        두번째 항목을 누른 경우 : 첫번째 항목과 두번째 항목을 바꿈
        세번째 항목을 누른 경우 : 모든 항목 뒤로 이동
         */
        int startIdx = duplicatedIdx>=0?duplicatedIdx:LOC_POINT_NUM;
        for(int i = startIdx; i > 1; i--) {
            if(hasPointValue(i-1)) {
                WeatherPointModel.WeatherPoint tmp = getPoint(i-1);
                editor.putString(""+i, tmp.deg_1+"/"+tmp.deg_2+"/"+tmp.deg_3);
                editor.putString(""+(i+LOC_POINT_NUM), tmp.x+"/"+tmp.y+"/"+tmp.midTempCode+"/"+tmp.midWeatherCode);
            }
        }

        editor.putString(""+1, address);
        editor.putString(""+(1+LOC_POINT_NUM), pointStr);
        editor.apply();
    }

    private int removeDuplicateItem(WeatherPointModel.WeatherPoint point) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for(int i = 1; i <= LOC_POINT_NUM; i++) {
            if(hasPointValue(i) && point.toString().equals(getPoint(i).toString())) {
                editor.remove(""+i);
                editor.remove(""+(i+LOC_POINT_NUM));
                editor.apply();
                return i;
            }
        }

        return -1;
    }


    public List<WeatherPointModel.WeatherPoint> getPointList() {
        List<WeatherPointModel.WeatherPoint> list = new ArrayList<>();
        for(int i = 1; i <= LOC_POINT_NUM; i++) {
            if(hasPointValue(i)) {
                list.add(getPoint(i));
            } else {
                break;
            }
        }

        return list;
    }

    private boolean hasPointValue(int idx) {
        return sharedPreferences.getString(""+idx, null) != null;
    }

    private WeatherPointModel.WeatherPoint getPoint(int idx) {
        WeatherPointModel.WeatherPoint point = new WeatherPointModel.WeatherPoint();
        String address = sharedPreferences.getString(""+idx, null);
        if(address != null) {
            String[] reg = address.split("/");
            point.deg_1 = reg[0];
            if(reg.length > 1) {
                point.deg_2 = reg[1];
            }
            if(reg.length > 2) {
                point.deg_3 = reg[2];
            }
        }

        String pointStr = sharedPreferences.getString(""+(idx+LOC_POINT_NUM), null);
        if(pointStr != null) {
            String[] tmp = pointStr.split("/");
            point.x = Integer.parseInt(tmp[0]);
            point.y = Integer.parseInt(tmp[1]);
            point.midTempCode = tmp[2];
            point.midWeatherCode = tmp[3];
        }

        return point;
    }
}
