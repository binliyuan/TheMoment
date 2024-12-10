package com.volcengine.effectone.auto.templates.ui.layer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.airbnb.lottie.LottieAnimationView
import com.ss.ugc.android.editor.track.utils.visible
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.widget.player.BaseVideoView
import com.volcengine.effectone.auto.templates.widget.player.IPlayerControl
import com.volcengine.effectone.auto.templates.widget.player.IPlayerLayer

/**
 * @author tyx
 * @description:
 * @date :2024/5/22 18:06
 */
class LoadingLayer : IPlayerLayer<IPlayerControl> {
	companion object {
		const val TAG = "LoadingLayer"
	}

	private var mView: View? = null
	private var mLottieAnimationView: LottieAnimationView? = null

	override fun bindPlayerControl(playControl: IPlayerControl) {}

	override fun unBindPlayerControl() {}

	override fun tag(): String = TAG

	override fun getView(context: Context): View {
		val inf = LayoutInflater.from(context)
		return mView ?: inf.inflate(R.layout.layout_loading_layer, null).also {
			mLottieAnimationView = it.findViewById(R.id.loading_view)
			mView = it
			onVisible(false)
		}
	}

	override fun onProgress(progress: Long, duration: Long) {}

	override fun onStateChange(state: Int) {
		when (state) {
			BaseVideoView.STATE_PREPARING,
			BaseVideoView.STATE_BUFFERING -> {
				mLottieAnimationView?.playAnimation()
				onVisible(true)
			}

			else -> {
				onVisible(false)
				mLottieAnimationView?.pauseAnimation()
			}
		}
	}

	override fun onVisible(visible: Boolean) {
		mView?.visible = visible
	}
}