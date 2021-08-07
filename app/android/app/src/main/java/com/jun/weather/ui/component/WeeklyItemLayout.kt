package com.jun.weather.ui.component

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.jun.weather.R
import com.jun.weather.databinding.ComponentWeeklyLayoutBinding
import com.jun.weather.databinding.FragmentWeekWeatherBinding

class WeeklyItemLayout : ConstraintLayout {
    private lateinit var weeklyItemViews: Array<WeeklyItemView?>
    private lateinit var binding: ComponentWeeklyLayoutBinding

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
        getAttrs(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context)
        getAttrs(attrs, defStyleAttr)
    }

    private fun initView(context: Context) {
        val view = LayoutInflater.from(context).inflate(R.layout.component_weekly_layout, this, false)
        binding = ComponentWeeklyLayoutBinding.bind(view)
        addView(view)

        weeklyItemViews = arrayOfNulls(8)
        weeklyItemViews[1] = binding.weeklyItem1
        weeklyItemViews[2] = binding.weeklyItem2
        weeklyItemViews[3] = binding.weeklyItem3
        weeklyItemViews[4] = binding.weeklyItem4
        weeklyItemViews[5] = binding.weeklyItem5
        weeklyItemViews[6] = binding.weeklyItem6
        weeklyItemViews[7] = binding.weeklyItem7
    }

    private fun getAttrs(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.WeeklyItemLayout)
        setAttrs(typedArray)
    }

    private fun getAttrs(attrs: AttributeSet?, defStyleAttr: Int) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.WeeklyItemLayout, defStyleAttr, 0)
        setAttrs(typedArray)
    }

    private fun setAttrs(typedArray: TypedArray) {
        weeklyItemViews[1]!!.visibility = typedArray.getInt(R.styleable.WeeklyItemLayout_item1_visibility, VISIBLE)
        weeklyItemViews[2]!!.visibility = typedArray.getInt(R.styleable.WeeklyItemLayout_item2_visibility, VISIBLE)
        weeklyItemViews[3]!!.visibility = typedArray.getInt(R.styleable.WeeklyItemLayout_item3_visibility, VISIBLE)
        weeklyItemViews[4]!!.visibility = typedArray.getInt(R.styleable.WeeklyItemLayout_item4_visibility, VISIBLE)
        weeklyItemViews[5]!!.visibility = typedArray.getInt(R.styleable.WeeklyItemLayout_item5_visibility, VISIBLE)
        weeklyItemViews[6]!!.visibility = typedArray.getInt(R.styleable.WeeklyItemLayout_item6_visibility, VISIBLE)
        weeklyItemViews[7]!!.visibility = typedArray.getInt(R.styleable.WeeklyItemLayout_item7_visibility, VISIBLE)
        typedArray.recycle()
    }

    fun setWeeklyItemVisibility(idx: Int, `val`: Int) {
        try {
            if (`val` != VISIBLE && `val` != GONE && `val` != INVISIBLE) {
                throw Exception("value is not visibility constant.")
            }
            if (idx > 7 || idx < 1) {
                throw Exception("item index exceed.")
            }
            weeklyItemViews[idx]!!.visibility = `val`
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setDateText(idx: Int, date: String?) {
        try {
            if (idx > 7 || idx < 1) {
                throw Exception("item index exceed.")
            }
            weeklyItemViews[idx]!!.setDateText(date)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setSkyImage(idx: Int, dayResourceId: Int, nightResourceId: Int) {
        try {
            if (idx > 7 || idx < 1) {
                throw Exception("item index exceed.")
            }
            weeklyItemViews[idx]!!.setSkyDayImage(dayResourceId)
            weeklyItemViews[idx]!!.setSkyNightImage(nightResourceId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setTemp(idx: Int, low: String, high: String) {
        try {
            if (idx > 7 || idx < 1) {
                throw Exception("item index exceed.")
            }
            weeklyItemViews[idx]!!.setTemp(low, high)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}