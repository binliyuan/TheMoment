package com.volcengine.effectone.auto.templates.ui.layer

import android.view.View
import com.ss.android.ugc.aweme.views.setDebounceOnClickListener
import com.volcengine.effectone.auto.templates.widget.player.BaseVideoView
import com.volcengine.effectone.auto.templates.widget.player.IPlayerControl

/**
 * @author tyx
 * @description:
 * @date :2024/5/23 11:18
 */
class PreviewProgressLayer : PlayerProgressLayer<IPlayerControl>() {

	override fun initView(view: View) {
		super.initView(view)
		mView?.setDebounceOnClickListener {
			if (mPlayerControl?.isPlaying() == true) {
				mPlayerControl?.pause()
			} else {
				mPlayerControl?.start()
			}
		}
	}

	override fun onProgressChange(progress: Int) {}
	override fun onStartTrackingTouch() {
		isStartTracking = true
	}

	override fun onStateChange(state: Int) {
		onVisible(
			state == BaseVideoView.STATE_PLAYING
					|| state == BaseVideoView.STATE_PREPARED
					|| state == BaseVideoView.STATE_BUFFERING
					|| state == BaseVideoView.STATE_BUFFERED
					|| state == BaseVideoView.STATE_COMPLETED
		)
		when (state) {
			BaseVideoView.STATE_PLAYING,
			BaseVideoView.STATE_PREPARING,
			BaseVideoView.STATE_BUFFERED,
			BaseVideoView.STATE_BUFFERING -> {
				mPlayerIcon?.isSelected = true
			}

			else -> mPlayerIcon?.isSelected = false
		}
	}
}