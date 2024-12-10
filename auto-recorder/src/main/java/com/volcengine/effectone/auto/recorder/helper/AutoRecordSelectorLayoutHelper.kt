package com.volcengine.effectone.auto.recorder.helper

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.volcengine.effectone.api.EOModelChangeLayoutListener
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.recorder.AutoCameraViewModel
import com.volcengine.effectone.auto.recorder.base.RecordHighlightModel
import com.volcengine.effectone.auto.recorder.viewmodel.AutoRecordUIViewModel
import com.volcengine.effectone.recordersdk.base.RecordAction.START_RECORD
import com.volcengine.effectone.recorderui.R
import com.volcengine.effectone.recorderui.base.Record30SModel
import com.volcengine.effectone.recorderui.base.RecordPhotoModel
import com.volcengine.effectone.widget.EOModelSelectLayout
import com.volcengine.effectone.widget.IEOModelItem

/**
 *Author: gaojin
 *Time: 2023/12/12 14:12
 */

class AutoRecordSelectorLayoutHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
) : IUIHelper {

    private val recordUIViewModel by lazy { AutoRecordUIViewModel.get(activity) }
    private val cameraViewModel by lazy { AutoCameraViewModel.get(activity) }

    private var modelSelectLayout: EOModelSelectLayout? = null
    private var modelNeedHide: Boolean = false

    override fun initView(rootView: ViewGroup) {
        modelSelectLayout = rootView.findViewById<EOModelSelectLayout>(R.id.eo_recorder_model_select).apply {
            init(
                listOf(
                    RecordPhotoModel(), Record30SModel(), RecordHighlightModel()
                )
            )
            setOnModelChangedListener(object : EOModelChangeLayoutListener {
                override fun onModelChanged(item: IEOModelItem) {
                    recordUIViewModel.recordModelLiveData.value = item
                }
            })
            if (cameraViewModel.isHLShoot()) {
                setCurrentItem(2)
            } else {
                setCurrentItem(1)
            }
        }

        cameraViewModel.recordAction.observe(owner) {
            it?.let { action ->
                if (action == START_RECORD) {
                    modelSelectLayout?.visibility = View.GONE
                    modelNeedHide = true
                } else {
                    if (!cameraViewModel.isRecordListEmpty()) {
                        modelSelectLayout?.visibility = View.GONE
                        modelNeedHide = true
                    } else {
                        modelSelectLayout?.visibility = View.VISIBLE
                        modelNeedHide = false
                    }
                }
            }
        }
        recordUIViewModel.selectShot30SModel.observe(owner) {
            modelSelectLayout?.setCurrentItem(1)
        }
    }
}