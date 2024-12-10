package com.volcengine.effectone.auto.algorithm.helper

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.gyf.immersionbar.ImmersionBar
import com.volcengine.auth.api.EOAuthorizationInternal
import com.volcengine.ck.logkit.LogKit
import com.volcengine.effectone.auto.algorithm.viewmodel.AlgorithmViewModel
import com.volcengine.effectone.auto.algorithm.R
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.recorder.viewmodel.AutoRecordUIViewModel
import com.volcengine.effectone.recordersdk.arsdk.algorithm.base.AlgorithmConfig
import com.volcengine.effectone.recordersdk.base.EORecorderSDKConfig
import com.volcengine.effectone.singleton.AppSingleton
import com.volcengine.effectone.utils.EOUtils
import com.volcengine.effectone.utils.runOnUiThread

class AlgorithmContentViewHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
): IUIHelper {

    companion object {
        private const val TAG = "AlgorithmContentViewHelper"
    }

    private val algorithmViewModel by lazy { AlgorithmViewModel.get(activity) }
    private val recordUIViewModel by lazy { AutoRecordUIViewModel.get(activity) }

    private val algorithmDockerHelper by lazy { AlgorithmDockerHelper(activity, owner) }
    private val focusLayoutHelper by lazy { AlgorithmFocusLayoutHelper(activity, owner) }

    private val contentViewHelper by lazy {
        mutableListOf(
            //左侧区域
            algorithmDockerHelper,
            //缩放
            focusLayoutHelper
        )
    }

    override fun initView(rootView: ViewGroup) {
        contentViewHelper.forEach {
            owner.lifecycle.addObserver(it)
        }
        val container = rootView.findViewById<ViewGroup>(com.volcengine.effectone.auto.recorder.R.id.auto_record_root).apply {
            addView(LayoutInflater.from(activity).inflate(com.volcengine.effectone.auto.recorder.R.layout.auto_recorder_layout_root_content, this, false))
        }

        val navigationBarHeight = ImmersionBar.getNavigationBarHeight(activity).toFloat()
        val isNavigationAtBottom = ImmersionBar.isNavigationAtBottom(activity)
        val statusBarHeight = ImmersionBar.getStatusBarHeight(activity)
        val screenHeight = EOUtils.sizeUtil.getScreenHeight(AppSingleton.instance).toFloat()
        val screenWidth = EOUtils.sizeUtil.getScreenWidth(AppSingleton.instance).toFloat()

        val surfaceRatioSize = if (isNavigationAtBottom) {
            screenWidth / (screenHeight - navigationBarHeight - statusBarHeight)
        } else {
            (screenWidth - navigationBarHeight) / (screenHeight - statusBarHeight)
        }

        LogKit.i(TAG, "navigationBarHeight: $navigationBarHeight")
        LogKit.i(TAG, "statusBarHeight: $statusBarHeight")
        LogKit.i(TAG, "screenHeight: $screenHeight")
        LogKit.i(TAG, "screenWidth: $screenWidth")
        LogKit.i(TAG, "surfaceRatioSize: $surfaceRatioSize")

        val surfaceContainer = container.findViewById<FrameLayout>(com.volcengine.effectone.auto.recorder.R.id.auto_surface_view_container)

        container.post {
            LogKit.i(TAG, "container measuredWidth: ${surfaceContainer.measuredWidth}")
            LogKit.i(TAG, "container measuredHeight: ${surfaceContainer.measuredHeight}")
            LogKit.i(
                TAG,
                "container ratio size: ${surfaceContainer.measuredWidth / surfaceContainer.measuredHeight.toFloat()}"
            )
        }

        val intent = activity.intent
        val algoKey = intent.getStringExtra("algoKey")
        val recorderSDKConfig = EORecorderSDKConfig().apply {
            surfaceViewContainer = surfaceContainer
            defaultFrontCamera = true
            ratioSize = surfaceRatioSize
            defaultResolution = 720
            rotation = 270
            licensePath = EOAuthorizationInternal.getVELicensePath() ?: ""
            licenseOnline = EOAuthorizationInternal.licenseLoadOnline()
            sdkType = EORecorderSDKConfig.EOSDKType.ALGO_KIT
            algoConfig = AlgorithmConfig(algoKey, mapOf(algoKey to true))
        }
        algorithmViewModel.recordManager.initRecorder(recorderSDKConfig)

        contentViewHelper.forEach {
            it.initView(rootView)
        }
        initObserver()
    }

    private fun initObserver() {
        recordUIViewModel.rootViewVisible.observe(owner) {
            it?.let { show ->
                if (show) {
                    algorithmDockerHelper.showView(true)
                } else {
                    algorithmDockerHelper.showView(false)
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        algorithmViewModel.recordManager.onCameraOpen {
            runOnUiThread {
                algorithmViewModel.cameraOpenResult.value = true
                algorithmViewModel.resetComposerNodes()
            }
        }
    }
}