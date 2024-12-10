package com.volcengine.effectone.auto.recorder.helper

import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.recorder.AutoCameraViewModel
import com.volcengine.effectone.auto.recorder.R
import com.volcengine.effectone.recorderui.widget.EORecordFocusLayout
import com.volcengine.effectone.recorderui.widget.OnExposureListener
import com.volcengine.effectone.recorderui.widget.OnFocusEnable
import com.volcengine.effectone.recorderui.widget.OnZoomListener

open class AutoRecordFocusLayoutHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
) : IUIHelper {
    protected open val cameraViewModel by lazy { AutoCameraViewModel.get(activity) }
    private var focusLayout :EORecordFocusLayout? = null
    override fun initView(rootView: ViewGroup) {
        focusLayout = rootView.findViewById<EORecordFocusLayout>(R.id.auto_recorder_exposure_layout).apply {
            bringToFront()
            setOnZoomListener(object : OnZoomListener {
                override fun zoom(value: Float) {
                    cameraViewModel.recordManager.setZoomValue(value)
                }

                override fun setFocusValue(
                    x: Float,
                    y: Float,
                    viewWidth: Int,
                    viewHeight: Int,
                    displayDensity: Float
                ) {
                    cameraViewModel.recordManager.setFocusValue(x,y,viewWidth,viewHeight,displayDensity)
                }

            })
            setOnFocusEnable(object : OnFocusEnable {
                override fun focusEnable(): Boolean {
                    return true
                }
            })

            setExposureListener(object : OnExposureListener {
                override fun setExposureValue(value: Int) {
                    cameraViewModel.recordManager.setExposureValue(value)
                }
            })
        }
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume(){
        focusLayout?.resetValues()
    }
}
