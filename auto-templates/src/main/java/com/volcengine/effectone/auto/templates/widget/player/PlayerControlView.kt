package com.volcengine.effectone.auto.templates.widget.player

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.ss.android.ugc.cut_log.LogUtil

/**
 * @author tyx
 * @description:
 * @date :2024/4/28 16:00
 */
open class PlayerControlView<D : IPlayerControl, T : IPlayerLayer<D>> : FrameLayout {

	companion object {
		private const val TAG = "PlayerControlView"
	}

	protected var mPlayerControl: D? = null
	protected val mControlLayers by lazy { mutableMapOf<String, T>() }
	private var mIsStartProgress = false
	private var mOpenProgress = false

	constructor(context: Context) : this(context, null)
	constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

	fun bindPlayerControl(control: D) {
		mPlayerControl = control
		mControlLayers.forEach { it.value.bindPlayerControl(control) }
	}

	fun unBindPlayerControl() {
		mPlayerControl = null
		mControlLayers.forEach { it.value.unBindPlayerControl() }
	}

	fun addPlayerLayer(layer: T) {
		val view = layer.getView(context)
		addView(view, 0)
		mControlLayers[layer.tag()] = layer
	}

	fun removePlayLayer(layer: IPlayerLayer<*>) {
		removeView(layer.getView(context))
		mControlLayers.remove(layer.tag())
	}

	fun setOpenProgress(boolean: Boolean) {
		mOpenProgress = boolean
	}

	open fun onPlayerStateChange(state: Int) {
		LogUtil.d(TAG, "state:$state")
		mControlLayers.forEach { layer -> layer.value.onStateChange(state) }
		if (state == BaseVideoView.STATE_PLAYING || state == BaseVideoView.STATE_BUFFERED) {
			if (mIsStartProgress && !mOpenProgress) return
			mIsStartProgress = true
			post(mProgressRunnable)
		} else {
			if (!mIsStartProgress) return
			mIsStartProgress = false
			removeCallbacks(mProgressRunnable)
		}
	}

	fun findPlayerLayer(tag: String): T? {
		return mControlLayers[tag]
	}

	fun onPlayerProgress(position: Long, duration: Long) {
		mControlLayers.forEach { layer ->
			layer.value.onProgress(position, duration)
		}
	}

	private val mProgressRunnable = object : Runnable {
		override fun run() {
			val progress = setProgress()
			if (mPlayerControl?.isPlaying() == true) {
				postDelayed(this, (100 - (progress % 100)).toLong())
			} else {
				mIsStartProgress = false
			}
		}
	}

	private fun setProgress(): Int {
		val duration = mPlayerControl?.getDuration()?.toInt() ?: 0
		val position = mPlayerControl?.getPosition()?.toInt() ?: 0
		onPlayerProgress(position.toLong(), duration.toLong())
		return position
	}

	fun release() {
		unBindPlayerControl()
		removeAllViews()
		mControlLayers.clear()
	}
}