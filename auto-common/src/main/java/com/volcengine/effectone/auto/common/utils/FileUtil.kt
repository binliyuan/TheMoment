package com.volcengine.effectone.auto.common.utils

import java.util.Formatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

object DurationFormatUtil {

    // {zh} 转换成字符串的时间 {en} Time to convert to string
    private val mFormatBuilder = StringBuilder()
    private val mFormatter = Formatter(
        mFormatBuilder,
        Locale.ENGLISH,
    )

    /** {zh}
     * 把毫秒转换成：1：20：30这样的形式
     * @param timeMs
     * @return
     */
    /** {en}
     * Convert milliseconds to: 1:20:30
     * @param timeMs
     * @return
     */
    fun stringForTime(timeMs: Long, minimumValue: Int = 1): String {
        val totalSeconds = (timeMs / 1_000F).roundToInt()
        val seconds = (totalSeconds % 60).coerceAtLeast(minimumValue)
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3_600
        mFormatBuilder.setLength(0)
        return if (hours > 0) {
            mFormatter.format(
                Locale.ENGLISH,
                "%d:%02d:%02d",
                hours,
                minutes,
                seconds,
            ).toString()
        } else {
            mFormatter.format(
                Locale.ENGLISH,
                "%02d:%02d",
                minutes,
                seconds,
            ).toString()
        }
    }

    fun getStringForTime(timeMs: Long): String {
        var timeMs = timeMs
        if (timeMs == -1L) {
            timeMs = 0
        }
        val prefix = if (timeMs < 0) "-" else ""
        timeMs = abs(timeMs.toDouble()).toLong()
        val totalSeconds = (timeMs + 500) / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        mFormatBuilder.setLength(0)
        return if (hours > 0) mFormatter.format(
            Locale.ENGLISH,
            "%s%d:%02d:%02d",
            prefix,
            hours,
            minutes,
            seconds
        )
            .toString() else mFormatter.format(
            Locale.ENGLISH,
            "%s%02d:%02d",
            prefix,
            minutes,
            seconds
        ).toString()
    }
}
