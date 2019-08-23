package com.github.luoyemyy.image.picker.gallery

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.github.luoyemyy.image.R


object BindingAdapter {

    @JvmStatic
    @BindingAdapter("image_url")
    fun image(imageView: ImageView, path: String?) {
        Glide.with(imageView).load(path).error(R.drawable.image_ic_error_image).into(imageView)
    }
}