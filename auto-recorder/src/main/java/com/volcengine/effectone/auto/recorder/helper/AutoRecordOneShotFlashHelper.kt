package com.volcengine.effectone.auto.recorder.helper

import android.graphics.Rect
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.common.widget.AutoOneShotOverlay
import com.volcengine.effectone.auto.recorder.AutoCameraViewModel
import com.volcengine.effectone.auto.recorder.R
import com.volcengine.effectone.auto.recorder.viewmodel.AutoRecordUIViewModel

class AutoRecordOneShotFlashHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
) : IUIHelper {
    private val cameraViewModel by lazy { AutoCameraViewModel.get(activity) }
    private val recordUIViewModel by lazy { AutoRecordUIViewModel.get(activity) }

    private val autoOneShotOverlay by lazy { AutoOneShotOverlay(activity) }
    private val previewRegion = Rect()
    override fun initView(rootView: ViewGroup) {
        rootView.findViewById<FrameLayout>(R.id.auto_surface_view_container)?.apply {
            (autoOneShotOverlay.parent as? ViewGroup)?.removeView(autoOneShotOverlay)
            addView(autoOneShotOverlay,FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT)
            addOnLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                if(left!=oldLeft || top!=oldTop || right !=oldRight || bottom !=oldBottom){
                    previewRegion.set(left,top,right,bottom)
                    autoOneShotOverlay.updateFlashRegion(previewRegion)
                }
            }
        }
        initObserver()
    }

    private fun initObserver() {
        cameraViewModel.concatVideoStateWithResult.observe(owner){
            it?.let {
                if (it.first.not() && recordUIViewModel.isPhotoMode) {
                    autoOneShotOverlay.startFlashAnimation()
                }
            }
        }
    }

}
