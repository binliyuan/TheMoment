package com.volcengine.effectone.auto.recorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.volcengine.auth.api.EOAuthorizationInternal
import com.volcengine.effectone.EffectOneSdk
import com.volcengine.effectone.auto.recorder.base.AutoRecordConfig
import com.volcengine.effectone.auto.recorder.config.AutoRecordConstant
import com.volcengine.effectone.auto.recorder.helper.AutoRecordBeautyButtonHelper
import com.volcengine.effectone.auto.recorder.helper.AutoRecordButtonHelper
import com.volcengine.effectone.auto.recorder.helper.AutoRecordCameraSwitchHelper
import com.volcengine.effectone.auto.recorder.helper.AutoRecordClearViewHelper
import com.volcengine.effectone.auto.recorder.helper.AutoRecordDrawerHelper
import com.volcengine.effectone.auto.recorder.helper.AutoRecordDurationHelper
import com.volcengine.effectone.auto.recorder.helper.AutoRecordFocusLayoutHelper
import com.volcengine.effectone.auto.recorder.helper.AutoRecordLeftDockerHelper
import com.volcengine.effectone.auto.recorder.helper.AutoRecordOneShotFlashHelper
import com.volcengine.effectone.auto.recorder.helper.AutoRecordSelectorLayoutHelper
import com.volcengine.effectone.auto.recorder.helper.AutoRecordVideoControlHelper
import com.volcengine.effectone.auto.recorder.helper.AutoRecorderAlbumButtonHelper
import com.volcengine.effectone.auto.recorder.helper.AutoRecorderBackButtonHelper
import com.volcengine.effectone.auto.recorder.hl.AutoHLRecordViewHelper
import com.volcengine.effectone.auto.recorder.hl.AutoRecordHLVideoListHelper
import com.volcengine.effectone.filter.FilterViewModel
import com.volcengine.effectone.recordersdk.base.EORecorderSDKConfig
import com.volcengine.effectone.recorderui.beauty.BeautyViewModel
import com.volcengine.effectone.recorderui.viewmodel.RecoderEventTrackViewModel
import com.volcengine.effectone.sticker.EOBaseStickerViewModel
import com.volcengine.effectone.utils.runOnUiThread

/**
 *Author: gaojin
 *Time: 2024/4/16 17:00
 */

class AutoRecordFragment : Fragment() {

    companion object {
        private const val TAG = "AutoRecordFragment"
    }

    private val cameraViewModel by lazy { AutoCameraViewModel.get(requireActivity()) }
    private val filterViewModel by lazy { FilterViewModel.get(requireActivity()) }
    private val beautyViewModel by lazy { BeautyViewModel.get(requireActivity()) }
    private val recorderEventTrackViewModel by lazy { RecoderEventTrackViewModel.get(requireActivity()) }
    private val recorderStickerViewModel by lazy { EOBaseStickerViewModel.get(requireActivity()) }

    private val viewHelpers by lazy {
        mutableListOf(
            //Drawer右侧抽屉管理
            AutoRecordDrawerHelper(requireActivity(), viewLifecycleOwner),

            AutoRecordLeftDockerHelper(requireActivity(), viewLifecycleOwner),
            AutoRecordClearViewHelper(requireActivity(), viewLifecycleOwner),
            AutoRecordFocusLayoutHelper(requireActivity(), viewLifecycleOwner),
            AutoHLRecordViewHelper(requireActivity(), viewLifecycleOwner),

            AutoRecordButtonHelper(requireActivity(), viewLifecycleOwner),
            AutoRecordVideoControlHelper(requireActivity(), viewLifecycleOwner),
            AutoRecordDurationHelper(requireActivity(), viewLifecycleOwner),
            AutoRecordDurationHelper(requireActivity(), viewLifecycleOwner),
            AutoRecordSelectorLayoutHelper(requireActivity(), viewLifecycleOwner),
            AutoRecordBeautyButtonHelper(requireActivity(), viewLifecycleOwner),
            AutoRecordBeautyButtonHelper(requireActivity(), viewLifecycleOwner),
            AutoRecordCameraSwitchHelper(requireActivity(), viewLifecycleOwner),
            AutoRecorderBackButtonHelper(requireActivity(), viewLifecycleOwner),
            //高光拍摄视频列表
            AutoRecordHLVideoListHelper(requireActivity(), viewLifecycleOwner),
            //相册按钮的逻辑
            AutoRecorderAlbumButtonHelper(requireActivity(), viewLifecycleOwner),
            //拍照闪屏
            AutoRecordOneShotFlashHelper(requireActivity(), viewLifecycleOwner),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraViewModel.injectLifeCycle(lifecycle)
        arguments?.let {
            val type = it.getInt(AutoRecordConstant.TYPE_KEY, AutoRecordConstant.ORDINARY_SHOOT)
            cameraViewModel.type = type
        }
        cameraViewModel.clearWorkSpace()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.auto_recorder_root_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewHelpers.forEach {
            requireActivity().lifecycle.addObserver(it)
        }
        super.onViewCreated(view, savedInstanceState)
        val container = view.findViewById<ViewGroup>(R.id.auto_record_root).apply {
            addView(LayoutInflater.from(activity).inflate(R.layout.auto_recorder_layout_root_content, this, false))
        }

        val width = AutoRecordConfig.widthRatio
        val height = AutoRecordConfig.heightRatio

        val surfaceContainer = container.findViewById<FrameLayout>(R.id.auto_surface_view_container)
        if (width != 0 && height != 0) {
            val params = surfaceContainer.layoutParams as ConstraintLayout.LayoutParams
            params.dimensionRatio = "${width}:${height}"
            surfaceContainer.layoutParams = params
        }

        val recorderSDKConfig = EORecorderSDKConfig().apply {
            surfaceViewContainer = surfaceContainer
            defaultFrontCamera = true
            ratioSize = width.toFloat() / height.toFloat()
            defaultResolution = 1080
            rotation = 270
            modelPath = EffectOneSdk.modelPath
            licensePath = EOAuthorizationInternal.getVELicensePath() ?: ""
            licenseOnline = EOAuthorizationInternal.licenseLoadOnline()
        }
        cameraViewModel.recordManager.initRecorder(recorderSDKConfig)
        //应用默认美颜
        cameraViewModel.setBeauty(beautyViewModel.defaultSelectedComposerItem())


        viewHelpers.forEach {
            it.initView(view as ViewGroup)
        }
        initObserver()
    }

    override fun onResume() {
        super.onResume()
        cameraViewModel.recordManager.onCameraOpen {
            runOnUiThread {
                cameraViewModel.cameraOpenResult.value = true
                cameraViewModel.resetComposerNodes()
            }
        }
    }

    private fun initObserver() {
        recorderEventTrackViewModel.initObserver(viewLifecycleOwner)

        filterViewModel.filterChanged.observe(viewLifecycleOwner) {
            it?.let { filterItem ->
                cameraViewModel.setFilter(filterItem)
            }
        }

        filterViewModel.filterIntensity.observe(viewLifecycleOwner) {
            it?.let { filterIntensity ->
                cameraViewModel.updateFilterIntensity(filterIntensity.intensity)
            }
        }

        beautyViewModel.beautyChangedEvent.observe(viewLifecycleOwner) {
            cameraViewModel.setBeauty(beautyViewModel.buildComposerList())
        }

        beautyViewModel.updateComposerIntensityEvent.observe(viewLifecycleOwner) {
            it?.let { item ->
                cameraViewModel.updateBeauty(item)
            }
        }

        recorderStickerViewModel.selectedItem.observe(viewLifecycleOwner) {
            it?.let {
                val stickerPath = it.resource.absPath
                cameraViewModel.setSticker(stickerPath)
                if (stickerPath.isEmpty()) {
                    beautyViewModel.updateBeautyConflictWithSticker(true)
                } else {
                    beautyViewModel.updateBeautyConflictWithSticker(false)
                }
                beautyViewModel.beautyChangedEvent.value = Unit
                beautyViewModel.triggerBeautySideBarState()
            }
        }
    }
}