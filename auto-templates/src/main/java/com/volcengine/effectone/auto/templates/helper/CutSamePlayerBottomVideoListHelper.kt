package com.volcengine.effectone.auto.templates.helper

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.ies.cutsame.util.SizeUtil
import com.ss.android.ugc.aweme.views.setDebounceOnClickListener
import com.ss.ugc.android.editor.track.utils.runOnUiThread
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.adapter.CutSamePlayerBottomVideoListAdapter
import com.volcengine.effectone.auto.templates.ui.AutoCutSameExportActivity
import com.volcengine.effectone.auto.templates.utils.CutSameUtil
import com.volcengine.effectone.auto.templates.vm.CutSamePlayerViewModel
import com.volcengine.effectone.auto.templates.vm.CutSameViewModel
import com.volcengine.effectone.auto.templates.widget.CenterScrollLinearLayoutManager
import com.volcengine.effectone.auto.templates.widget.CustomButtonLayout
import com.volcengine.effectone.auto.templates.widget.player.IWrapPlayerStateListener

/**
 * @author tyx
 * @description:
 * @date :2024/4/29 14:07
 */
class CutSamePlayerBottomVideoListHelper(
	override val activity: FragmentActivity,
	override val owner: LifecycleOwner
) : IUIHelper, IWrapPlayerStateListener {

	private lateinit var mRv: RecyclerView
	private lateinit var mExport: CustomButtonLayout
	private lateinit var mAdapter: CutSamePlayerBottomVideoListAdapter
	private val mCutSameViewModel by lazy { CutSameViewModel.get(activity) }
	private val mCutSamePlayerViewModel by lazy { CutSamePlayerViewModel.get(activity) }
	private var mClickPosition = -1
	private var mClickStartProgress = -1L

	override fun initView(rootView: ViewGroup) {
		val rootFrame = rootView.findViewById<FrameLayout>(R.id.bottom_frame)
		val inf = LayoutInflater.from(rootView.context)
		val view = inf.inflate(R.layout.layout_cutsame_player_bottom_video_list, rootFrame, false)
		rootFrame.addView(view)
		mRv = view.findViewById(R.id.recycler_view)
		mExport = view.findViewById(R.id.tv_export)
		mExport.setDebounceOnClickListener {
			mCutSamePlayerViewModel.pause()
			val key = mCutSameViewModel.mCurrentTemplateItem?.zipPath
			if (key.isNullOrEmpty()) return@setDebounceOnClickListener
			val intent = AutoCutSameExportActivity.createIntent(activity, key)
			activity.startActivity(intent)
		}
		initAdapter()
		startObserver()
	}

	private fun initAdapter() {
		mAdapter = CutSamePlayerBottomVideoListAdapter()
		mRv.layoutManager = CenterScrollLinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
		mRv.adapter = mAdapter
		mRv.addItemDecoration(object : RecyclerView.ItemDecoration() {
			override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
				super.getItemOffsets(outRect, view, parent, state)
				outRect.right = SizeUtil.dp2px(18f)
			}
		})
		mAdapter.clickBlock = { view, position, mediaItem ->
			mClickPosition = position
			mClickStartProgress = mediaItem.targetStartTime
			mCutSamePlayerViewModel.pause()
			mCutSamePlayerViewModel.seekTo(mediaItem.targetStartTime.toInt(), false)
			if (view.id == R.id.video_edit_bg) {
				mCutSamePlayerViewModel.mCurrentEditMediaItem.value = mediaItem
			}
		}
	}

	private fun startObserver() {
		mCutSameViewModel.mUpdateBottomVideoList.observe(activity) {
			it ?: return@observe
			mAdapter.updateItems(it)
		}
		mCutSamePlayerViewModel.mPlayerState.observe(activity) { state ->
			if (state == CutSamePlayerViewModel.CREATE) {
				mCutSamePlayerViewModel.unRegisterPlayerStateListener(this)
				mCutSamePlayerViewModel.registerPlayerStateListener(this)
			}
		}
	}

	override fun onPlayProgress(process: Long) {
		//正在修改音量，将播放区间控制在当前槽位的时间内，必须
		val isShowVoicePanel = mCutSamePlayerViewModel.mShowVoicePanel.value == true
		if (isShowVoicePanel) {
			mCutSamePlayerViewModel.mCurrentEditMediaItem.value?.let {
				if (process < it.targetStartTime || process >= it.targetEndTime) {
					mCutSamePlayerViewModel.pause()
				}
			}
			return
		}
		val list = mCutSameViewModel.mUpdateBottomVideoList.value ?: arrayListOf()
		val index = if (mClickPosition > 0 && mClickStartProgress == process) {
			//直接点击
			val position = mClickPosition
			mClickPosition = -1
			mClickStartProgress = -1L
			position
		} else CutSameUtil.findIndexFromPlayerProgressV3(process, list)
		runOnUiThread {
			val change = mAdapter.updateSelectIndex(index)
			if (change) {
				mCutSamePlayerViewModel.mCurrentEditMediaItem.value = null
				mRv.adapter?.run {
					if (!mRv.isPressed && mRv.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
						mRv.smoothScrollToPosition(minOf(index, itemCount - 1))
					}
				}
			}
		}
	}

	@OnLifecycleEvent(value = Lifecycle.Event.ON_DESTROY)
	fun onDestroy() {
		mCutSamePlayerViewModel.unRegisterPlayerStateListener(this)
	}
}