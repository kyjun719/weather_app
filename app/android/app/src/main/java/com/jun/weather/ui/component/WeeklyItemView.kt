package com.jun.weather.ui.component

import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.jun.weather.R
import com.jun.weather.databinding.ComponentWeeklyItemBinding
import java.util.*

class WeeklyItemView : ConstraintLayout {
    private lateinit var binding: ComponentWeeklyItemBinding

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
        val view = LayoutInflater.from(context).inflate(R.layout.component_weekly_item, this, false)
        addView(view)
        binding = ComponentWeeklyItemBinding.bind(view)
    }

    private fun getAttrs(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.WeeklyItemView)
        setTypeArray(typedArray)
    }

    private fun getAttrs(attrs: AttributeSet?, defStyleAttr: Int) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.WeeklyItemView, defStyleAttr, 0)
        setTypeArray(typedArray)
    }

    private fun setTypeArray(typedArray: TypedArray) {
        binding.textDate.text = typedArray.getString(R.styleable.WeeklyItemView_date)
        binding.imageSkyDay.setImageResource(typedArray.getResourceId(R.styleable.WeeklyItemView_day_sky_image, 0))
        binding.imageSkyNight.setImageResource(typedArray.getResourceId(R.styleable.WeeklyItemView_night_sky_image, 0))
//        binding.textLowTemp.text = typedArray.getString(R.styleable.WeeklyItemView_low_temp)
//        binding.textHighTemp.text = typedArray.getString(R.styleable.WeeklyItemView_high_temp)


        typedArray.recycle()
    }

    fun setDateText(s: String?) {
        binding.textDate.text = s
    }

    fun setSkyDayImage(resourceId: Int) {
        binding.imageSkyDay.setImageResource(resourceId)
    }

    fun setSkyNightImage(resourceId: Int) {
        binding.imageSkyNight.setImageResource(resourceId)
    }

    fun setTemp(low: Int, high: Int) {
        setTemp(low.toString(), high.toString())
    }

    fun setTemp(low: String, high: String) {
        val sp =  SpannableString(String.format(Locale.getDefault(), resources.getString(R.string.week_temp2), low,high))
        sp.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this.context, R.color.colorAccentBlue)),
                0,sp.indexOf("/"),Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )
        sp.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this.context, R.color.colorAccentRed)),
                sp.indexOf("/"),sp.length,Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )
    }
}