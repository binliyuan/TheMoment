package com.volcengine.effectone.auto.templates.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.cutsame.solution.template.model.TemplateItem
import com.volcengine.effectone.EffectOneSdk
import com.volcengine.effectone.auto.common.cutsame.CutSameContract
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.bean.BaseResultData
import com.volcengine.effectone.auto.templates.helper.PageTitleHelper
import com.volcengine.effectone.auto.templates.utils.PlayCacheServer
import com.volcengine.effectone.auto.templates.vm.CutSameComposeViewModel
import kotlin.math.roundToInt

/**
 * @author tyx
 * @description:
 * @date :2024/4/26 10:59
 */
open class AutoComposeFragment : Fragment() {

	companion object {
		const val MESSAGE = "message"
		const val TAG = "AutoComposeFragment"
	}

	private var mMessage: String = "视频合成中"
	private var mTemplateItem: TemplateItem? = null
	private val mViewHelper by lazy {
		listOf(PageTitleHelper(requireActivity(), viewLifecycleOwner, fitsSystemWindows = true))
	}
	private val mCutSameComposeViewModel by lazy { CutSameComposeViewModel.create(requireActivity()) }
	protected lateinit var mIvCover: ImageView
	protected lateinit var mProgressbar: ProgressBar
	protected lateinit var mTvMsg: TextView


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		mMessage = arguments?.getString(MESSAGE, mMessage) ?: mMessage
		mTemplateItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			arguments?.getParcelable(CutSameContract.ARG_TEMPLATE_ITEM, TemplateItem::class.java)
		} else {
			arguments?.getParcelable(CutSameContract.ARG_TEMPLATE_ITEM)
		}
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_compose, container, false).also {
			mIvCover = it.findViewById(R.id.iv_cover)
			mTvMsg = it.findViewById(R.id.tv_msg)
			mProgressbar = it.findViewById(R.id.progress)
			mTvMsg.text = mMessage
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		mViewHelper.forEach { lifecycle.addObserver(it) }
		super.onViewCreated(view, savedInstanceState)
		mViewHelper.forEach { it.initView(view as ViewGroup) }
		val url = mTemplateItem?.videoInfo?.url ?: ""
		if (url.isNotEmpty()) {
			val videoUrl = PlayCacheServer.proxyServer.getProxyUrl(url)
			EffectOneSdk.imageLoader.loadImageView(mIvCover, videoUrl)
		}
		startObserver()
	}

	open fun startObserver() {
		mCutSameComposeViewModel.mComposeLiveData.observe(viewLifecycleOwner) {
			if (it.state == BaseResultData.START || it.state == BaseResultData.PROGRESS) {
				val progress = it.progress
				mProgressbar.progress = progress.roundToInt()
			}
		}
	}
}