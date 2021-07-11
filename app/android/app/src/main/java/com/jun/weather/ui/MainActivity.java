package com.jun.weather.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;
import com.jun.weather.BaseApplication;
import com.jun.weather.R;
import com.jun.weather.databinding.ActivityMainBinding;
import com.jun.weather.repository.AppRepository;
import com.jun.weather.repository.web.enums.Enums;
import com.jun.weather.util.CLogger;
import com.jun.weather.util.CommonUtils;
import com.jun.weather.util.GPSHelper;
import com.jun.weather.util.GeoLocationHelper;
import com.jun.weather.util.PreferenceUtils;
import com.jun.weather.viewmodel.CustomViewModelProvider;
import com.jun.weather.viewmodel.WeatherPointViewModel;
import com.jun.weather.viewmodel.entity.FailRestResponse;
import com.jun.weather.viewmodel.entity.WeatherPointModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity {
    private ActivityMainBinding mBinding;
    private WeatherPointViewModel weatherPointViewModel;
    private PagerAdapter pagerAdapter;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        BaseApplication application = (BaseApplication)getApplication();
        weatherPointViewModel = new CustomViewModelProvider(getViewModelStore()).getWeatherPointViewModel(application);

        loadAutoCompleteTextViewData();
        GeoLocationHelper.getInstance().init(this, this);

        mContext = this;

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setInitLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        initViewPager2();
        initAutoCompleteTextView();
        initGPSButton();
        loadRecentPointList();
        initHelpButton();
    }

    private void initHelpButton() {
        findViewById(R.id.btn_help).setOnClickListener((v) -> {
            Intent intent = new Intent(mContext, HelpActivity.class);
            startActivity(intent);
        });
    }

    private void setInitLocation() {
        if(PreferenceUtils.getInstance().getPointList().size() > 0) {
            updateWeatherFromPoint(PreferenceUtils.getInstance().getPointList().get(0));
        } else {
            GeoLocationHelper.getInstance().registerInitPointListener(point -> {
                if(point != null) {
                    updateWeatherFromPoint(point);
                }
            });
        }
    }

    private void initViewPager2() {
        ViewPager2 viewPager2 = findViewById(R.id.pager);
        viewPager2.setUserInputEnabled(false);
        pagerAdapter = new PagerAdapter(this);
        viewPager2.setAdapter(pagerAdapter);

        new TabLayoutMediator(findViewById(R.id.layout_tab), viewPager2, (tab, position) -> {
            if(position == 0) {
                tab.setText("현재 날씨");
            } else if(position == 1) {
                tab.setText("주간 날씨");
            }
        }).attach();
    }

    private void initAutoCompleteTextView() {
        AutoCompleteTextView autoCompleteTextView = findViewById(R.id.auto_text_address);
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            hideKeyboard(autoCompleteTextView);
            CLogger.d("itemClicked::"+position+","+parent.getItemAtPosition(position));
            WeatherPointModel.WeatherPoint point = (WeatherPointModel.WeatherPoint) parent.getItemAtPosition(position);
            CLogger.d(point+">>"+point.x+","+point.y+","+point.midWeatherCode+","+point.midTempCode);
            addPointToRecentList(point);
            updateWeather(point);
            autoCompleteTextView.clearFocus();
            updateGPSButtonIcon(false);
        });

        autoCompleteTextView.setOnFocusChangeListener((v, hasFocus) -> {
            CLogger.d(v+">>>"+hasFocus);
            if(v.getId() != autoCompleteTextView.getId()) {
                hideKeyboard(v);
            }
        });
    }

    private void addPointToRecentList(WeatherPointModel.WeatherPoint point) {
        PreferenceUtils.getInstance().addPoint(point);
        CLogger.d(point);
        loadRecentPointList();
    }

    private void initGPSButton() {
        findViewById(R.id.btn_gps).setOnClickListener(v -> {
            setLoading(true);
            GeoLocationHelper.getInstance().getNowLocation(mContext, new GeoLocationHelper.GeoLocationResultListener() {
                @Override
                public void onSuccess(WeatherPointModel.WeatherPoint locationPoint) {
                    CLogger.d("find Point::"+locationPoint);
                    updateWeatherFromPoint(locationPoint);
                    updateGPSButtonIcon(true);
                }

                @Override
                public void onFail(FailRestResponse failRestResponse) {
                    CLogger.d(failRestResponse.getCode()+">>"+failRestResponse.getFailMsg());
                    if(failRestResponse.getCode() == Enums.ResponseCode.PERMISSION_NOT_GRANTED.getValue() ||
                            failRestResponse.getCode() == Enums.ResponseCode.SETTING_NOT_GRANTED.getValue()) {
                        runOnUiThread(() -> Toast.makeText(mContext, failRestResponse.getFailMsg(), Toast.LENGTH_SHORT).show());
                    } else {
                        runOnUiThread(() -> Toast.makeText(mContext, "위치정보를 가져오는데 실패하였습니다.", Toast.LENGTH_SHORT).show());
                    }
                    setLoading(false);
                    updateGPSButtonIcon(false);
                }
            });
        });
    }

    private void updateWeatherFromPoint(WeatherPointModel.WeatherPoint locationPoint) {
        addPointToRecentList(locationPoint);

        AutoCompleteTextView autoCompleteTextView = findViewById(R.id.auto_text_address);
        autoCompleteTextView.setText(locationPoint.toString());
        autoCompleteTextView.clearFocus();

        updateWeather(locationPoint);
    }

    private void loadRecentPointList() {
        List<WeatherPointModel.WeatherPoint> list = PreferenceUtils.getInstance().getPointList();
        mBinding.setPointList(list);
        CLogger.d("pref::"+list.size());
        for(WeatherPointModel.WeatherPoint point : list) {
            CLogger.d(point.deg_1+","+point.deg_2+","+point.deg_3);
        }

        findViewById(R.id.recent_point1).setOnClickListener((v) -> {
            if(list.size() > 0) {
                updateWeatherFromPoint(list.get(0));
            }
        });

        findViewById(R.id.recent_point2).setOnClickListener((v) -> {
            if(list.size() > 1) {
                updateWeatherFromPoint(list.get(1));
            }
        });

        findViewById(R.id.recent_point3).setOnClickListener((v) -> {
            if(list.size() > 2) {
                updateWeatherFromPoint(list.get(2));
            }
        });
    }

    private static class PagerAdapter extends FragmentStateAdapter {
        private NowWeatherFragment nowWeatherFragment;
        private WeekWeatherFragment weekWeatherFragment;
        private WeatherPointModel.WeatherPoint point;

        public PagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        private LifecycleEventObserver lifecycleEventObserver = new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                CLogger.d(source.toString()+">>"+event.toString());
                if(event == Lifecycle.Event.ON_RESUME && point != null) {
                    if(source instanceof NowWeatherFragment || source instanceof WeekWeatherFragment) {
                        notifyPointSelected(point);
                    }
                }
            }
        };

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            CLogger.d("createFragment::"+position);
            if(position == 0) {
                return new NowWeatherFragment();
            } else {
                return new WeekWeatherFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }

        public void notifyPointSelected(WeatherPointModel.WeatherPoint point) {
            this.point = point;
            if(nowWeatherFragment != null) {
                CLogger.d("update nowWeatherFragment");
                nowWeatherFragment.updateWeather(point);
            } else {
                CLogger.d("nowWeatherFragment is null");
            }
            if(weekWeatherFragment != null) {
                weekWeatherFragment.updateWeather(point);
            }
        }

        public void addFragment(Fragment fragment) {
            if(fragment instanceof NowWeatherFragment) {
                nowWeatherFragment = (NowWeatherFragment) fragment;
                nowWeatherFragment.getLifecycle().addObserver(lifecycleEventObserver);
            }
            if(fragment instanceof WeekWeatherFragment) {
                weekWeatherFragment = (WeekWeatherFragment) fragment;
                weekWeatherFragment.getLifecycle().addObserver(lifecycleEventObserver);
            }
        }
    }

    public void addFragment(Fragment fragment) {
        pagerAdapter.addFragment(fragment);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        CommonUtils.notifyPermissionRequestResult(mContext, requestCode, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GPSHelper.REQUEST_CHECK_GPS_SETTINGS) {
            GPSHelper.getInstance().checkGPSSettingResult(mContext, resultCode);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                //EditText에 입력중인 경우
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    //EditText외의 영역을 터치한 경우 키보드 내림
                    hideKeyboard(v);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    private void hideKeyboard(View v) {
        v.clearFocus();
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    private void loadAutoCompleteTextViewData() {
        AutoCompleteTextView autoCompleteTextView = findViewById(R.id.auto_text_address);
        ArrayAdapter<WeatherPointModel.WeatherPoint> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        autoCompleteTextView.setAdapter(adapter);

        weatherPointViewModel.getData().observe(this, (weatherPointModels) -> {
            adapter.addAll(weatherPointModels.weatherPointList);
            adapter.notifyDataSetChanged();
        });
        weatherPointViewModel.updatePointData(this);
    }

    private void updateGPSButtonIcon(boolean b) {
        runOnUiThread(() -> {
            ImageButton imageButton = findViewById(R.id.btn_gps);
            if(b) {
                imageButton.setImageResource(R.drawable.ic_baseline_gps_accent);
            } else {
                imageButton.setImageResource(R.drawable.ic_baseline_gps_black);
            }
        });
    }

    private void updateWeather(WeatherPointModel.WeatherPoint point) {
        setLoading(true);
        pagerAdapter.notifyPointSelected(point);
    }

    public void setLoading(boolean isLoading) {
        runOnUiThread(() -> mBinding.setLoading(isLoading));
    }

    public void setUpdateFailUi() {
        if(mBinding.getLoading()) {
            runOnUiThread(() -> {
                Toast.makeText(mContext, "업데이트에 실패하였습니다. 다시 시도해주시기 바랍니다.", Toast.LENGTH_SHORT).show();
                ((AutoCompleteTextView)findViewById(R.id.auto_text_address)).setText("");
                updateGPSButtonIcon(false);
                mBinding.setLoading(false);
            });
        }
    }
}
