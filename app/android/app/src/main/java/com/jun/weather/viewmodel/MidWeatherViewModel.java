package com.jun.weather.viewmodel;

import android.app.Application;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.jun.weather.repository.AppRepository;
import com.jun.weather.repository.web.api.MidLandItem;
import com.jun.weather.repository.web.api.MidTempItem;
import com.jun.weather.repository.web.entity.Enum;
import com.jun.weather.repository.web.entity.RestResponse;
import com.jun.weather.util.CLogger;
import com.jun.weather.util.CommonUtils;
import com.jun.weather.viewmodel.entity.FailRestResponse;
import com.jun.weather.viewmodel.entity.MidForecastModel;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import static com.jun.weather.util.CommonUtils.getDateString;

public class MidWeatherViewModel extends CustomViewModel<SparseArray<MidForecastModel>> {
    public MidWeatherViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);

        LiveData<RestResponse<MidLandItem>> data1 = repository.getMidLandData();
        LiveData<RestResponse<MidTempItem>> data2 = repository.getMidTempData();

        data1.observeForever(data -> updateMidForecastModel(data, data2.getValue()));

        data2.observeForever(data -> updateMidForecastModel(data1.getValue(), data));
    }

    private void updateMidForecastModel(RestResponse<MidLandItem> data1, RestResponse<MidTempItem> data2) {
        if(data1 == null || data2 == null) {
            return;
        }

        if(data1.code != Enum.ResponseCode.OK.getValue()) {
            FailRestResponse failRestResponse = new FailRestResponse();
            failRestResponse.code = data1.code;
            failRestResponse.failMsg = "land forecast fail,"+data1.failMsg;

            appExecutor.runInUIIO(() -> updateFailData.setValue(failRestResponse));
        } else if(data2.code != Enum.ResponseCode.OK.getValue()) {
            FailRestResponse failRestResponse = new FailRestResponse();
            failRestResponse.code = data2.code;
            failRestResponse.failMsg = "temp forecast fail,"+data2.failMsg;

            appExecutor.runInUIIO(() -> updateFailData.setValue(failRestResponse));
        } else if(data1.listBody.size() == 0 || data2.listBody.size() == 0) {
            FailRestResponse failRestResponse = new FailRestResponse();
            failRestResponse.code = Enum.ResponseCode.EXCEPTION_ERROR.getValue();
            failRestResponse.failMsg = "data is empty";

            appExecutor.runInUIIO(() -> updateFailData.setValue(failRestResponse));
        } else {
            if(baseDate == null) {
                setBaseDate();
            }

            SparseArray<MidForecastModel> sparseArray = new SparseArray<>();
            parseData(sparseArray, data1, data2);

            appExecutor.runInUIIO(() -> data.setValue(sparseArray));
        }
    }

    private void parseData(SparseArray<MidForecastModel> sparseArray, RestResponse<MidLandItem> data1,
                           RestResponse<MidTempItem> data2) {
        MidLandItem midLandItem = data1.listBody.get(0);
        MidTempItem midTempItem = data2.listBody.get(0);

        int keyIdx = isYesterdayBase?3:4;
        for(int i = 3; i <= 10; i++) {
            if(i < 8) {
                sparseArray.put(keyIdx++, getUniModel(i, midLandItem, midTempItem));
            } else {
                sparseArray.put(keyIdx++, getMonoModel(i, midLandItem, midTempItem));
            }
        }
    }

    private MidForecastModel getUniModel(int i, MidLandItem midLandItem, MidTempItem midTempItem) {
        try {
            Object amRainPercentage = midLandItem.getClass().getDeclaredField("rnSt"+i+"Am").get(midLandItem);
            Object pmRainPercentage = midLandItem.getClass().getDeclaredField("rnSt"+i+"Pm").get(midLandItem);
            Object amSky = midLandItem.getClass().getDeclaredField("wf"+i+"Am").get(midLandItem);
            Object pmSky = midLandItem.getClass().getDeclaredField("wf"+i+"Pm").get(midLandItem);

            Object highTemp = midTempItem.getClass().getDeclaredField("taMax"+i).get(midTempItem);
            Object highTempMax = midTempItem.getClass().getDeclaredField("taMax"+i+"High").get(midTempItem);
            Object highTempMin = midTempItem.getClass().getDeclaredField("taMax"+i+"Low").get(midTempItem);
            Object lowTemp = midTempItem.getClass().getDeclaredField("taMin"+i).get(midTempItem);
            Object lowTempMax = midTempItem.getClass().getDeclaredField("taMin"+i+"High").get(midTempItem);
            Object lowTempMin = midTempItem.getClass().getDeclaredField("taMin"+i+"Low").get(midTempItem);

            return new MidForecastModel.Builder()
                    .amRainPercentage(amRainPercentage==null?"":amRainPercentage.toString())
                    .pmRainPercentage(pmRainPercentage==null?"":pmRainPercentage.toString())
                    .amSky(CommonUtils.getSkyIcon(amSky==null?"":amSky.toString()))
                    .pmSky(CommonUtils.getSkyIcon(pmSky==null?"":pmSky.toString()))
                    .highTemp(getTempVal(highTemp==null?"":highTemp.toString(),
                            highTempMax==null?"":highTempMax.toString(),
                            highTempMin==null?"":highTempMin.toString()))
                    .lowTemp(getTempVal(lowTemp==null?"":lowTemp.toString(),
                            lowTempMax==null?"":lowTempMax.toString(),
                            lowTempMin==null?"":lowTempMin.toString()))
                    .baseDate(getDateString(i, baseDate))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return new MidForecastModel.Builder().build();
        }
    }

    private MidForecastModel getMonoModel(int i, MidLandItem midLandItem, MidTempItem midTempItem) {
        try {
            Object rainPercentage = midLandItem.getClass().getDeclaredField("rnSt"+i).get(midLandItem);
            Object sky = midLandItem.getClass().getDeclaredField("wf"+i).get(midLandItem);

            Object highTemp = midTempItem.getClass().getDeclaredField("taMax"+i).get(midTempItem);
            Object highTempMax = midTempItem.getClass().getDeclaredField("taMax"+i+"High").get(midTempItem);
            Object highTempMin = midTempItem.getClass().getDeclaredField("taMax"+i+"Low").get(midTempItem);
            Object lowTemp = midTempItem.getClass().getDeclaredField("taMin"+i).get(midTempItem);
            Object lowTempMax = midTempItem.getClass().getDeclaredField("taMin"+i+"High").get(midTempItem);
            Object lowTempMin = midTempItem.getClass().getDeclaredField("taMin"+i+"Low").get(midTempItem);

            return new MidForecastModel.Builder()
                    .dayRainPercentage(rainPercentage==null?"":rainPercentage.toString())
                    .daySky(CommonUtils.getSkyIcon(sky==null?"":sky.toString()))
                    .highTemp(getTempVal(highTemp==null?"":highTemp.toString(),
                            highTempMax==null?"":highTempMax.toString(),
                            highTempMin==null?"":highTempMin.toString()))
                    .lowTemp(getTempVal(lowTemp==null?"":lowTemp.toString(),
                            lowTempMax==null?"":lowTempMax.toString(),
                            lowTempMin==null?"":lowTempMin.toString()))
                    .baseDate(getDateString(i, baseDate))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return new MidForecastModel.Builder().build();
        }
    }

    private String getTempVal(String val, String high, String low) {
        try {
            return ""+(Integer.parseInt(val)+((Integer.parseInt(high)-Integer.parseInt(low))/2));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private boolean isYesterdayBase = false;
    private String baseDate;
    public void updateMidWeather(String landRegionId, String tempRegionId) {
        setBaseDate();

        appExecutor.runInRepositoryIO(() -> {
            repository.updateMidLandData(baseDate, landRegionId);
            repository.updateMidTempData(baseDate, tempRegionId);
        });
    }

    private void setBaseDate() {
        DateTime dateTime = DateTime.now().toDateTime(DateTimeZone.forID("Asia/Seoul"));
        if(dateTime.getHourOfDay() < 6) {
            dateTime = dateTime.minusHours(6+dateTime.getHourOfDay());
            isYesterdayBase = true;
        } else if(dateTime.getHourOfDay() < 18) {
            dateTime = dateTime.minusHours(dateTime.getHourOfDay()-6);
        } else {
            dateTime = dateTime.minusHours(dateTime.getHourOfDay()-18);
        }
        dateTime = dateTime.minusMinutes(dateTime.getMinuteOfHour());
        baseDate = dateTime.toString("yyyyMMddHHmm");
        CLogger.d(baseDate);
    }
}
