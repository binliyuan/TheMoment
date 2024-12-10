package com.volcengine.effectone.auto.templates.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cutsame.solution.template.model.TemplateItem
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.bean.TemplateByMedias
import com.volcengine.effectone.auto.templates.helper.PageTitleHelper
import com.volcengine.effectone.auto.templates.ui.layer.CoverLayer
import com.volcengine.effectone.auto.templates.ui.layer.LoadingLayer
import com.volcengine.effectone.auto.templates.ui.layer.PreviewProgressLayer
import com.volcengine.effectone.auto.templates.ui.layer.TemplatePreviewCoverLayer
import com.volcengine.effectone.auto.templates.utils.PlayCacheServer
import com.volcengine.effectone.auto.templates.widget.player.IPlayerControl
import com.volcengine.effectone.auto.templates.widget.player.IPlayerLayer
import com.volcengine.effectone.auto.templates.widget.player.IRenderView
import com.volcengine.effectone.auto.templates.widget.player.PlayerView
import com.volcengine.effectone.auto.templates.widget.player.PlayerControlView

/**
 * @author tyx
 * @description:
 * @date :2024/5/22 9:54
 */
class AutoTemplatePreviewFragment : Fragment() {
	companion object {
		const val TAG = "TemplatePreviewFragment"
	}

	private var mTemplateItem: TemplateItem? = null
	private val mViewHelper by lazy { mutableListOf<IUIHelper>() }
	private lateinit var mPlayerView: PlayerView
	private var mVideoControlView: PlayerControlView<IPlayerControl, IPlayerLayer<IPlayerControl>>? = null
	private var isStartCutSame = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		mTemplateItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			arguments?.getParcelable(AutoTemplatePreviewActivity.TEMPLATE_ITEM, TemplateItem::class.java)
		} else arguments?.getParcelable(AutoTemplatePreviewActivity.TEMPLATE_ITEM)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_template_preview, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		mViewHelper.clear()
		val title = mTemplateItem?.title ?: ""
		mViewHelper.add(PageTitleHelper(requireActivity(), viewLifecycleOwner, title, true))
		mViewHelper.forEach { lifecycle.addObserver(it) }
		super.onViewCreated(view, savedInstanceState)
		mViewHelper.forEach { it.initView(view as ViewGroup) }
		if (mTemplateItem == null) {
			requireActivity().finish()
			return
		}
		mPlayerView = view.findViewById(R.id.player_view)
		initPlayerControlView()
		initPlayer()
	}

	private fun initPlayerControlView() {
		if (mVideoControlView == null) {
			mVideoControlView = PlayerControlView(requireActivity())
			mVideoControlView?.setOpenProgress(true)
			mTemplateItem?.let {
				mVideoControlView?.addPlayerLayer(TemplatePreviewCoverLayer(it).apply {
					mUseBlock = { template ->
						isStartCutSame = true
						val list = arrayListOf(TemplateByMedias(template, emptyList()))
						val intent = AutoCutSameActivity.createIntent(requireActivity(), list)
						requireActivity().startActivity(intent)
					}
				})
			}
			mVideoControlView?.addPlayerLayer(LoadingLayer())
			mVideoControlView?.addPlayerLayer(PreviewProgressLayer())
			mVideoControlView?.addPlayerLayer(CoverLayer(mTemplateItem))
		}
	}

	private fun initPlayer() {
		mVideoControlView?.let { controlView ->
			mPlayerView.setVideoControlView(controlView)
			val videoUrl = PlayCacheServer.proxyServer.getProxyUrl(mTemplateItem?.videoInfo?.url ?: "")
			mPlayerView.setDataSource(videoUrl)
			mPlayerView.setScreenScaleType(IRenderView.SCREEN_SCALE_16_9)
		}
	}

	override fun onStop() {
		super.onStop()
		mPlayerView.pause()
	}

	override fun onDestroy() {
		super.onDestroy()
		mVideoControlView?.release()
		mPlayerView.release()
	}
}