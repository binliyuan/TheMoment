package com.volcengine.effectone.auto.templates.utils

import android.content.Context
import android.preference.PreferenceManager
import java.util.*


enum class Environment {
    PRODUCT_CN,
    PRODUCT_OVER_SEA
}

object ApiUtil {
    var ENV: Environment =
        if (Locale.getDefault().language == "zh" && Locale.getDefault().country == "CN") Environment.PRODUCT_CN else Environment.PRODUCT_OVER_SEA

    val host = when (ENV) {
        Environment.PRODUCT_CN -> "http://common.voleai.com"
        Environment.PRODUCT_OVER_SEA -> "http://ck-common.byteintl.com"
    }

    private val extra_param_order_id = when (ENV) {
        Environment.PRODUCT_CN -> "7117128998756794382"
        Environment.PRODUCT_OVER_SEA -> "714507345"
    }

    fun getOrderId(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(ORDER_ID_KEY, extra_param_order_id) ?: extra_param_order_id
    }

    private const val ORDER_ID_KEY = "eo_cutsame_order_id"
}