package com.volcengine.effectone.auto.moment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cutsame.solution.template.model.TemplateItem
import com.volcengine.ck.highlight.data.HLTemplateMatchedInfo
import com.volcengine.ck.highlight.data.RecognizeMedia
import com.volcengine.ck.highlight.ila.MomentScope
import com.volcengine.ck.moment.ILAMomentSDK
import com.volcengine.ck.moment.base.CKMomentTemplate
import com.volcengine.ck.moment.base.WrapperCKMomentStateListener
import com.volcengine.effectone.InnerEffectOneConfigList
import com.volcengine.effectone.auto.common.widget.AutoLoadingDialog
import com.volcengine.effectone.auto.common.widget.GridItemDecoration
import com.volcengine.effectone.auto.moment.configure.Constants.MOMENT_CONFIGURE_ROOT_PATH
import com.volcengine.effectone.auto.moment.configure.MomentConfigureHelper
import com.volcengine.effectone.auto.moment.list.MomentListAdapter
import com.volcengine.effectone.auto.moment.utils.queryMedias
import com.volcengine.effectone.auto.moment.utils.toMediaItem
import com.volcengine.effectone.auto.templates.bean.TemplateByMedias
import com.volcengine.effectone.auto.templates.cutsame.TemplateConfig
import com.volcengine.effectone.auto.templates.ui.AutoMomentCutSameActivity
import com.volcengine.effectone.singleton.AppSingleton
import com.volcengine.effectone.utils.SizeUtil
import com.volcengine.effectone.utils.inflate
import com.volcengine.effectone.widget.EOLoadingImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

/**
 *Author: gaojin
 *Time: 2024/5/16 10:13
 */

class AutoMomentFragment : Fragment() {

    private var mediaScanProgress: ProgressBar? = null
    private var mediaScanProgressText: TextView? = null
    private var momentListView: RecyclerView? = null

    private var momentListContainer: View? = null
    private var scanProgressContainer: View? = null
    private var autoMomentRefresh: View? = null
    private var loadingViewContainer: View? = null
    private var loadingView: EOLoadingImageView? = null

    private val toCutSameLoadingDialog by lazy {
        AutoLoadingDialog.Builder(requireContext())
            .setTipMsg("加载中...")
            .show()
    }


    private val momentListAdapter by lazy {
        MomentListAdapter()
    }

    private val mTemplateConfig: TemplateConfig? = InnerEffectOneConfigList.getConfig()

    private val scope = MomentScope()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_auto_moment)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mediaScanProgress = view.findViewById(R.id.auto_moment_scan_progress)
        mediaScanProgressText = view.findViewById(R.id.auto_moment_scan_progress_text)
        momentListView = view.findViewById(R.id.auto_moment_list)
        autoMomentRefresh = view.findViewById<TextView?>(R.id.auto_moment_refresh)?.apply {
            setCompoundDrawables(
                ResourcesCompat.getDrawable(resources, R.drawable.auto_moment_refresh_icon, requireActivity().theme)?.apply {
                    setBounds(0, 0, SizeUtil.dp2px(28f), SizeUtil.dp2px(28f))
                },
                null,
                null,
                null
            )
        }

        momentListContainer = view.findViewById(R.id.auto_moment_list_container)
        scanProgressContainer = view.findViewById(R.id.auto_moment_scan_progress_container)

        loadingViewContainer = view.findViewById(R.id.auto_moment_loading_container)
        loadingView = view.findViewById(R.id.auto_moment_loading_first)

        view.findViewById<View>(R.id.iv_back).setOnClickListener {
            requireActivity().finish()
        }

        autoMomentRefresh?.setOnClickListener {
            refreshMoment()
        }

        momentListView?.run {
            adapter = momentListAdapter
            val spanCount = 3
            layoutManager = GridLayoutManager(requireContext(), spanCount, RecyclerView.VERTICAL, false)
            addItemDecoration(GridItemDecoration(spanCount, SizeUtil.dp2px(20f), SizeUtil.dp2px(20f)))
        }

        momentListAdapter.clickAction = { _, moment ->
            val templateAndAssets = ILAMomentSDK.getTemplateAndAssets(moment)
            templateAndAssets?.let {
                toCutSamePage(it)
            }
        }

        scope.launch {
            showLoading()
            val medias = withContext(Dispatchers.IO) {
                val medias = mutableListOf<RecognizeMedia>()
                val time = measureTimeMillis {
                    medias.addAll(queryMedias(AppSingleton.instance))
                }
                Log.i("AutoMomentFragment", "time:${time}")
                medias
            }
            initSDK()
            loadData()
            ILAMomentSDK.startScan(medias)
        }
    }

    private suspend fun loadData() {
        val totalTemplateList = withContext(Dispatchers.IO) {
            mTemplateConfig?.getTemplateList() ?: emptyList()
        }
        val moments = MomentConfigureHelper(requireContext(), MOMENT_CONFIGURE_ROOT_PATH)
            .loadConfigs()
            .map {
                it.copy(
                    config = it.config.copy(
                        momentTemplates = templateListToCKMoment(it.title, totalTemplateList)
                    )
                )
            }
        ILAMomentSDK.startCalculate(moments)
    }

    private fun initSDK() {
        ILAMomentSDK.registerListener(object : WrapperCKMomentStateListener() {

            override fun onUpdate() {
                val result = ILAMomentSDK.getMomentList()
                if (result.isNotEmpty()) {
                    dismissLoading()
                    momentListAdapter.updateItems(result)
                }
            }

            override fun didFinish() {
                dismissLoading()
            }

            @SuppressLint("SetTextI18n")
            override fun onProgress(value: Int) {
                mediaScanProgress?.progress = value
                mediaScanProgressText?.text = "${value}%"
            }
        })
    }

    private suspend fun templateListToCKMoment(title: String, totalTemplateList: List<TemplateItem>): List<CKMomentTemplate> {
        val templateList = withContext(Dispatchers.IO) {
            totalTemplateList.filter { it.templateTags == title }
        }
        return templateList.map {
            CKMomentTemplate(
                id = it.id.toString(),
                cover = it.cover?.url ?: "",
                segmentDurations = it.fragments.map { it.duration },
                extra = "",
                title = it.shortTitle
            ).apply {
                any = it
            }
        }
    }

    private fun toCutSamePage(matchedInfo: List<HLTemplateMatchedInfo>) {
        scope.launch(Dispatchers.Main) {
            toCutSameLoadingDialog.show()

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
            toCutSameLoadingDialog.dismiss()
            if (data.isNotEmpty()) {
                val intent = AutoMomentCutSameActivity.createIntent(requireActivity(), data)
                requireActivity().startActivity(intent)
            } else {
                Toast.makeText(AppSingleton.instance, "模板配置错误", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading() {
        loadingViewContainer?.visibility = View.VISIBLE
        loadingView?.changeVisibility(View.VISIBLE)
        momentListView?.visibility = View.GONE
    }

    private fun dismissLoading() {
        loadingViewContainer?.visibility = View.GONE
        loadingView?.changeVisibility(View.GONE)
        momentListView?.visibility = View.VISIBLE
    }

    private fun refreshMoment() {
        if (!ILAMomentSDK.isDone()) {
            Toast.makeText(AppSingleton.instance, "扫描中,请稍后刷新", Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch {
            showLoading()
            val medias = withContext(Dispatchers.IO) {
                val medias = mutableListOf<RecognizeMedia>()
                val time = measureTimeMillis {
                    medias.addAll(queryMedias(AppSingleton.instance))
                }
                Log.i("AutoMomentFragment", "time:${time}")
                medias
            }
            initSDK()
            loadData()
            ILAMomentSDK.startScan(medias)

            val totalTemplateList = withContext(Dispatchers.IO) {
                mTemplateConfig?.getTemplateList() ?: emptyList()
            }
            val moments = MomentConfigureHelper(requireContext(), MOMENT_CONFIGURE_ROOT_PATH)
                .loadConfigs()
                .map {
                    it.copy(
                        config = it.config.copy(
                            momentTemplates = templateListToCKMoment(it.title, totalTemplateList)
                        )
                    )
                }
            ILAMomentSDK.startCalculate(moments)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        ILAMomentSDK.unregisterListener()
    }
}