package com.jun.weather.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jun.weather.repository.AppRepository
import com.jun.weather.ui.entity.FailRestResponse

open class BaseViewModel(var repository: AppRepository) : ViewModel() {
    val updateFailData = MutableLiveData<FailRestResponse>()
    val isLoading = MutableLiveData<Boolean>()
}