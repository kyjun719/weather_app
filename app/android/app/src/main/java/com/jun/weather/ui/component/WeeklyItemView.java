package com.jun.weather.ui.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.jun.weather.R;

public class WeeklyItemView extends ConstraintLayout {
    private TextView text_date;
    private TextView text_lowTemp;
    private TextView text_highTemp;
    private ImageView image_sky_day, image_sky_night;

    public WeeklyItemView(Context context) {
        super(context);
        initView(context);
    }

    public WeeklyItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        getAttrs(attrs);
    }

    public WeeklyItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
        getAttrs(attrs, defStyleAttr);
    }

    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.component_weekly_item, this, false);
        addView(view);

        text_date = findViewById(R.id.text_date);
        image_sky_day = findViewById(R.id.image_sky_day);
        image_sky_night = findViewById(R.id.image_sky_night);
        text_lowTemp = findViewById(R.id.text_lowTemp);
        text_highTemp = findViewById(R.id.text_highTemp);
    }

    private void getAttrs(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.WeeklyItemView);
        setTypeArray(typedArray);
    }

    private void getAttrs(AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.WeeklyItemView, defStyleAttr, 0);
        setTypeArray(typedArray);
    }

    private void setTypeArray(TypedArray typedArray) {
        text_date.setText(typedArray.getString(R.styleable.WeeklyItemView_date));
        image_sky_day.setImageResource(typedArray.getResourceId(R.styleable.WeeklyItemView_day_sky_image, 0));
        image_sky_night.setImageResource(typedArray.getResourceId(R.styleable.WeeklyItemView_night_sky_image, 0));
        text_lowTemp.setText(typedArray.getString(R.styleable.WeeklyItemView_low_temp));
        text_highTemp.setText(typedArray.getString(R.styleable.WeeklyItemView_high_temp));

        typedArray.recycle();
    }

    public void setDateText(String s) {
        text_date.setText(s);
    }

    public void setSkyDayImage(int resourceId) {
        image_sky_day.setImageResource(resourceId);
    }

    public void setSkyNightImage(int resourceId) {
        image_sky_night.setImageResource(resourceId);
    }

    public void setLowTemp(int i) {
        text_lowTemp.setText(String.valueOf(i));
    }

    public void setLowTemp(String val) {
        text_lowTemp.setText(val);
    }

    public void setHighTemp(int i) {
        text_highTemp.setText(String.valueOf(i));
    }

    public void setHighTemp(String val) {
        text_highTemp.setText(val);
    }
}
