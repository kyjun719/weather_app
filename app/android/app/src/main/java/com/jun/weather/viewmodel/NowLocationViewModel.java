package com.jun.weather.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.jun.weather.repository.AppRepository;
import com.jun.weather.repository.web.api.KakaoRegionCodeRes;
import com.jun.weather.repository.web.api.RegionItem;
import com.jun.weather.repository.web.entity.Enum;
import com.jun.weather.repository.web.entity.RestResponse;
import com.jun.weather.viewmodel.entity.FailRestResponse;
import com.jun.weather.viewmodel.entity.NowLocationModel;

public class NowLocationViewModel extends CustomViewModel<NowLocationModel> {
    public NowLocationViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);

        LiveData<RestResponse<KakaoRegionCodeRes>> kakaoRegionCodeResLiveData = repository.getKakaoRegionCodeRes();
        kakaoRegionCodeResLiveData.observeForever(data -> updateNowLocationModel(data));
    }

    private void updateNowLocationModel(RestResponse<KakaoRegionCodeRes> data1) {
        if(data1.code != Enum.ResponseCode.OK.getValue()) {
            FailRestResponse failRestResponse = new FailRestResponse();
            failRestResponse.code = data1.code;
            failRestResponse.failMsg = data1.failMsg;

            appExecutor.runInUIIO(() -> updateFailData.setValue(failRestResponse));
        } else {
            KakaoRegionCodeRes kakaoRegionCodeRes = data1.singleBody;
            NowLocationModel locationModel = new NowLocationModel();
            for(RegionItem item : kakaoRegionCodeRes.getDocuments()) {
                NowLocationModel.NowLocation nowLocation = new NowLocationModel.NowLocation();
                nowLocation.x = item.getX();
                nowLocation.y = item.getY();
                nowLocation.code = item.getCode();
                nowLocation.deg1 = item.getRegion_1depth_name().replace("특별자치", "");
                nowLocation.deg2 = item.getRegion_2depth_name();
                if(nowLocation.deg1.equals("세종특별자치시")  && item.getRegion_2depth_name().isEmpty()) {
                    nowLocation.deg2 = "세종특별자치시";
                }
                nowLocation.deg3 = item.getRegion_3depth_name();

                nowLocation.address = item.getAddress_name().replace("특별자치", "");
                locationModel.addressList.add(nowLocation);
            }

            appExecutor.runInUIIO(() -> data.setValue(locationModel));
        }
    }

    public void updateNowLocation(double lng, double lat) {
        appExecutor.runInRepositoryIO(() -> repository.updateKakaoRegionCodeRes(lng, lat));
    }
}
