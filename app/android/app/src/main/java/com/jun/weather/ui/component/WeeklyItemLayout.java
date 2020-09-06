package com.jun.weather.ui.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.jun.weather.R;

public class WeeklyItemLayout extends ConstraintLayout {
    private WeeklyItemView[] weeklyItemViews;

    public WeeklyItemLayout(Context context) {
        super(context);
        initView(context);
    }

    public WeeklyItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        getAttrs(attrs);
    }

    public WeeklyItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
        getAttrs(attrs, defStyleAttr);
    }

    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.component_weekly_layout, this, false);
        addView(view);

        weeklyItemViews = new WeeklyItemView[8];
        weeklyItemViews[1] = findViewById(R.id.weekly_item_1);
        weeklyItemViews[2] = findViewById(R.id.weekly_item_2);
        weeklyItemViews[3] = findViewById(R.id.weekly_item_3);
        weeklyItemViews[4] = findViewById(R.id.weekly_item_4);
        weeklyItemViews[5] = findViewById(R.id.weekly_item_5);
        weeklyItemViews[6] = findViewById(R.id.weekly_item_6);
        weeklyItemViews[7] = findViewById(R.id.weekly_item_7);
    }

    private void getAttrs(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.WeeklyItemLayout);
        setAttrs(typedArray);
    }

    private void getAttrs(AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.WeeklyItemLayout, defStyleAttr, 0);
        setAttrs(typedArray);
    }

    private void setAttrs(TypedArray typedArray) {
        weeklyItemViews[1].setVisibility(typedArray.getInt(R.styleable.WeeklyItemLayout_item1_visibility, View.VISIBLE));
        weeklyItemViews[2].setVisibility(typedArray.getInt(R.styleable.WeeklyItemLayout_item2_visibility, View.VISIBLE));
        weeklyItemViews[3].setVisibility(typedArray.getInt(R.styleable.WeeklyItemLayout_item3_visibility, View.VISIBLE));
        weeklyItemViews[4].setVisibility(typedArray.getInt(R.styleable.WeeklyItemLayout_item4_visibility, View.VISIBLE));
        weeklyItemViews[5].setVisibility(typedArray.getInt(R.styleable.WeeklyItemLayout_item5_visibility, View.VISIBLE));
        weeklyItemViews[6].setVisibility(typedArray.getInt(R.styleable.WeeklyItemLayout_item6_visibility, View.VISIBLE));
        weeklyItemViews[7].setVisibility(typedArray.getInt(R.styleable.WeeklyItemLayout_item7_visibility, View.VISIBLE));

        typedArray.recycle();
    }

    public void setWeeklyItemVisibility(int idx, int val) {
        try {
            if(val != View.VISIBLE && val != View.GONE && val != View.INVISIBLE) {
                throw new Exception("value is not visibility constant.");
            }

            if(idx > 7 || idx < 1) {
                throw new Exception("item index exceed.");
            }

            weeklyItemViews[idx].setVisibility(val);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDateText(int idx, String date) {
        try {
            if(idx > 7 || idx < 1) {
                throw new Exception("item index exceed.");
            }

            weeklyItemViews[idx].setDateText(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSkyImage(int idx, int dayResourceId, int nightResourceId) {
        try {
            if(idx > 7 || idx < 1) {
                throw new Exception("item index exceed.");
            }

            weeklyItemViews[idx].setSkyDayImage(dayResourceId);
            weeklyItemViews[idx].setSkyNightImage(nightResourceId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLowTemp(int idx, int i) {
        try {
            if(idx > 7 || idx < 1) {
                throw new Exception("item index exceed.");
            }

            weeklyItemViews[idx].setLowTemp(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLowTemp(int idx, String val) {
        try {
            if(idx > 7 || idx < 1) {
                throw new Exception("item index exceed.");
            }

            weeklyItemViews[idx].setLowTemp(val);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setHighTemp(int idx, int i) {
        try {
            if(idx > 7 || idx < 1) {
                throw new Exception("item index exceed.");
            }

            weeklyItemViews[idx].setHighTemp(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setHighTemp(int idx, String val) {
        try {
            if(idx > 7 || idx < 1) {
                throw new Exception("item index exceed.");
            }

            weeklyItemViews[idx].setHighTemp(val);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
