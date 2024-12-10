package com.volcengine.effectone.auto

import android.app.Application
import com.volcengine.effectone.auto.business.AutoQuickInitHelper

/**
 *Author: gaojin
 *Time: 2023/11/29 17:38
 */

class EffectAutoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AutoQuickInitHelper.initApplication(this)
    }
}