package com.volcengine.effectone.auto.recorder.hl

import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cutsame.solution.template.model.TemplateItem
import com.ss.android.ugc.aweme.views.setDebounceOnClickListener
import com.volcengine.auth.api.EOAuthorizationInternal
import com.volcengine.ck.highlight.config.HLResultByMedia
import com.volcengine.ck.highlight.ila.ILASDKInit
import com.volcengine.ck.logkit.LogKit
import com.volcengine.effectone.InnerEffectOneConfigList
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.common.widget.AutoLoadingDialog
import com.volcengine.effectone.auto.moment.utils.toMediaItem
import com.volcengine.effectone.auto.recorder.AutoCameraViewModel
import com.volcengine.effectone.auto.recorder.R
import com.volcengine.effectone.auto.recorder.base.AutoRecordScope
import com.volcengine.effectone.auto.recorder.base.EO_RECORD_MODEL_HIGH_LIGHT
import com.volcengine.effectone.auto.recorder.config.AutoRecordConstant
import com.volcengine.effectone.auto.recorder.hl.list.AutoHLRecordVideoListAdapter
import com.volcengine.effectone.auto.recorder.hl.list.HLVideoItemDecoration
import com.volcengine.effectone.auto.recorder.hl.utils.toRecognizeMedia
import com.volcengine.effectone.auto.recorder.viewmodel.AutoRecordUIViewModel
import com.volcengine.effectone.auto.templates.bean.TemplateByMedias
import com.volcengine.effectone.auto.templates.cutsame.TemplateConfig
import com.volcengine.effectone.auto.templates.ui.AutoMomentCutSameActivity
import com.volcengine.effectone.recordersdk.base.RecordAction.STOP_RECORD
import com.volcengine.effectone.recorderui.base.EO_RECORD_MODEL_30S
import com.volcengine.effectone.recorderui.base.EO_RECORD_MODEL_PHOTO
import com.volcengine.effectone.singleton.AppSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *Author: gaojin
 *Time: 2024/5/28 22:19
 */

class AutoRecordHLVideoListHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
) : IUIHelper {

    private val mTemplateConfig: TemplateConfig? = InnerEffectOneConfigList.getConfig()
    private val autoRecordScope = AutoRecordScope()

    private val cameraViewModel by lazy { AutoCameraViewModel.get(activity) }
    private val recordUIViewModel by lazy { AutoRecordUIViewModel.get(activity) }
    private val autoHLRecordViewModel by lazy { AutoHLRecordViewModel.get(activity) }

    private var videoListContainer: View? = null
    private var toHLM: View? = null
    private var videoListView: RecyclerView? = null

    private val loadingDialog by lazy {
        AutoLoadingDialog.Builder(activity).setTipMsg("匹配中...").create()
    }

    private val hlAdapter = AutoHLRecordVideoListAdapter()


    override fun initView(rootView: ViewGroup) {
        videoListContainer = rootView.findViewById(R.id.auto_recorder_highlight_video_list_container)

        videoListView = rootView.findViewById(R.id.auto_record_hl_video_list_view)

        toHLM = rootView.findViewById(R.id.auto_record_to_hlm)

        toHLM?.setDebounceOnClickListener {
            val cutSameLicensePath = EOAuthorizationInternal.getCutSameLicensePath()
            if (cutSameLicensePath == null) {
                Toast.makeText(AppSingleton.instance, "剪同款鉴权失败", Toast.LENGTH_SHORT).show()
            } else {
                if (cameraViewModel.isRecording()) {
                    cameraViewModel.recordManager.stopRecord {
                        recordUIViewModel.stopRecord.value = Unit
                        toHLMPage()
                    }
                } else {
                    toHLMPage()
                }
            }
        }

        videoListView?.run {
            adapter = hlAdapter
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
            addItemDecoration(HLVideoItemDecoration())
        }

        recordUIViewModel.recordModelLiveData.observe(owner) {
            it?.let { modelItem ->
                when (modelItem.id) {
                    EO_RECORD_MODEL_PHOTO -> {
                        videoListContainer?.visibility = View.GONE
                    }

                    EO_RECORD_MODEL_30S -> {
                        videoListContainer?.visibility = View.GONE
                    }

                    EO_RECORD_MODEL_HIGH_LIGHT -> {
                    }
                }
            }
        }

        cameraViewModel.recordAction.observe(owner) {
            it?.let {
                if (it == STOP_RECORD) {
                    videoListChanged()
                }
            }
        }
        cameraViewModel.deleteAllVideoEvent.observe(owner) {
            videoListChanged()
        }
    }

    private fun videoListChanged() {
        if (!recordUIViewModel.isHighLight) {
            return
        }
        val videoList = cameraViewModel.recordManager.getRecordedVideoPath()
        if (videoList.isEmpty()) {
            videoListContainer?.visibility = View.GONE
        } else {
            videoListContainer?.visibility = View.VISIBLE
            hlAdapter.updateItems(videoList)
            videoListView?.smoothScrollToPosition(videoList.size - 1)
        }
    }

    private fun toHLMPage() {
        autoRecordScope.launch {
            loadingDialog.show()
            val pathByHLResultList = autoHLRecordViewModel.groupHLResultByPath()
            val recordedVideo = cameraViewModel.recordManager.getRecordedVideoPath()

            LogKit.i(AutoRecordConstant.HL_RECORD_TAG, pathByHLResultList.toString())
            LogKit.i(AutoRecordConstant.HL_RECORD_TAG, cameraViewModel.recordManager.getRecordedVideoPath().joinToString("\n"))

            val hlResultByMediaList = mutableListOf<HLResultByMedia>()
            recordedVideo.forEach {
                val hlResults = pathByHLResultList[it.path] ?: emptyList()
                if (hlResults.isNotEmpty()) {
                    val recognizeMedia = it.toRecognizeMedia()
                    val hlResultByMedia = HLResultByMedia(recognizeMedia, hlResults)
                    hlResultByMediaList.add(hlResultByMedia)
                } else {
                    LogKit.i(AutoRecordConstant.HL_RECORD_TAG, "${it.path} hlResults is null")
                }
            }

            if (hlResultByMediaList.isEmpty()) {
                Toast.makeText(AppSingleton.instance, "数据错误", Toast.LENGTH_SHORT).show()
                loadingDialog.dismiss()
                return@launch
            }

            LogKit.i(AutoRecordConstant.HL_RECORD_TAG, "final hlResultByMediaList size:${hlResultByMediaList.size}")

            val matchedInfo = withContext(Dispatchers.IO) {
                ILASDKInit.getTemplateFinder().findSuitedTemplate(
                    hlResultByMediaList,
                    ILASDKInit.getTemplateProvider()
                )
            }
            if (matchedInfo.isEmpty()) {
                Toast.makeText(AppSingleton.instance, "未匹配到模板", Toast.LENGTH_SHORT).show()
                loadingDialog.dismiss()
                return@launch
            } else {
                val data = arrayListOf<TemplateByMedias>()
                matchedInfo.forEach { info ->
                    val templateItem = info.template.any as? TemplateItem
                    if (templateItem != null) {
                        withContext(Dispatchers.IO) {
                            val result = mTemplateConfig!!.loadTemplateResource(templateItem)
                            data.add(TemplateByMedias(result, info.mediaList.map { it.toMediaItem() }))
                        }
                    }
                }
                loadingDialog.dismiss()
                if (data.isNotEmpty()) {
                    val intent = AutoMomentCutSameActivity.createIntent(activity, data)
                    activity.startActivity(intent)
                } else {
                    Toast.makeText(AppSingleton.instance, "模板配置错误", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}