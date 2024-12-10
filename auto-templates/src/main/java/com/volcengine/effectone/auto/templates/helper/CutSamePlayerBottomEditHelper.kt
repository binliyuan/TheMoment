package com.volcengine.effectone.auto.templates.helper

import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.ss.android.ugc.aweme.views.setDebounceOnClickListener
import com.ss.android.ugc.tools.view.activity.AVActivityOnKeyDownListener
import com.ss.ugc.android.editor.track.utils.visible
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.vm.CutSamePlayerViewModel
import com.volcengine.effectone.auto.templates.widget.CustomButtonLayout
import com.volcengine.effectone.utils.SizeUtil

/**
 * @author tyx
 * @description:
 * @date :2024/5/7 17:28
 */
class CutSamePlayerBottomEditHelper(
	override val activity: FragmentActivity,
	override val owner: LifecycleOwner
) : IUIHelper, AVActivityOnKeyDownListener {

	private lateinit var mPanelContainer: ConstraintLayout
	private lateinit var mIvBack: ImageView
	private lateinit var mLyChangeResource: CustomButtonLayout
	private lateinit var mLyClip: CustomButtonLayout
	private lateinit var mLyVoice: CustomButtonLayout
	private lateinit var mCySave: CustomButtonLayout
	private val mVoicePanelHelper by lazy { CutSamePlayerBottomVoiceHelper(activity, owner) }

	private val mCutSamePlayerViewModel by lazy { CutSamePlayerViewModel.get(activity) }

	private val mBackPressedCallback by lazy {
		object : OnBackPressedCallback(false) {
			override fun handleOnBackPressed() {
				mIvBack.performClick()
			}
		}
	}

	init {
		activity.onBackPressedDispatcher.addCallback(owner, mBackPressedCallback)
	}

	override fun initView(rootView: ViewGroup) {
		val rootFrame = rootView.findViewById<FrameLayout>(R.id.bottom_frame)
		val inf = LayoutInflater.from(rootView.context)
		val view = inf.inflate(R.layout.layout_player_bottom_edit, rootFrame, false)
		rootFrame.addView(view)
		mPanelContainer = view.findViewById(R.id.ly_edit_panel_container)
		mVoicePanelHelper.initView(mPanelContainer)
		mIvBack = view.findViewById(R.id.iv_back)
		mLyChangeResource = view.findViewById(R.id.ly_change_source)
		mLyClip = view.findViewById(R.id.ly_clip)
		mLyVoice = view.findViewById(R.id.ly_voice)
		mCySave = view.findViewById(R.id.ly_save)
		setVisible(false)
		setOnClick()
		startObserver()
	}

	private fun setOnClick() {
		mIvBack.setDebounceOnClickListener {
			mCutSamePlayerViewModel.mCurrentEditMediaItem.value = null
		}
		mLyChangeResource.setDebounceOnClickListener {
			mCutSamePlayerViewModel.mCurrentEditMediaItem.value?.let {
				mCutSamePlayerViewModel.pause()
				mCutSamePlayerViewModel.mLaunchPicker.value = listOf(it) to true
			}
		}
		mLyClip.setDebounceOnClickListener {
			mCutSamePlayerViewModel.mCurrentEditMediaItem.value?.let {
				mCutSamePlayerViewModel.mCurrentEditMediaItem.value = null
				mCutSamePlayerViewModel.pause()
				mCutSamePlayerViewModel.mLaunchClip.value = it
			}
		}
		mLyVoice.setDebounceOnClickListener {
			mCutSamePlayerViewModel.pause()
			mCutSamePlayerViewModel.mShowVoicePanel.value = true
		}
		mCySave.setDebounceOnClickListener {}
	}

	private fun startObserver() {
		mCutSamePlayerViewModel.mCurrentEditMediaItem.observe(activity) { mediaItem ->
			mediaItem?.let {
				mLyVoice.visible = it.isVideo()
			}
			setVisible(mediaItem != null)
		}
	}

	fun setVisible(visible: Boolean) {
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

	override fun onKeyDown(p0: Int, p1: KeyEvent?): Boolean {
		if (mPanelContainer.visible) {
			mCutSamePlayerViewModel.mCurrentEditMediaItem.value = null
			return true
		}
		return false
	}

	@OnLifecycleEvent(value = Lifecycle.Event.ON_DESTROY)
	fun onDestroy() {
		mBackPressedCallback.remove()
	}
}