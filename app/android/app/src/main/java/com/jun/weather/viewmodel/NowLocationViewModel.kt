package com.jun.weather.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.jun.weather.repository.AppRepository
import com.jun.weather.repository.web.entity.KakaoRegionCodeRes
import com.jun.weather.repository.web.entity.RestResponse
import com.jun.weather.repository.web.enums.Enums
import com.jun.weather.ui.entity.FailRestResponse
import com.jun.weather.ui.entity.NowLocation
import kotlinx.coroutines.launch

class NowLocationViewModel(repository: AppRepository) : BaseViewModel(repository) {
    private val _data = MutableLiveData<List<NowLocation>>()
    val data: LiveData<List<NowLocation>> = _data

    private fun updateNowLocationModel(data1: RestResponse<KakaoRegionCodeRes>) {
        if (data1.code != Enums.ResponseCode.OK.value) {
            updateFailData.postValue(
                    FailRestResponse(data1.code, data1.failMsg)
            )
        } else {
            val kakaoRegionCodeRes = data1.singleBody
            val list = mutableListOf<NowLocation>()
            for (item in kakaoRegionCodeRes!!.documents) {
                val nowLocation = NowLocation(
                        x = item.x,
                        y = item.y,
                        code = item.code,
                        deg1 = item.region_1depth_name.replace("특별자치", ""),
                        deg2 = item.region_2depth_name,
                        deg3 = item.region_3depth_name,
                        address = item.address_name.replace("특별자치", ""),
                )
                if (nowLocation.deg1 == "세종특별자치시" && nowLocation.deg2.isEmpty()) {
                    nowLocation.deg2 = "세종특별자치시"
                }

                list.add(nowLocation)
            }
            _data.postValue(list)
        }
    }

    fun updateNowLocation(lng: Double, lat: Double) {
        viewModelScope.launch {
            updateNowLocationModel(repository.getKakaoRegionCodeRes(lng, lat))
        }
    }
}