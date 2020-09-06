package com.jun.weather.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;

import com.jun.weather.BaseApplication;
import com.jun.weather.R;
import com.jun.weather.databinding.FragmentWeekWeatherBinding;
import com.jun.weather.repository.AppRepository;
import com.jun.weather.ui.component.WeeklyItemLayout;
import com.jun.weather.util.CLogger;
import com.jun.weather.viewmodel.CustomViewModelProvider;
import com.jun.weather.viewmodel.DayForecastViewModel;
import com.jun.weather.viewmodel.MidWeatherViewModel;
import com.jun.weather.viewmodel.NowWeatherViewModel;
import com.jun.weather.viewmodel.entity.DayForecastModel;
import com.jun.weather.viewmodel.entity.FailRestResponse;
import com.jun.weather.viewmodel.entity.MidForecastModel;
import com.jun.weather.viewmodel.entity.NowWeatherModel;
import com.jun.weather.viewmodel.entity.WeatherPointModel;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

import java.util.List;

import static com.jun.weather.util.CommonUtils.getDateString;

public class WeekWeatherFragment extends Fragment {
    private Activity mActivity;
    private MidWeatherViewModel midWeatherViewModel;
    private NowWeatherViewModel weatherViewModel;
    private DayForecastViewModel dayForecastViewModel;
    private FragmentWeekWeatherBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_week_weather, container, false);
        return mBinding.getRoot();
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

            mBinding.setDataSet(false);

            midWeatherViewModel = new CustomViewModelProvider(getViewModelStore()).getMidWeatherViewModel(application, appRepository);
            midWeatherViewModel.getUpdateFailData().observe(this, failRestResponse -> midWeatherDataFailProcess(failRestResponse));

            weatherViewModel = new CustomViewModelProvider(getViewModelStore()).getNowWeatherViewModel(application, appRepository);

            dayForecastViewModel = new CustomViewModelProvider(getViewModelStore()).getDayForecastViewModel(application, appRepository);

            LiveData<NowWeatherModel> data1 = weatherViewModel.getData();
            LiveData<List<DayForecastModel>> data2 = dayForecastViewModel.getData();
            LiveData<SparseArray<MidForecastModel>> data3 = midWeatherViewModel.getData();

            data1.observe(getViewLifecycleOwner(), data -> updateMidWeatherData(data, data2.getValue(), data3.getValue()));
            data2.observe(getViewLifecycleOwner(), data -> updateMidWeatherData(data1.getValue(), data, data3.getValue()));
            data3.observe(getViewLifecycleOwner(), data -> updateMidWeatherData(data1.getValue(), data2.getValue(), data));

            if(pointModel != null) {
                updateWeather(pointModel);
            }
        }
    }

    private void updateMidWeatherData(NowWeatherModel nowWeatherModel, List<DayForecastModel> dayForecastModels,
                                      SparseArray<MidForecastModel> midForecastModelSparseArray) {
        if(nowWeatherModel == null || dayForecastModels == null || midForecastModelSparseArray == null) {
            return;
        }

        if(!isVisible()) {
            return;
        }

        mBinding.setDataSet(true);
        ((MainActivity) mActivity).setLoading(false);

        SparseArray<MidForecastModel> shortForecastSparseArray = getShortForecastArray(nowWeatherModel, dayForecastModels);

        for(int i = 0; i < shortForecastSparseArray.size(); i++) {
            int key = shortForecastSparseArray.keyAt(i);
            midForecastModelSparseArray.put(key, shortForecastSparseArray.get(key));
        }

        DateTime dateTime = DateTime.now().toDateTime(DateTimeZone.forID("Asia/Seoul"));

        //init calendar UI
        int nowDay = dateTime.getDayOfWeek();
        if(nowDay < DateTimeConstants.SUNDAY) {
            for(int i = 0; i < nowDay; i++) {
                ((WeeklyItemLayout)mActivity.findViewById(R.id.weekly_row1)).setWeeklyItemVisibility(
                        i+1, View.GONE
                );
            }
        }
        if(nowDay == DateTimeConstants.SUNDAY || nowDay < DateTimeConstants.THURSDAY) {
            mActivity.findViewById(R.id.weekly_row3).setVisibility(View.GONE);
        }

        int startIdx = (nowDay%7)+1;
        int arrayIdx = 1;
        WeeklyItemLayout row1 = mActivity.findViewById(R.id.weekly_row1);
        for(; startIdx <= 7; arrayIdx++,startIdx++) {
            MidForecastModel model = midForecastModelSparseArray.get(arrayIdx);
            CLogger.d(dateTime.plusDays(arrayIdx-1).toString()+">>"+model);
            if(model == null) {
                continue;
            }
            row1.setDateText(startIdx, dateTime.plusDays(arrayIdx-1).toString("MM/dd"));
            row1.setLowTemp(startIdx, model.lowTemp);
            row1.setHighTemp(startIdx, model.highTemp);

            int daySky, nightSky;
            if(model.daySky == 0) {
                daySky = model.amSky;
                nightSky = model.pmSky;
            } else {
                daySky = model.daySky;
                nightSky = model.daySky;
            }
            row1.setSkyImage(startIdx, daySky, nightSky);
        }

        WeeklyItemLayout row2 = mActivity.findViewById(R.id.weekly_row2);
        for(int i = 1; i <= 7; i++,arrayIdx++) {
            MidForecastModel model = midForecastModelSparseArray.get(arrayIdx);
            CLogger.d(dateTime.plusDays(arrayIdx-1).toString()+">>"+model);
            if(model == null) {
                continue;
            }
            row2.setDateText(i, dateTime.plusDays(arrayIdx-1).toString("MM/dd"));
            row2.setLowTemp(i, model.lowTemp);
            row2.setHighTemp(i, model.highTemp);

            int daySky, nightSky;
            if(model.daySky == 0) {
                daySky = model.amSky;
                nightSky = model.pmSky;
            } else {
                daySky = model.daySky;
                nightSky = model.daySky;
            }
            row2.setSkyImage(i, daySky, nightSky);
        }

        WeeklyItemLayout row3 = mActivity.findViewById(R.id.weekly_row3);
        if(arrayIdx <= 10) {
            int endIdx = 10-arrayIdx+1;
            for(int i = 1; i <= endIdx; i++,arrayIdx++) {
                MidForecastModel model = midForecastModelSparseArray.get(arrayIdx);
                CLogger.d(dateTime.plusDays(arrayIdx-1).toString()+">>"+model);
                if(model == null) {
                    continue;
                }
                row3.setDateText(i, dateTime.plusDays(arrayIdx-1).toString("MM/dd"));
                row3.setLowTemp(i, model.lowTemp);
                row3.setHighTemp(i, model.highTemp);

                int daySky, nightSky;
                if(model.daySky == 0) {
                    daySky = model.amSky;
                    nightSky = model.pmSky;
                } else {
                    daySky = model.daySky;
                    nightSky = model.daySky;
                }
                row3.setSkyImage(i, daySky, nightSky);
            }
        }

        for(int i = 0; i < midForecastModelSparseArray.size(); i++) {
            //Log.d(TAG, dateTime.plusDays(i+3).toString()+">>"+midForecastModelSparseArray.keyAt(i)+","+midForecastModelSparseArray.get(midForecastModelSparseArray.keyAt(i)));
            //CLogger.d(dateTime.plusDays(i).toString()+">>"+midForecastModelSparseArray.keyAt(i)+","+midForecastModelSparseArray.get(midForecastModelSparseArray.keyAt(i)));
            CLogger.d(midForecastModelSparseArray.keyAt(i)+","+midForecastModelSparseArray.get(midForecastModelSparseArray.keyAt(i)));
        }
    }

    private SparseArray<MidForecastModel> getShortForecastArray(NowWeatherModel nowWeatherModel,
                                                                List<DayForecastModel> dayForecastModels) {
        SparseArray<MidForecastModel> sparseArray = new SparseArray<>();
        sparseArray.put(1, getNowForecastModel(nowWeatherModel));

        for(DayForecastModel dayForecastModel : dayForecastModels) {
            if(dayForecastModel.forecastLowTemp != null) {
                int idx = getIndexFromDate(dayForecastModel.forecastDate);
                MidForecastModel model = sparseArray.get(idx, new MidForecastModel());
                model.lowTemp = ""+(int)Double.parseDouble(dayForecastModel.forecastLowTemp);
                model.amRainPercentage = dayForecastModel.forecastRainPercentage;
                model.amSky = dayForecastModel.forecastSkyDrawableId;
                model.baseDate = dayForecastModel.forecastDate;
                sparseArray.put(idx, model);
            }

            if(dayForecastModel.forecastHighTemp != null) {
                int idx = getIndexFromDate(dayForecastModel.forecastDate);
                MidForecastModel model = sparseArray.get(idx, new MidForecastModel());
                model.highTemp = ""+(int)Double.parseDouble(dayForecastModel.forecastHighTemp);
                model.pmRainPercentage = dayForecastModel.forecastRainPercentage;
                model.pmSky = dayForecastModel.forecastSkyDrawableId;
                sparseArray.put(idx, model);
            }
        }

        return sparseArray;
    }

    private int getIndexFromDate(String date) {
        DateTime dateTime = DateTime.now().toDateTime(DateTimeZone.forID("Asia/Seoul"));

        for(int i = 0; i < 3; i++) {
            if(dateTime.plusDays(i).toString("yyyyMMdd").equals(date)) {
                return i+1;
            }
        }
        return 0;
    }

    private MidForecastModel getNowForecastModel(NowWeatherModel nowWeatherModel) {
        return new MidForecastModel.Builder()
                .lowTemp(nowWeatherModel.lowTemp.replace("\u2103", ""))
                .highTemp(nowWeatherModel.highTemp.replace("\u2103",""))
                .daySky(nowWeatherModel.nowSkyDrawableId)
                .baseDate(getDateString(0, nowWeatherModel.baseDate+nowWeatherModel.baseTime))
                .build();
    }

    private int failCnt = 0;
    private WeatherPointModel.WeatherPoint pointModel;
    private void midWeatherDataFailProcess(FailRestResponse failRestResponse) {
        CLogger.d(failRestResponse.code + ">>" + failRestResponse.failMsg);
        if(failCnt < 5) {
            failCnt++;
            midWeatherViewModel.updateMidWeather(pointModel.midWeatherCode, pointModel.midTempCode);
        } else {
            failCnt = 0;
            ((MainActivity) mActivity).setUpdateFailUi();
        }
    }

    public void updateWeather(WeatherPointModel.WeatherPoint point) {
        this.pointModel = point;
        if(mActivity != null) {
            ((MainActivity) mActivity).setLoading(true);
            midWeatherViewModel.updateMidWeather(point.midWeatherCode, point.midTempCode);
            weatherViewModel.updateNowWeather(point.x, point.y);
            dayForecastViewModel.updateShortForecastData(point.x, point.y);
        }
    }
}
