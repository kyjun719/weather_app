package com.jun.weather.ui

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.jun.weather.BaseApplication
import com.jun.weather.R
import com.jun.weather.databinding.ActivityMainBinding
import com.jun.weather.repository.web.enums.Enums
import com.jun.weather.ui.entity.FailRestResponse
import com.jun.weather.ui.entity.WeatherPoint
import com.jun.weather.util.CLogger
import com.jun.weather.util.CommonUtils.notifyPermissionRequestResult
import com.jun.weather.util.GPSHelper
import com.jun.weather.util.GPSHelper.Companion.REQUEST_CHECK_GPS_SETTINGS
import com.jun.weather.util.GeoLocationHelper
import com.jun.weather.util.GeoLocationHelper.GeoLocationResultListener
import com.jun.weather.util.GeoLocationHelper.InitPointListener
import com.jun.weather.util.PreferenceUtils.Companion.getInstance
import com.jun.weather.viewmodel.CustomViewModelProvider
import com.jun.weather.viewmodel.WeatherPointViewModel
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var weatherPointViewModel: WeatherPointViewModel? = null
    private lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mContext = this

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val application = application as BaseApplication
        weatherPointViewModel = CustomViewModelProvider(application.repository)
                .getViewModel(this, WeatherPointViewModel::class.java)

        GeoLocationHelper.instance!!.init(this, this)
        initView()
    }

    private fun initView() {
        initViewPager2()
        initHelpButton()

        SearchAddressComponent(this, binding.layoutSearchAddress)
    }

    private fun initHelpButton() {
        binding.btnHelp.setOnClickListener {
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initViewPager2() {
        binding.pager.isUserInputEnabled = false

        binding.pager.adapter = object: FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                CLogger.d("createFragment::$position")
                return if (position == 0) {
                    NowWeatherFragment()
                } else {
                    WeekWeatherFragment()
                }
            }

            override fun getItemCount(): Int {
                return 2
            }
        }

        TabLayoutMediator(findViewById(R.id.layout_tab), binding.pager) { tab: TabLayout.Tab, position: Int ->
            if (position == 0) {
                tab.text = "현재 날씨"
            } else if (position == 1) {
                tab.text = "주간 날씨"
            }
        }.attach()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        notifyPermissionRequestResult(this, requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_GPS_SETTINGS) {
            GPSHelper.instance!!.checkGPSSettingResult(this, resultCode)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText || v is AutoCompleteTextView) {
                //EditText에 입력중인 경우
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    //EditText외의 영역을 터치한 경우 키보드 내림
                    hideKeyboard(v)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    fun hideKeyboard(v: View) {
        v.clearFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }

    fun setLoading(isLoading: Boolean) {
        runOnUiThread { binding.loading = isLoading }
    }

    fun setUpdateFailUi() {
        binding.loading?.let {
            if(it) {
                runOnUiThread {
                    Toast.makeText(mContext, "업데이트에 실패하였습니다. 다시 시도해주시기 바랍니다.", Toast.LENGTH_SHORT).show()
                    (findViewById<View>(R.id.auto_text_address) as AutoCompleteTextView).setText("")
                    binding.loading = false
                }
            }
        }
    }
}