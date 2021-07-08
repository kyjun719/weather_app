package com.jun.weather.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.jun.weather.repository.AppRepository;
import com.jun.weather.repository.web.api.ForecastItem;
import com.jun.weather.repository.web.api.ObserveItem;
import com.jun.weather.repository.web.entity.RestResponse;
import com.jun.weather.util.CLogger;
import com.jun.weather.util.CommonUtils;
import com.jun.weather.viewmodel.entity.Enum;
import com.jun.weather.viewmodel.entity.FailRestResponse;
import com.jun.weather.viewmodel.entity.NowWeatherModel;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class NowWeatherViewModel extends CustomViewModel<NowWeatherModel> {
    public NowWeatherViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);

        LiveData<RestResponse<ObserveItem>> nowWeatherData = repository.getNowWeatherData();
        LiveData<RestResponse<ForecastItem>> lowTempData = repository.getLowTempData();
        LiveData<RestResponse<ForecastItem>> highTempData = repository.getHighTempData();
        LiveData<RestResponse<ForecastItem>> shortForecastData = repository.getUltraShortForecastData();

        nowWeatherData.observeForever(data -> updateNowWeatherData(data, lowTempData.getValue(), highTempData.getValue(), shortForecastData.getValue()));
        lowTempData.observeForever(data -> updateNowWeatherData(nowWeatherData.getValue(), data, highTempData.getValue(), shortForecastData.getValue()));
        highTempData.observeForever(data -> updateNowWeatherData(nowWeatherData.getValue(), lowTempData.getValue(), data, shortForecastData.getValue()));
        shortForecastData.observeForever(data -> updateNowWeatherData(nowWeatherData.getValue(), lowTempData.getValue(), highTempData.getValue(), data));
    }

    private void updateNowWeatherData(RestResponse<ObserveItem> nowWeatherData,
                                      RestResponse<ForecastItem> lowTempData,
                                      RestResponse<ForecastItem> highTempData,
                                      RestResponse<ForecastItem> shortForecastData) {
        if(data == null) {
            return;
        }

        if(nowWeatherData == null || lowTempData == null || highTempData == null || shortForecastData == null) {
            return;
        }

        FailRestResponse failRestResponse = new FailRestResponse();
        if(nowWeatherData.code != com.jun.weather.repository.web.entity.Enum.ResponseCode.OK.getValue()) {
            failRestResponse.code = nowWeatherData.code;
            failRestResponse.failMsg = nowWeatherData.failMsg;

            appExecutor.runInUIIO(() -> updateFailData.setValue(failRestResponse));
        } else if(lowTempData.code != com.jun.weather.repository.web.entity.Enum.ResponseCode.OK.getValue()) {
            failRestResponse.code = lowTempData.code;
            failRestResponse.failMsg = lowTempData.failMsg;

            appExecutor.runInUIIO(() -> updateFailData.setValue(failRestResponse));
        } else if(highTempData.code != com.jun.weather.repository.web.entity.Enum.ResponseCode.OK.getValue()) {
            failRestResponse.code = highTempData.code;
            failRestResponse.failMsg = highTempData.failMsg;

            appExecutor.runInUIIO(() -> updateFailData.setValue(failRestResponse));
        } else if(shortForecastData.code != com.jun.weather.repository.web.entity.Enum.ResponseCode.OK.getValue()) {
            failRestResponse.code = shortForecastData.code;
            failRestResponse.failMsg = shortForecastData.failMsg;

            appExecutor.runInUIIO(() -> updateFailData.setValue(failRestResponse));
        } else {
            NowWeatherModel nowWeatherModel = new NowWeatherModel();
            nowWeatherModel.baseDate = nowWeatherData.listBody.get(0).getBaseDate();
            nowWeatherModel.baseTime = nowWeatherData.listBody.get(0).getBaseTime();
            nowWeatherModel.nowBaseTime = "기준시간: "+nowWeatherModel.baseDate+" "+nowWeatherModel.baseTime;

            /*
            T1H	기온	℃	10
            RN1	1시간 강수량	mm	8
            UUU	동서바람성분	m/s	12
            VVV	남북바람성분	m/s	12
            REH	습도	%	8
            PTY	강수형태	코드값	4
            VEC	풍향	0	10
            WSD	풍속	1	10
             */
            for(ObserveItem item : nowWeatherData.listBody) {
                switch (item.getCategory()) {
                    case "T1H":
                        nowWeatherModel.nowTemp = item.getObsrValue().split("\\.")[0]+"\u2103";
                        break;
                    case "RN1":
                        nowWeatherModel.nowRain = item.getObsrValue();
                        break;
                    case "REH":
                        nowWeatherModel.nowHumidity = item.getObsrValue()+"%";
                        break;
                    case "PTY":
                        nowWeatherModel.rainState = Enum.PTY.getStringValByVal(Integer.parseInt(item.getObsrValue()));
                        break;
                    case "VEC":
                        nowWeatherModel.windDir = Enum.WIND_DIR.getStringValByVal(Integer.parseInt(item.getObsrValue()));
                        break;
                    case "WSD":
                        nowWeatherModel.wind = item.getObsrValue();
                        break;
                }
            }
            nowWeatherModel.nowWind = nowWeatherModel.windDir+" "+nowWeatherModel.wind;

            for(ForecastItem item : lowTempData.listBody) {
                if(item.getCategory().equals("TMN")) {
                    nowWeatherModel.lowTemp = item.getFcstValue().split("\\.")[0]+"\u2103";
                    break;
                }
            }

            for(ForecastItem item : highTempData.listBody) {
                if(item.getCategory().equals("TMX")) {
                    nowWeatherModel.highTemp = item.getFcstValue().split("\\.")[0]+"\u2103";
                    break;
                }
            }

            DateTime dateTime = getUltraShortForecastDateTime().plusMinutes(30);
            String baseDate = dateTime.toString("yyyyMMdd");
            String baseTime = dateTime.toString("HHmm");
            int sky = -1, pty = -1;
            for(ForecastItem item : shortForecastData.listBody) {
                if(item.getFcstDate().equals(baseDate) && item.getFcstTime().equals(baseTime)) {
                    if(item.getCategory().equals("PTY")) {
                        pty = Integer.parseInt(item.getFcstValue());
                    }
                    if(item.getCategory().equals("SKY")) {
                        sky = Integer.parseInt(item.getFcstValue());
                    }
                }
            }

            nowWeatherModel.nowSkyDrawableId = CommonUtils.getSkyIcon(sky, pty);

            appExecutor.runInUIIO(() -> data.setValue(nowWeatherModel));
        }
    }

    public void updateNowWeather(int nx, int ny) {
        DateTime dateTime = DateTime.now().toDateTime(DateTimeZone.forID("Asia/Seoul"));

        if(dateTime.getMinuteOfHour()<40) {
            dateTime = dateTime.minusHours(1);
        }

        dateTime = dateTime.minusMinutes(dateTime.getMinuteOfHour());
        String baseDate = dateTime.toString("yyyyMMdd");
        String baseTime = dateTime.toString("HHmm");
        CLogger.d(baseDate+","+baseTime);
        appExecutor.runInRepositoryIO(() -> {
            repository.updateNowWeather(baseDate, baseTime, nx, ny);

            updateTempMinMaxValue(nx, ny);
            updateShortForecastData(nx, ny);
        });
    }

    private void updateTempMinMaxValue(int nx, int ny) {
        DateTime lowTempDateTime = getLowTempDateTime();
        DateTime highTempDateTime = getHighTempDateTime();

        CLogger.d(lowTempDateTime+">>>>>>>"+highTempDateTime);
        repository.updateLowTempData(lowTempDateTime.toString("yyyyMMdd"), lowTempDateTime.toString("HHmm"), nx, ny);
        repository.updateHighTempData(highTempDateTime.toString("yyyyMMdd"), highTempDateTime.toString("HHmm"), nx, ny);
    }

    private DateTime getLowTempDateTime() {
        //최저기온은 금일 02시, 아닐경우 전일23시
        DateTime dateTime = DateTime.now().toDateTime(DateTimeZone.forID("Asia/Seoul"));
        int nowHour = dateTime.getHourOfDay();
        int nowMinute = dateTime.getMinuteOfHour();
        if(nowHour < 2 || (nowHour == 2 && nowMinute < 10)) {
            dateTime = dateTime.minusHours(nowHour+1);
        } else {
            dateTime = dateTime.minusHours(nowHour-2);
        }
        dateTime = dateTime.minusMinutes(dateTime.getMinuteOfHour());
        return dateTime;
    }

    private DateTime getHighTempDateTime() {
        //최고기온은 금일 11시, 아닐경우 3시간전씩 돌림
        DateTime dateTime = DateTime.now().toDateTime(DateTimeZone.forID("Asia/Seoul"));
        int nowHour = dateTime.getHourOfDay();
        int nowMinute = dateTime.getMinuteOfHour();
        if(nowHour > 11 || (nowHour == 11 && nowMinute > 10)) {
            dateTime = dateTime.minusHours(nowHour-11);
        } else {
            for(int i = 8; i >= -1; i-=3) {
                if(nowHour > i) {
                    dateTime = dateTime.minusHours(nowHour-i);
                    CLogger.d("selected::"+dateTime);
                    break;
                }
            }
        }
        CLogger.d("high::"+dateTime);
        dateTime = dateTime.minusMinutes(dateTime.getMinuteOfHour());
        return dateTime;
    }

    private void updateShortForecastData(int nx, int ny) {
        DateTime dateTime = getUltraShortForecastDateTime();
        String baseDate = dateTime.toString("yyyyMMdd");
        String baseTime = dateTime.toString("HHmm");
        CLogger.d(baseDate+","+baseTime);
        repository.updateUltraShortForecastData(baseDate, baseTime, nx, ny);
    }

    private DateTime getUltraShortForecastDateTime() {
        DateTime dateTime = DateTime.now().toDateTime(DateTimeZone.forID("Asia/Seoul"));

        if(dateTime.getMinuteOfHour()<45) {
            dateTime = dateTime.minusHours(1);
        }

        return dateTime.minusMinutes(dateTime.getMinuteOfHour()-30);
    }
}
