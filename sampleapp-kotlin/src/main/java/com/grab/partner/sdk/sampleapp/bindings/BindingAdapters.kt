package com.grab.partner.sdk.sampleapp.bindings

import android.widget.TextView
import android.databinding.BindingAdapter
import android.text.method.MovementMethod

@BindingAdapter("app:scrollable")
fun setScrollable(textView: TextView, movementMethod: MovementMethod) {
    textView.movementMethod = movementMethod
}