package com.jun.weather.repository.db;

import android.content.Context;
import android.content.res.Resources;

import com.jun.weather.R;
import com.jun.weather.repository.db.entity.ShortWeatherPoint;
import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FileRepository {
    private static FileRepository instance;
    public static FileRepository getInstance() {
        if(instance == null) {
            instance = new FileRepository();
        }
        return instance;
    }

    private Resources resources;
    public void init(Context context) {
        resources = context.getResources();
    }

    private List<ShortWeatherPoint> shortWeatherPointList;
    public List<ShortWeatherPoint> getShortWeatherPoint() {
        if(shortWeatherPointList != null) {
            return shortWeatherPointList;
        }

        shortWeatherPointList = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(resources.openRawResource(R.raw.code_short_weather)));

            CSVReader read = new CSVReader(br);
            String[] record = read.readNext();
            while ((record = read.readNext()) != null) {
                //Log.d(TAG, Arrays.toString(record));
                ShortWeatherPoint point = new ShortWeatherPoint();
                point.deg_1 = record[0];
                point.deg_2 = record[1];
                point.deg_3 = record[2];
                point.x = Integer.parseInt(record[3]);
                point.y = Integer.parseInt(record[4]);
                point.mid_temp_code = record[5];
                point.mid_weather_code = record[6];
                shortWeatherPointList.add(point);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return shortWeatherPointList;
    }
}
