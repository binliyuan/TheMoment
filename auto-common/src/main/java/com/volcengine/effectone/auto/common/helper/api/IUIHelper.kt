package com.volcengine.effectone.auto.common.helper.api

import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner

interface IUIHelper : LifecycleObserver {
    val activity: FragmentActivity
    val owner: LifecycleOwner
    fun initView(rootView: ViewGroup)
}