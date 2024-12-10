package com.volcengine.effectone.auto.templates.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.cutsame.solution.player.CutSamePlayer
import com.cutsame.solution.player.PlayerStateListener
import com.ss.android.ugc.cut_ui.MediaItem
import com.ss.ugc.android.editor.track.utils.visible
import com.volcengine.ck.logkit.LogKit
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.cutsame.CutSameContext
import com.volcengine.effectone.auto.templates.event.TrackNodeEvent
import com.volcengine.effectone.auto.templates.helper.ChangeMusicHelper
import com.volcengine.effectone.auto.templates.helper.CutSamePlayerBottomEditHelper
import com.volcengine.effectone.auto.templates.helper.CutSamePlayerBottomVideoListHelper
import com.volcengine.effectone.auto.templates.helper.CutSamePlayerTextEditHelper
import com.volcengine.effectone.auto.templates.helper.textedit.IPlayerTextEditListener
import com.volcengine.effectone.auto.templates.ui.layer.CutSamePlayerControl
import com.volcengine.effectone.auto.templates.ui.layer.CutSamePlayerControlView
import com.volcengine.effectone.auto.templates.ui.layer.CutSamePlayerEditLayer
import com.volcengine.effectone.auto.templates.ui.layer.CutSamePlayerProgressLayer
import com.volcengine.effectone.auto.templates.vm.CutSamePlayerViewModel
import com.volcengine.effectone.auto.templates.vm.CutSameViewModel
import com.volcengine.effectone.auto.templates.vm.TemplatesSelectViewModel
import com.volcengine.effectone.auto.templates.widget.CutSamePlayerSurfaceView
import com.volcengine.effectone.auto.templates.widget.player.IWrapPlayerStateListener
import com.volcengine.effectone.utils.SizeUtil
import com.volcengine.effectone.utils.runOnUiThread
import kotlin.system.measureTimeMillis

/**
 * @author tyx
 * @description:
 * @date :2024/4/28 16:58
 */
class AutoCutSamePlayerFragment : Fragment(), IWrapPlayerStateListener, IPlayerTextEditListener {

	companion object {
		const val TAG = "AutoCutSamePlayerFragment"
	}

	private val mCutSamePlayerBottomVideoListHelper by lazy { CutSamePlayerBottomVideoListHelper(requireActivity(), viewLifecycleOwner) }
	private val mCutSamePlayerEditHelper by lazy { CutSamePlayerBottomEditHelper(requireActivity(), viewLifecycleOwner) }
	private val mCutSamePlayerTextEditHelper by lazy { CutSamePlayerTextEditHelper(requireActivity(), viewLifecycleOwner) }
	private val mChangeMusicHelper by lazy { ChangeMusicHelper(requireActivity(), viewLifecycleOwner) }
	private val mViewHelpers by lazy { mutableListOf<IUIHelper>() }
	private val mCutSameViewModel by lazy { CutSameViewModel.get(requireActivity()) }
	private val mTemplatesSelectViewModel by lazy { TemplatesSelectViewModel.get(requireActivity()) }
	private val mCutSamePlayerViewModel by lazy { CutSamePlayerViewModel.get(requireActivity()) }
	private var mPlayerControlView: CutSamePlayerControlView? = null
	private val mPlayerControl by lazy { CutSamePlayerControl(requireActivity(), viewLifecycleOwner) }
	private lateinit var mSurfaceView: CutSamePlayerSurfaceView
	private lateinit var mSurfaceContainer: FrameLayout
	private lateinit var mBottomFrameLayout: FrameLayout

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_cutsame_player, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		mViewHelpers.clear()
		mViewHelpers.add(mCutSamePlayerBottomVideoListHelper)
		mViewHelpers.add(mCutSamePlayerEditHelper)
		mViewHelpers.add(mCutSamePlayerTextEditHelper)
		mViewHelpers.add(mChangeMusicHelper)
		mViewHelpers.forEach { lifecycle.addObserver(it) }
		lifecycle.addObserver(mPlayerControl)
		super.onViewCreated(view, savedInstanceState)
		mViewHelpers.forEach { it.initView(view as ViewGroup) }
		mSurfaceView = view.findViewById(R.id.surface_view)
		mSurfaceContainer = view.findViewById(R.id.surface_view_container)
		mBottomFrameLayout = view.findViewById(R.id.bottom_frame)
		mCutSamePlayerTextEditHelper.setPlayerTextEditListener(this)
		initPlayerControl()
		startObserver()
	}

	private fun startObserver() {
		mTemplatesSelectViewModel.mStartSwitchTemplate.observe(viewLifecycleOwner) { template ->
			template ?: return@observe
			LogKit.d(TAG, "switch template,name = ${template.title}")
			mCutSameViewModel.prepareSource(template)
		}
		mTemplatesSelectViewModel.mIsShowTemplatePanel.observe(viewLifecycleOwner) { isShow ->
			isShow ?: return@observe
			mPlayerControlView?.onShowTemplatePanel(isShow)
			mBottomFrameLayout.visible = !isShow
			val params = mSurfaceContainer.layoutParams as MarginLayoutParams
			//底部docker高度 - 底部docker圆角 18?
			params.bottomMargin = if (isShow) 0 else SizeUtil.dp2px(117f) - SizeUtil.dp2px(18f)
			mSurfaceContainer.layoutParams = params
		}
		mCutSameViewModel.mPlayerDataReady.observe(viewLifecycleOwner) { result ->
			result ?: return@observe
			initPlayer(result)
		}
		mCutSamePlayerViewModel.mShowEditTextPanel.observe(viewLifecycleOwner) { result ->
			result ?: return@observe
			val isShow = result == true
			mPlayerControlView?.onShowEditTextPanel(isShow)
		}
		mCutSamePlayerViewModel.mCurrentEditMediaItem.observe(viewLifecycleOwner) { result ->
			mPlayerControlView?.onShowEditPanel(result != null)
		}
		mCutSamePlayerViewModel.mShowChangeMusic.observe(viewLifecycleOwner) { result ->
			result ?: return@observe
			val isShow = result == true
			if (isShow) mCutSamePlayerViewModel.pause() else mCutSamePlayerViewModel.start()
			mPlayerControlView?.onShowChangeMusicPanel(isShow)
		}
	}

	private fun initPlayerControl() {
		if (mPlayerControlView == null) {
			mPlayerControlView = CutSamePlayerControlView(requireActivity())
			mPlayerControlView?.run {
				addPlayerLayer(CutSamePlayerEditLayer())
				addPlayerLayer(CutSamePlayerProgressLayer())
				mSurfaceContainer.addView(this)
			}
		}
	}

	private fun initPlayer(mediaItems: ArrayList<MediaItem>) {
		LogKit.d(TAG, "initPlayer start")
		val delay = measureTimeMillis {
			val textItemList = mCutSameViewModel.mTextItemList
			val cutSameSource = mCutSameViewModel.mCutSameSource
			val player = mCutSamePlayerViewModel.initPlayer(cutSameSource, mSurfaceView)
			mCutSameViewModel.mCurrentTemplateItem?.let {
				CutSameContext.addCutSamePlayer(it.zipPath, player)
			}
			bindPlayerControl(player)
			mCutSamePlayerViewModel.registerPlayerStateListener(this)
			mCutSamePlayerViewModel.playerPrepare(mediaItems, textItemList)
		}
		LogKit.d(TAG, "initPlayer end delay:$delay")
	}

	private fun bindPlayerControl(player: CutSamePlayer?) {
		mPlayerControlView?.unBindPlayerControl()
		mPlayerControl.setPlayer(player)
		mPlayerControlView?.bindPlayerControl(mPlayerControl)
	}

	override fun onChanged(state: Int) {
		LogKit.d(TAG, "onPlayerStateChange:$state")
		runOnUiThread {
			mPlayerControlView?.onPlayerStateChange(state)
			when (state) {
				PlayerStateListener.PLAYER_STATE_PREPARED -> {
					//替换成视频合成后的textItems
					val textItems = mCutSamePlayerViewModel.getTextItems()
					if (textItems != null) {
						mCutSameViewModel.mTextItemList = textItems
					}
					mPlayerControlView?.onTextItems(textItems)
					val audioDuration = mCutSamePlayerViewModel.getAudioManager()?.getTemplateAudioDuration() ?: 0L
					mPlayerControlView?.onChangeMusicVisible(audioDuration != 0L)
					mPlayerControl.start()
				}

				PlayerStateListener.PLAYER_STATE_PLAYING -> {
					mCutSamePlayerViewModel.onStartPlayFps()
				}

				else -> mCutSamePlayerViewModel.onStopPlayFps()
			}
		}
	}

	override fun onFirstFrameRendered() {
		LogKit.d(TAG, "onFirstFrameRendered")
		runOnUiThread { mPlayerControlView?.onPlayerStateChange(-1) }
	}

	override fun onPlayEof() {
		LogKit.d(TAG, "onPlayerEof")
		runOnUiThread { mPlayerControlView?.onPlayerStateChange(-2) }
	}

	override fun onPlayError(what: Int, extra: String) {
		LogKit.d(TAG, "onPlayerError,what:$what,extra:$extra")
	}

	override fun onPlayProgress(process: Long) {
		LogKit.d(TAG, "position:$process,duration:${mPlayerControl.getDuration()}")
		runOnUiThread {
			mPlayerControlView?.onPlayerProgress(process, mPlayerControl.getDuration())
		}
	}

	override fun getCanvasSize(): IntArray {
		val surfaceW = mSurfaceView.measuredWidth
		val surfaceH = mSurfaceView.measuredHeight
		val size = mCutSamePlayerViewModel.getCanvasSize()
		val canvasW = size?.first() ?: surfaceW
		val canvasH = size?.last() ?: surfaceH
		val surfaceRatio = surfaceW.toFloat() / surfaceH
		val canvasRatio = canvasW.toFloat() / canvasH
		val width: Int
		val height: Int
		if (surfaceRatio > canvasRatio) {
			height = surfaceH
			width = (surfaceH * canvasRatio).toInt()
		} else {
			width = surfaceW
			height = (surfaceW / canvasRatio).toInt()
		}
		return intArrayOf(width, height)
	}

	override fun getSurfaceSize(): IntArray {
		return intArrayOf(mSurfaceView.measuredWidth, mSurfaceView.measuredHeight)
	}

	override fun onPause() {
		super.onPause()
		mCutSamePlayerViewModel.pause()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		mCutSamePlayerTextEditHelper.setPlayerTextEditListener(null)
		mCutSamePlayerViewModel.unRegisterPlayerStateListener(this)
		mPlayerControlView?.release()
		mPlayerControl.release()
	}
}