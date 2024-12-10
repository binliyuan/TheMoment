package com.volcengine.effectone.auto.algorithm.helper

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.imageview.ShapeableImageView
import com.volcengine.effectone.auto.algorithm.R
import com.volcengine.effectone.auto.algorithm.viewmodel.AlgorithmViewModel
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.recorder.viewmodel.AutoRecordUIViewModel
import com.volcengine.effectone.data.EditorSurfacePlaceHolder
import com.volcengine.effectone.recordersdk.arsdk.algorithm.tasks.ChildrenAlgorithmTask
import com.volcengine.effectone.recordersdk.arsdk.algorithm.tasks.LicenseCakeAlgorithmTask
import com.volcengine.effectone.recorderui.widget.EORecordButton
import com.volcengine.effectone.recorderui.widget.RecordMode
import com.volcengine.effectone.widget.EOModelSelectLayout

class AlgorithmDockerHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
) : IUIHelper {

    companion object {
        private const val TAG = "AlgorithmDockerHelper"
    }

    private val algorithmViewModel by lazy { AlgorithmViewModel.get(activity) }

    private val recordButtonHelper by lazy { AlgorithmRecordButtonHelper(activity, owner) }
//    private val recordVideoControlHelper by lazy { AutoRecordVideoControlHelper(activity, owner) }
//    private val recordDurationHelper by lazy { AutoRecordDurationHelper(activity, owner) }
    private val switchButtonHelper by lazy { AlgorithmSwitchButtonHelper(activity, owner) }
    private val cameraSwitchHelper by lazy { AlgorithmCameraSwitchHelper(activity, owner) }
    private val recordUIViewModel by lazy { AutoRecordUIViewModel.get(activity) }
    private val backButtonHelper by lazy { AlgorithmBackButtonHelper(activity, owner) }

    private val dockerComponentHelper by lazy {
        mutableListOf(
            recordButtonHelper,
//            recordVideoControlHelper,
//            recordDurationHelper,
            switchButtonHelper,
            cameraSwitchHelper,
            backButtonHelper
        )
    }

    private var leftDockerView: ViewGroup? = null
    override fun initView(rootView: ViewGroup) {
        dockerComponentHelper.forEach {
            owner.lifecycle.addObserver(it)
            it.initView(rootView)
        }
        leftDockerView = rootView.findViewById(com.volcengine.effectone.auto.recorder.R.id.auto_recorder_root_left_docker)

        if (leftDockerView != null) {
            val highlightMsg = leftDockerView!!.findViewById<LinearLayout>(com.volcengine.effectone.auto.recorder.R.id.auto_recorder_highlight_container)
            highlightMsg.visibility = LinearLayout.GONE

            val recordContainer = leftDockerView!!.findViewById<EOModelSelectLayout>(com.volcengine.effectone.auto.recorder.R.id.eo_recorder_model_select)
            recordContainer.visibility = LinearLayout.GONE

            val albumContainer = leftDockerView!!.findViewById<LinearLayout>(com.volcengine.effectone.auto.recorder.R.id.auto_recorder_beauty_drawer)
            albumContainer.visibility = LinearLayout.INVISIBLE

            val recordButton = leftDockerView!!.findViewById<EORecordButton>(com.volcengine.effectone.auto.recorder.R.id.auto_recorder_start_record)
            recordButton.changeRecordMode(RecordMode.PICTURE)

            val intent = activity.intent
            val algoKey = intent.getStringExtra("algoKey")
            replaceIcon(algoKey)
        }
        addActivityCallBack()
        addFirstFrameBitmapListener()
    }

    private fun replaceIcon(algoKey: String?) {
        val textString = when(algoKey) {
            LicenseCakeAlgorithmTask.LICENSE_CAKE.key -> R.string.algorithm_license_cake
            ChildrenAlgorithmTask.CHILDREN_DETECTION.key -> R.string.algorithm_child_detect
            else -> null
        }
        val recordText = leftDockerView!!.findViewById<TextView>(com.volcengine.effectone.auto.recorder.R.id.auto_recorder_album_text)
        recordText.text = textString?.let { activity.getString(it) }

        val iconDrawable = when(algoKey) {
            LicenseCakeAlgorithmTask.LICENSE_CAKE.key -> R.drawable.algorithm_car_icon
            ChildrenAlgorithmTask.CHILDREN_DETECTION.key -> R.drawable.algorithm_child_icon
            else -> null
        }
        val recordIcon = leftDockerView!!.findViewById<ShapeableImageView>(com.volcengine.effectone.auto.recorder.R.id.auto_recorder_album_icon)
        recordIcon.setImageDrawable(iconDrawable?.let {
            AppCompatResources.getDrawable(activity, it)
        })
    }

    private fun addActivityCallBack() {
        activity.onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                algorithmViewModel.existRecordPage(activity, dialogShowAction = {
                    recordUIViewModel.rootViewVisible.value = false
                }) {
                    recordUIViewModel.rootViewVisible.value = true
                }
            }
        })
    }

    private fun addFirstFrameBitmapListener() {
        algorithmViewModel.recordManager.firstFrameCallback {
            EditorSurfacePlaceHolder.setPlaceHolder(it)
        }
    }

    fun showView(showView: Boolean) {
        leftDockerView?.run {
            visibility = if (showView) View.VISIBLE else View.GONE
            alpha = 1f
        }
    }
}