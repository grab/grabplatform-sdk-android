package com.grab.partner.sdk.sampleapp.bindings

import android.widget.TextView
import androidx.databinding.BindingAdapter
import android.text.method.MovementMethod

@BindingAdapter("scrollable")
fun setScrollable(textView: TextView, movementMethod: MovementMethod) {
    textView.movementMethod = movementMethod
}