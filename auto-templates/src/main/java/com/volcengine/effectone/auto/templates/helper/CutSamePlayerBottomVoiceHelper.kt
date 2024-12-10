package com.volcengine.effectone.auto.templates.helper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.ss.android.ugc.aweme.views.setDebounceOnClickListener
import com.ss.android.ugc.cut_ui.MediaItem
import com.ss.ugc.android.editor.track.utils.visible
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.vm.CutSamePlayerViewModel
import com.volcengine.effectone.auto.templates.widget.ThumbTextSeekBar
import com.volcengine.effectone.utils.SizeUtil

/**
 * @author tyx
 * @description:CutSamePlayerBottomEditHelper的子helper
 * @date :2024/5/9 21:34
 */
class CutSamePlayerBottomVoiceHelper(
	override val activity: FragmentActivity,
	override val owner: LifecycleOwner
) : IUIHelper {

	private lateinit var mPanelContainer: View
	private lateinit var mSeekBar: ThumbTextSeekBar
	private lateinit var mBtnConfirm: Button
	private lateinit var mBtnCancel: Button
	private val mCutSamePlayerViewModel by lazy { CutSamePlayerViewModel.get(activity) }
	private var mBeforeVolumeProgress = 0
	private var mCurrentMediaItem: MediaItem? = null
	private var mIsConfirm = false   //是否确认

	private val mBackPressedCallback by lazy {
		object : OnBackPressedCallback(false) {
			override fun handleOnBackPressed() {
				mBtnCancel.performClick()
			}
		}
	}

	init {
		activity.onBackPressedDispatcher.addCallback(owner, mBackPressedCallback)
	}

	override fun initView(rootView: ViewGroup) {
		activity.lifecycle.addObserver(this)
		val inf = LayoutInflater.from(activity)
		val editPanelContainer = inf.inflate(R.layout.layout_voice_panel, rootView, true)
		mPanelContainer = editPanelContainer.findViewById(R.id.ly_voice_panel_container)
		mSeekBar = mPanelContainer.findViewById(R.id.seek_bar_voice)
		mBtnConfirm = mPanelContainer.findViewById(R.id.btn_confirm)
		mBtnCancel = mPanelContainer.findViewById(R.id.btn_cancel)
		mBtnConfirm.setDebounceOnClickListener {
			mIsConfirm = true
			mCutSamePlayerViewModel.mShowVoicePanel.value = false
		}
		mBtnCancel.setDebounceOnClickListener {
			mIsConfirm = false
			mCutSamePlayerViewModel.mShowVoicePanel.value = false
		}
		mSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
			override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

			}

			override fun onStartTrackingTouch(seekBar: SeekBar?) {
				mCutSamePlayerViewModel.pause()
			}

			override fun onStopTrackingTouch(seekBar: SeekBar) {
				mCurrentMediaItem?.let {
					val volume = seekBar.progress / 100f
					mCutSamePlayerViewModel.setVolume(it.materialId, volume)
					mCutSamePlayerViewModel.seekTo(it.targetStartTime.toInt(), true)
				}
			}
		})
		startObserver()
	}

	private fun startObserver() {
		mCutSamePlayerViewModel.mCurrentEditMediaItem.observe(activity) { result ->
			mCurrentMediaItem = result
			if (result == null) {
				mCutSamePlayerViewModel.mShowVoicePanel.value = false
			}
		}
		mCutSamePlayerViewModel.mShowVoicePanel.observe(activity) { isShow ->
			if (!isShow) {
				mCurrentMediaItem?.let {
					if (!mIsConfirm) {
						//恢复音量
						mCutSamePlayerViewModel.setVolume(it.materialId, mBeforeVolumeProgress / 100f)
					}
					mCutSamePlayerViewModel.seekTo(it.targetStartTime.toInt(), true)
				}
				mIsConfirm = false
			} else {
				mCurrentMediaItem?.let { mediaItem ->
					mBeforeVolumeProgress = mCutSamePlayerViewModel.getVolume(mediaItem.materialId)?.times(100)?.toInt() ?: 0
					mSeekBar.progress = mBeforeVolumeProgress
				}
			}
			setVisible(isShow)
		}
	}

	private fun setVisible(visible: Boolean) {
		mBackPressedCallback.isEnabled = visible
		if (mPanelContainer.visible == visible) return
		val height = maxOf(mPanelContainer.measuredHeight.toFloat(), SizeUtil.dp2px(117f).toFloat())
		val translationY = if (visible) 0f else height
		mPanelContainer.animate()
			.withStartAction {
				mPanelContainer.visible = true
			}
			.withEndAction {
				mPanelContainer.visible = visible
			}
			.translationY(translationY).setDuration(250).start()
	}

	@OnLifecycleEvent(value = Lifecycle.Event.ON_DESTROY)
	fun onDestroy() {
		mBackPressedCallback.remove()
	}
}