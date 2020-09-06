package com.jun.weather.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.jun.weather.BaseApplication;
import com.jun.weather.R;
import com.jun.weather.databinding.FragmentDayWeatherBinding;
import com.jun.weather.repository.AppRepository;
import com.jun.weather.util.CLogger;
import com.jun.weather.viewmodel.CustomViewModelProvider;
import com.jun.weather.viewmodel.DayForecastViewModel;
import com.jun.weather.viewmodel.NowWeatherViewModel;
import com.jun.weather.viewmodel.entity.DayForecastModel;
import com.jun.weather.viewmodel.entity.FailRestResponse;
import com.jun.weather.viewmodel.entity.NowWeatherModel;
import com.jun.weather.viewmodel.entity.WeatherPointModel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class NowWeatherFragment extends Fragment {
    private NowWeatherViewModel weatherViewModel;
    private DayForecastViewModel dayForecastViewModel;
    private FragmentDayWeatherBinding mBinding;
    private WeatherPointModel.WeatherPoint pointModel;
    private Activity mActivity;
    private ImageButton btn_rain, btn_humidity, btn_temp;

    enum SelectedItem {
        EMPTY, TEMP, HUMIDITY, RAIN
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_day_weather, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((CombinedChart)view.findViewById(R.id.line_chart)).setNoDataText(getResources().getString(R.string.no_location_selected));
        btn_rain = view.findViewById(R.id.btn_rain);
        btn_rain.setTag(SelectedItem.RAIN);
        btn_rain.setOnClickListener((v) -> changeGraphData(v.getTag()));

        btn_humidity = view.findViewById(R.id.btn_humidity);
        btn_humidity.setTag(SelectedItem.HUMIDITY);
        btn_humidity.setOnClickListener((v) -> changeGraphData(v.getTag()));

        btn_temp = view.findViewById(R.id.btn_temp);
        btn_temp.setTag(SelectedItem.TEMP);
        btn_temp.setOnClickListener((v) -> changeGraphData(v.getTag()));

        changeGraphData(SelectedItem.TEMP);
    }

    private SelectedItem selectedItem = SelectedItem.EMPTY;
    private void changeGraphData(Object tag) {
        if(!(tag instanceof SelectedItem)) {
            return;
        }

        SelectedItem newSelected = (SelectedItem) tag;
        if(newSelected == selectedItem) {
            return;
        }

        switch (selectedItem) {
            case RAIN:
                btn_rain.setImageResource(R.drawable.ic_rain_normal);
                break;
            case TEMP:
                btn_temp.setImageResource(R.drawable.ic_temperature_normal);
                break;
            case HUMIDITY:
                btn_humidity.setImageResource(R.drawable.ic_thermometer_normal);
                break;
        }

        selectedItem = newSelected;
        switch (selectedItem) {
            case RAIN:
                btn_rain.setImageResource(R.drawable.ic_rain_accent);
                break;
            case TEMP:
                btn_temp.setImageResource(R.drawable.ic_temperature_accent);
                break;
            case HUMIDITY:
                btn_humidity.setImageResource(R.drawable.ic_thermometer_accent);
                break;
        }

        if(dayForecastViewModel != null && dayForecastViewModel.getData() != null) {
            updateDayForecastData(dayForecastViewModel.getData().getValue());
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FragmentActivity fragmentActivity = getActivity();
        if(fragmentActivity != null) {
            ((MainActivity) mActivity).addFragment(this);

            BaseApplication application = ((BaseApplication)mActivity.getApplication());
            AppRepository appRepository = application.getRepository();

            weatherViewModel = new CustomViewModelProvider(getViewModelStore()).getNowWeatherViewModel(application, appRepository);
            weatherViewModel.getData().observe(getViewLifecycleOwner(), nowWeatherModel -> updateNowWeatherData(nowWeatherModel));
            weatherViewModel.getUpdateFailData().observe(this, failRestResponse -> nowWeatherDataFailProcess(failRestResponse));

            dayForecastViewModel = new CustomViewModelProvider(getViewModelStore()).getDayForecastViewModel(application, appRepository);
            dayForecastViewModel.getData().observe(getViewLifecycleOwner(), dayForecastModels -> updateDayForecastData(dayForecastModels));
            dayForecastViewModel.getUpdateFailData().observe(this, failRestResponse -> dayForecastDateFailProcess(failRestResponse));
        }
    }

    public void updateWeather(WeatherPointModel.WeatherPoint point) {
        pointModel = point;
        ((MainActivity) mActivity).setLoading(true);
        weatherViewModel.updateNowWeather(point.x,point.y);
        dayForecastViewModel.updateShortForecastData(point.x, point.y);
    }

    private int failCnt = 0;
    private void nowWeatherDataFailProcess(FailRestResponse failRestResponse) {
        CLogger.d(failRestResponse.code + ">>" + failRestResponse.failMsg);
        if(failCnt < 5) {
            failCnt++;
            weatherViewModel.updateNowWeather(pointModel.x,pointModel.y);
        } else {
            failCnt = 0;
            ((MainActivity) mActivity).setUpdateFailUi();
        }
    }

    private void dayForecastDateFailProcess(FailRestResponse failRestResponse) {
        CLogger.d(failRestResponse.code+">>"+failRestResponse.failMsg);
        if(failCnt < 5) {
            failCnt++;
            dayForecastViewModel.updateShortForecastData(pointModel.x, pointModel.y);
        } else {
            failCnt = 0;
            ((MainActivity) mActivity).setUpdateFailUi();
        }
    }

    private void updateNowWeatherData(NowWeatherModel nowWeatherModel) {
        if(!isVisible()) {
            return;
        }

        ((MainActivity) mActivity).setLoading(false);
        mBinding.setNowWeather(nowWeatherModel);
        CLogger.d(nowWeatherModel.toString());
        Glide.with(this).load(nowWeatherModel.nowSkyDrawableId).into((ImageView) mActivity.findViewById(R.id.image_sky));
    }

    private String[] rainStrValArr = {"없음", "1mm미만", "1~4mm", "5~9mm", "10~19mm", "20~39mm", "40~69mm", "70mm 이상"};
    private float[] rainValArr = {0,1,4,9,19,39,60,70};
    private float rainStringToValue(String str) {
        for(int i = 0; i < rainStrValArr.length; i++) {
            if (rainStrValArr[i].equals(str)) {
                return rainValArr[i];
            }
        }
        return rainValArr[0];
    }

    private String rainValueToString(float value) {
        for(int i = 0; i < rainValArr.length; i++) {
            if (rainValArr[i] == value) {
                return rainStrValArr[i];
            }
        }
        return rainStrValArr[0];
    }

    private void updateDayForecastData(List<DayForecastModel> dayForecastModels) {
        if(!isVisible()) {
            return;
        }

        ArrayList<Entry> values = new ArrayList<>();
        ArrayList<String> dateList = new ArrayList<>();
        HashMap<String, Integer> dayIndexMap = new HashMap<>();

        ArrayList<BarEntry> tmp = new ArrayList<>();
        Field field;
        try {
            String fieldName = "";
            switch (selectedItem) {
                case TEMP:
                    fieldName = "forecastTemp";
                    break;
                case HUMIDITY:
                    fieldName = "forecastHumidity";
                    break;
                case RAIN:
                    fieldName = "forecastRainPercentage";
                    break;
            }
            field = dayForecastModels.get(0).getClass().getDeclaredField(fieldName);

            float befRainVal = 0;
            for(int i = 0; i < dayForecastModels.size(); i++) {
                DayForecastModel dayForecastModel = dayForecastModels.get(i);
                dateList.add(dayForecastModel.getHourString());
                if(dayIndexMap.get(dayForecastModel.getDayString()) == null) {
                    dayIndexMap.put(dayForecastModel.getDayString(), i);
                }
                CLogger.d(dayForecastModel.toString());
                //values.add(new Entry(i, Float.parseFloat(dayForecastModel.forecastTemp)));
                Object val = field.get(dayForecastModel);
                values.add(new Entry(i, Float.parseFloat(val==null?"0":val.toString())));

                if(dayForecastModel.forecastRain != null) {
                    float tmpVal = rainStringToValue(dayForecastModel.forecastRain);
                    tmp.add(new BarEntry(i, tmpVal));
                    befRainVal = tmpVal;
                } else {
                    tmp.add(new BarEntry(i, befRainVal));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        LineDataSet set1 = new LineDataSet(values, "DataSet 1");
        set1.setLineWidth(2);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
        // add the data sets
        // create a data object with the data sets
        LineData lineData = new LineData(dataSets);
        lineData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                String val = String.format(Locale.getDefault(), "%.0f", value);
                if(selectedItem == SelectedItem.TEMP) {
                    val += "\u2103";
                } else {
                    val += "%";
                }
                return val;
            }
        });
        lineData.setValueTextSize(18);

        CombinedChart combinedChart = mActivity.findViewById(R.id.line_chart);
        combinedChart.clear();
        combinedChart.setData(null);
        CombinedData combinedData = new CombinedData();
        combinedData.setData(lineData);

        if(selectedItem == SelectedItem.RAIN) {
            BarDataSet barDataSet = new BarDataSet(tmp, "bar");
            barDataSet.setColor(Color.GRAY);
            barDataSet.setValueTextSize(10f);
            barDataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    String tmp = rainValueToString(value);
                    return tmp.equals(rainStrValArr[0])?"":tmp;
                }
            });

            combinedData.setData(new BarData(barDataSet));
        }

        combinedChart.setData(combinedData);

        combinedChart.getXAxis().setGranularityEnabled(true);
        combinedChart.getXAxis().setGranularity(1);
        combinedChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        combinedChart.getXAxis().setTextSize(18);
        combinedChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axisBase) {
                return dateList.get((int)value);
            }
        });

        //add limitLine to separate day
        for(String day : dayIndexMap.keySet()) {
            Integer val = dayIndexMap.get(day);
            if(val == null) {
                continue;
            }

            LimitLine limitLine = new LimitLine(val, day);
            limitLine.setTextSize(18);
            limitLine.setLineColor(Color.RED);
            limitLine.setLineWidth(2);
            combinedChart.getXAxis().addLimitLine(limitLine);
        }

        combinedChart.getAxisLeft().setDrawGridLines(false);
        combinedChart.getAxisLeft().setDrawLabels(false);
        combinedChart.getAxisRight().setDrawGridLines(false);
        combinedChart.getAxisRight().setDrawLabels(false);
        combinedChart.getLegend().setEnabled(false);
        combinedChart.setDescription(null);

        combinedChart.setDoubleTapToZoomEnabled(false);
        combinedChart.setVisibleXRangeMaximum(5);
        combinedChart.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(pointModel != null) {
            updateWeather(pointModel);
        }
    }
}
