package com.jun.weather.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.jun.weather.repository.AppRepository;
import com.jun.weather.repository.web.entity.ForecastItem;
import com.jun.weather.repository.web.entity.RestResponse;
import com.jun.weather.repository.web.enums.Enums;
import com.jun.weather.util.CLogger;
import com.jun.weather.util.CommonUtils;
import com.jun.weather.viewmodel.entity.DayForecastModel;
import com.jun.weather.viewmodel.entity.Enum;
import com.jun.weather.viewmodel.entity.FailRestResponse;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DayForecastViewModel extends CustomViewModel<List<DayForecastModel>> {
    public DayForecastViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);

        LiveData<RestResponse<ForecastItem>> shortForecastData = repository.getShortForecastData();
        LiveData<RestResponse<ForecastItem>> ultraShortForecastData = repository.getUltraShortForecastData();

        shortForecastData.observeForever(data -> updateDayForecastData(data, ultraShortForecastData.getValue()));
        ultraShortForecastData.observeForever(data -> updateDayForecastData(shortForecastData.getValue(), data));
    }

    private void updateDayForecastData(RestResponse<ForecastItem> shortForecastData, RestResponse<ForecastItem> ultraShortForecastData) {
        if(data == null) {
            return;
        }

        if(shortForecastData == null || ultraShortForecastData == null) {
            return;
        }

        FailRestResponse failRestResponse = new FailRestResponse();
        if(shortForecastData.getCode() != Enums.ResponseCode.OK.getValue()) {
            appExecutor.runInUIIO(() ->
                    updateFailData.setValue(
                            new FailRestResponse(shortForecastData.getCode(),
                                    shortForecastData.getFailMsg()))
            );
        } else if(ultraShortForecastData.getCode() != Enums.ResponseCode.OK.getValue()) {
            appExecutor.runInUIIO(() ->
                    updateFailData.setValue(
                            new FailRestResponse(ultraShortForecastData.getCode(),
                                    ultraShortForecastData.getFailMsg()))
            );
        } else {
            Map<Double, DayForecastModel> map = new HashMap<>();
            List<ForecastItem> itemList = new ArrayList<>(shortForecastData.getListBody());
            itemList.addAll(ultraShortForecastData.getListBody());
            for(ForecastItem item : itemList) {
                double date = Double.parseDouble(item.getFcstDate()+item.getFcstTime());
                if(map.get(date) == null) {
                    map.put(date, new DayForecastModel());
                }

                DayForecastModel model = map.get(date);
                if(model != null) {
                    parseData(model, item);
                }
            }

            for(Double key : map.keySet()) {
                DayForecastModel model = map.get(key);
                if(model != null) {
                    setSkyIcon(model);
                }
            }

            List<DayForecastModel> list = new ArrayList<>(map.values());
            Collections.sort(list, (a, b) -> {
                long ta = a.getDateTime().getMillis();
                long tb = b.getDateTime().getMillis();

                return Long.compare(ta,tb);
            });

            appExecutor.runInUIIO(() -> data.setValue(list));
        }
    }

    private void parseData(DayForecastModel dayForecastModel, ForecastItem item) {
        /*
        동네예보
        POP	강수확률	%	8
        PTY	강수형태	코드값	4
        R06	6시간 강수량	범주 (1 mm)	8
        REH	습도	%	8
        S06	6시간 신적설	범주(1 cm)	8
        SKY	하늘상태	코드값	4
        T3H	3시간 기온	℃	10
        WAV	파고	M	8
        VEC	풍향	m/s	10
        WSD	풍속	1	10
         */

        /*
        초단기예보
        T1H	기온	℃	10
        RN1	1시간 강수량	mm	8
        SKY	하늘상태	코드값	4
        UUU	동서바람성분	m/s	12
        VVV	남북바람성분	m/s	12
        REH	습도	%	8
        PTY	강수형태	코드값	4
        LGT	낙뢰	코드값	4
        VEC	풍향	0	10
        WSD	풍속	1	10
         */
        //Log.d(TAG, item.category+">>"+item.fcstValue);
        dayForecastModel.forecastDate = item.getFcstDate();
        dayForecastModel.forecastTime = item.getFcstTime();
        switch (item.getCategory()) {
            case "POP":
                dayForecastModel.forecastRainPercentage = item.getFcstValue();
                break;
            case "PTY":
                dayForecastModel.forecastRainState = Enum.PTY.getStringValByVal(Integer.parseInt(item.getFcstValue()));
                break;
            case "R06":
                dayForecastModel.forecastRain = getForecastRainString(Double.parseDouble(item.getFcstValue()));
                break;
            case "RN1":
                dayForecastModel.forecastRain = item.getFcstValue();
                break;
            case "REH":
                dayForecastModel.forecastHumidity = item.getFcstValue();
                break;
            case "S06":
                dayForecastModel.forecastSnow = getForecastSnowString(Double.parseDouble(item.getFcstValue()));
                break;
            case "SKY":
                dayForecastModel.forecastSky = Enum.SKY.getStringValByVal(Integer.parseInt(item.getFcstValue()));
                break;
            case "T3H":
            case "T1H":
                dayForecastModel.forecastTemp = item.getFcstValue();
                break;
            case "TMN":
                dayForecastModel.forecastLowTemp = item.getFcstValue();
                break;
            case "TMX":
                dayForecastModel.forecastHighTemp = item.getFcstValue();
                break;
            case "WAV":
                dayForecastModel.forecastWave = item.getFcstValue();
                break;
            case "VEC":
                dayForecastModel.forecastWindDir = Enum.WIND_DIR.getStringValByVal(Integer.parseInt(item.getFcstValue()));
                break;
            case "WSD":
                dayForecastModel.forecastWind = item.getFcstValue();
                break;
        }
    }

    private String getForecastRainString(double val) {
        /*
        0.1mm 미만	0mm 또는 없음	0
        0.1mm 이상 1mm 미만	1mm 미만	1
        1 mm 이상 5 mm 미만	1~4mm	5
        5 mm 이상 10 mm 미만	5~9mm	10
        10 mm 이상 20 mm 미만	10~19mm	20
        20 mm 이상 40 mm 미만	20~39mm	40
        40 mm 이상 70 mm 미만	40~69mm	70
        70 mm 이상	70mm 이상	100
         */
        if(val < 0.1) {
            return "없음";
        }
        if(val < 1) {
            return "1mm미만";
        }
        if(val < 5) {
            return "1~4mm";
        }
        if(val < 10) {
            return "5~9mm";
        }
        if(val < 20) {
            return "10~19mm";
        }
        if(val < 40) {
            return "20~39mm";
        }
        if(val < 70) {
            return "40~69mm";
        }

        return "70mm 이상";
    }

    private String getForecastSnowString(double val) {
        /*
        범주	문자열표시	GRIB 저장값
        0.1 cm 미만	0cm 또는 없음	0
        0.1 cm 이상 1 cm 미만	1cm 미만	1
        1 cm 이상 5 cm 미만	1~4cm	5
        5 cm 이상 10 cm 미만	5~9cm	10
        10 cm 이상 20 cm 미만	10~19cm	20
        20 cm 이상	20cm 이상	100
         */
        if(val < 0.1) {
            return "없음";
        }
        if(val < 1) {
            return "1cm미만";
        }
        if(val < 5) {
            return "1~4cm";
        }
        if(val < 10) {
            return "5~9cm";
        }
        if(val < 20) {
            return "10~19cm";
        }
        return "20cm 이상";
    }

    private void setSkyIcon(DayForecastModel dayForecastModel) {
        int valPty = Enum.PTY.getValByStringVal(dayForecastModel.forecastRainState);
        int valSky = Enum.SKY.getValByStringVal(dayForecastModel.forecastSky);

        dayForecastModel.forecastSkyDrawableId = CommonUtils.getSkyIcon(valSky, valPty);
    }

    public void updateShortForecastData(int nx, int ny) {
        DateTime dateTime = DateTime.now().toDateTime(DateTimeZone.forID("Asia/Seoul"));

        int[] arr = {-1,2,5,8,11,14,17,20,23,26};
        int nowHour = dateTime.getHourOfDay();
        boolean isOClock = false;
        for(int i = 1; i < arr.length-1; i++) {
            if(nowHour == arr[i] && dateTime.getMinuteOfHour() > 10) {
                isOClock = true;
                break;
            }
        }

        if(!isOClock) {
            for(int i = 1; i < arr.length; i++) {
                if(arr[i-1] <= nowHour && arr[i] >= nowHour) {
                    CLogger.d(nowHour+">>"+arr[i-1]+"~"+arr[i]);
                    dateTime = dateTime.minusHours(nowHour-arr[i-1]);
                    break;
                }
            }
        }

        /*
        String baseDate = ""+dateTime.getYear();
        baseDate += dateTime.getMonthOfYear()>9?dateTime.getMonthOfYear():"0"+dateTime.getMonthOfYear();
        baseDate += dateTime.getDayOfMonth()>9?dateTime.getDayOfMonth():"0"+dateTime.getDayOfMonth();

        String baseTime = dateTime.getHourOfDay()>9?""+dateTime.getHourOfDay():"0"+dateTime.getHourOfDay();
        baseTime += "00";
         */
        dateTime = dateTime.minusMinutes(dateTime.getMinuteOfHour());
        String baseDate = dateTime.toString("yyyyMMdd");
        String baseTime = dateTime.toString("HHmm");

        CLogger.d(baseDate+","+baseTime);
        appExecutor.runInRepositoryIO(() -> repository.updateShortForecastWeather(baseDate, baseTime, nx, ny));
    }
}
