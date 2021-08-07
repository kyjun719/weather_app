
package com.jun.weather.viewmodel

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jun.weather.repository.AppRepository

class CustomViewModelProvider(private val appRepository: AppRepository) {
    private val factory = CustomViewModelFactory(appRepository)

    internal class CustomViewModelFactory(private val appRepository: AppRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            try {
                return modelClass.getConstructor(AppRepository::class.java)
                        .newInstance(appRepository)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            throw IllegalArgumentException()
        }
    }

    fun <T : ViewModel> getViewModel(activity: AppCompatActivity, clazz: Class<T>): T {
        return ViewModelProvider(activity, factory).get(clazz)
    }

    fun <T : ViewModel> getViewModel(fragment: Fragment, clazz: Class<T>): T {
        return ViewModelProvider(fragment, factory).get(clazz)
    }
}