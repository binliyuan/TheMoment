package com.volcengine.effectone.auto.algorithm.helper

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.imageview.ShapeableImageView
import com.volcengine.effectone.auto.algorithm.viewmodel.AlgorithmViewModel
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.recorder.R
import com.volcengine.effectone.extensions.setNoDoubleClickListener
import com.volcengine.effectone.utils.SizeUtil

class AlgorithmSwitchButtonHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
) : IUIHelper {
    companion object {
        private const val TAG = "AlgorithmSwitchButtonHelper"
    }
    private val cameraViewModel by lazy { AlgorithmViewModel.get(activity) }
    private var albumButton: View? = null
    private var mAlgorithmOn = true
    private var albumIcon: ShapeableImageView? = null

    override fun initView(rootView: ViewGroup) {
        albumButton = rootView.findViewById(R.id.auto_recorder_album_container)
        albumIcon =  rootView.findViewById<ShapeableImageView?>(R.id.auto_recorder_album_icon)?.apply {
            strokeWidth = SizeUtil.dp2px(1f).toFloat()
            val padding = SizeUtil.dp2px(1f)
            setPadding(padding, padding, padding, padding)
            post { updateAlbumButtonStroke(rootView) }
        }
        albumButton?.setNoDoubleClickListener {
            mAlgorithmOn = !mAlgorithmOn
            cameraViewModel.setAlgorithmOn(mAlgorithmOn)
            if (mAlgorithmOn) {
                it.alpha = 1.0f
            } else {
                it.alpha = 0.5f
            }
            updateAlbumButtonStroke(rootView)
        }
    }

    private fun updateAlbumButtonStroke(rootView: ViewGroup) {
        albumIcon?.strokeColor = if (mAlgorithmOn) ContextCompat.getColorStateList(
            rootView.context,
            com.volcengine.effectone.auto.algorithm.R.color.color_FF53BB
        ) else null
    }
}