package com.jun.weather.util

import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import org.jetbrains.annotations.Nullable

object BindingUtils {
    @BindingAdapter("loadRes")
    @JvmStatic fun loadRes(v: ImageView, @DrawableRes resId: Int?) {
        resId?.let {
            Glide.with(v.context).load(resId).into(v)
        }
    }
}