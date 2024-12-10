package com.volcengine.effectone.auto.templates.ui.layer

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.ss.android.ugc.aweme.views.setDebounceOnClickListener
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.widget.player.BaseVideoView
import com.volcengine.effectone.auto.templates.widget.player.IPlayerControl
import com.volcengine.effectone.auto.templates.widget.player.IPlayerLayer
import java.text.SimpleDateFormat

/**
 * @author tyx
 * @description:
 * @date :2024/5/22 16:24
 */
@SuppressLint("SimpleDateFormat")
open class PlayerProgressLayer<P : IPlayerControl> : IPlayerLayer<P> {

	protected var mPlayerControl: P? = null
	protected var mView: View? = null
	protected var mPlayerIcon: ImageView? = null
	protected var mSeekBar: SeekBar? = null
	protected var mTvDuration: TextView? = null
	protected var mTvCurrent: TextView? = null
	protected var mViewBg: View? = null
	protected var isStartTracking = false

	override fun bindPlayerControl(playControl: P) {
		mPlayerControl = playControl
	}

	override fun unBindPlayerControl() {
		mPlayerControl = null
	}

	override fun tag(): String {
		return this::class.java.simpleName
	}

	override fun getView(context: Context): View {
		val inf = LayoutInflater.from(context)
		return mView ?: inf.inflate(R.layout.layout_player_progress_layer, null).also {
			mView = it
			initView(it)
			onVisible(false)
		}
	}

	open fun initView(view: View) {
		mViewBg = view.findViewById(R.id.view_bg)
		mPlayerIcon = view.findViewById(R.id.iv_player_icon)
		mTvCurrent = view.findViewById(R.id.tv_current_time)
		mTvDuration = view.findViewById(R.id.tv_duration)
		mPlayerIcon?.setDebounceOnClickListener {
			if (mPlayerControl?.isPlaying() == true) {
				mPlayerControl?.pause()
			} else {
				mPlayerControl?.start()
			}
		}
		mSeekBar = view.findViewById(R.id.seek_bar)
		mSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
			override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
				onProgressChange(progress)
			}

			override fun onStartTrackingTouch(seekBar: SeekBar?) {
				onStartTrackingTouch()
			}

			override fun onStopTrackingTouch(seekBar: SeekBar?) {
				onStopTrackingTouch()
			}
		})
	}

	open fun onProgressChange(progress: Int) {
		if (isStartTracking) {
			mPlayerControl?.seeking(progress)
		}
	}

	open fun onStartTrackingTouch() {
		isStartTracking = true
		mPlayerControl?.pause()
	}

	open fun onStopTrackingTouch() {
		mPlayerControl?.seekTo(mSeekBar?.progress ?: 0, true) {
			isStartTracking = false
		}
	}

	private val dateFormat by lazy { SimpleDateFormat("mm:ss") }

	@SuppressLint("SetTextI18n")
	override fun onProgress(progress: Long, duration: Long) {
		mTvDuration?.text = " / ${dateFormat.format(duration)}"
		mTvCurrent?.text = dateFormat.format(progress)
		mSeekBar?.max = duration.toInt()
		if (!isStartTracking) mSeekBar?.progress = progress.toInt()
	}

	override fun onStateChange(state: Int) {
		onVisible(
			state != BaseVideoView.STATE_IDLE &&
					state != BaseVideoView.STATE_ERROR
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

	override fun onVisible(visible: Boolean) {
		mView?.visibility = if (visible) View.VISIBLE else View.GONE
	}
}