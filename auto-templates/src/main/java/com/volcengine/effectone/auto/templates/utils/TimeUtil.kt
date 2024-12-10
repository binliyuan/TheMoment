package com.volcengine.effectone.auto.templates.utils

import android.annotation.SuppressLint
import java.util.Formatter
import java.util.Locale

object TimeUtil {

    private val mFormatBuilder = StringBuilder()

    @SuppressLint("ConstantLocale")
    private val mFormatter = Formatter(mFormatBuilder, Locale.getDefault())

    fun stringForTime(timeMs: Long): String {
        val totalSeconds = timeMs / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        mFormatBuilder.setLength(0)
        return if (hours > 0) {
            mFormatter.format(
                "%d:%02d:%02d",
                hours,
                minutes,
                seconds
            ).toString()
        } else {
            mFormatter.format("%02d:%02d", minutes, seconds)
                .toString()
        }
    }
}