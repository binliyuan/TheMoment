package com.volcengine.effectone.auto.templates.widget.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaPlayer.OnBufferingUpdateListener
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnErrorListener
import android.media.MediaPlayer.OnInfoListener
import android.media.MediaPlayer.OnPreparedListener
import android.media.MediaPlayer.OnVideoSizeChangedListener
import android.media.MediaPlayer.SEEK_CLOSEST
import android.net.Uri
import android.os.Build
import android.view.Surface
import java.lang.ref.WeakReference

/**
 * @author tyx
 * @description:
 * @date :2024/5/22 10:15
 */
class MediaVideoPlayer(private val mContext: Context) : IVideoPlayer {

	private var mMediaPlayer: MediaPlayer? = null
	private var mVideoPlayerListener: VideoPlayerListener? = null
	private var mIsPreparing = false
	private var mListenerAdapter: MediaVideoPlayerListenerHolder? = null

	override fun initPlayer() {
		if (mMediaPlayer == null) {
			mMediaPlayer = MediaPlayer()
			mMediaPlayer?.run {
				val attributes = AudioAttributes.Builder()
					.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
					.build()
				setAudioAttributes(attributes)
			}
			attachInternalListeners()
		}
	}

	private fun attachInternalListeners() {
		mListenerAdapter = MediaVideoPlayerListenerHolder(this)
		mListenerAdapter?.let { holder ->
			mMediaPlayer?.run {
				setOnPreparedListener(holder)
				setOnCompletionListener(holder)
				setOnErrorListener(holder)
				setOnInfoListener(holder)
				setOnBufferingUpdateListener(holder)
				setOnVideoSizeChangedListener(holder)
			}
		}
	}

	private inner class MediaVideoPlayerListenerHolder(player: MediaVideoPlayer) : OnPreparedListener,
		OnCompletionListener,
		OnErrorListener,
		OnInfoListener,
		OnBufferingUpdateListener,
		OnVideoSizeChangedListener {

		private val mWeakMediaPlayer = WeakReference(player)

		override fun onPrepared(mp: MediaPlayer?) {
			mWeakMediaPlayer.get()?.let {
				it.mVideoPlayerListener?.onPrepared()
				it.play()
			}
		}

		override fun onCompletion(mp: MediaPlayer?) {
			mWeakMediaPlayer.get()?.let {
				it.mVideoPlayerListener?.onCompletion()
			}
		}

		override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
			mWeakMediaPlayer.get()?.let {
				return it.mVideoPlayerListener?.onError(what, extra) ?: false
			}
			return false
		}

		override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
			mWeakMediaPlayer.get()?.let {
				return it.mVideoPlayerListener?.onInfo(what, extra) ?: false
			}
			return false
		}

		override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
			mWeakMediaPlayer.get()?.let {
				it.mVideoPlayerListener?.onBufferingUpdate(percent)
			}
		}

		override fun onVideoSizeChanged(mp: MediaPlayer?, width: Int, height: Int) {
			mWeakMediaPlayer.get()?.let {
				it.mVideoPlayerListener?.onVideoSizeChanged(width, height)
			}
		}
	}

	override fun setDataSource(path: String) {
		runCatching {
			mMediaPlayer?.setDataSource(path)
		}.onFailure {
			mVideoPlayerListener?.onError(-1, -1)
		}
	}

	override fun setDataSource(path: String, heads: Map<String, String>) {
		runCatching {
			mMediaPlayer?.setDataSource(mContext, Uri.parse(path), heads)
		}.onFailure {
			mVideoPlayerListener?.onError(-1, -1)
		}
	}

	override fun setSurface(surface: Surface) {
		mMediaPlayer?.setSurface(surface)
	}

	override fun prepareAsync() {
		runCatching {
			mMediaPlayer?.prepareAsync()
		}.onSuccess {
			mIsPreparing = true
		}.onFailure {
			mVideoPlayerListener?.onError(-2, -2)
		}
	}

	override fun play() {
		runCatching {
			mMediaPlayer?.start()
		}.onFailure {
			mVideoPlayerListener?.onError(-3, -3)
		}
	}

	override fun seekTo(time: Int) {
		runCatching {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				mMediaPlayer?.seekTo(time.toLong(), SEEK_CLOSEST)
			} else {
				mMediaPlayer?.seekTo(time)
			}
		}.onFailure {
			mVideoPlayerListener?.onError(-4, -4)
		}
	}

	override fun pause() {
		runCatching {
			mMediaPlayer?.pause()
		}.onFailure {
			mVideoPlayerListener?.onError(-5, -5)
		}
	}

	override fun isPlaying(): Boolean {
		return mMediaPlayer?.isPlaying == true
	}

	override fun getDuration(): Int {
		return mMediaPlayer?.duration ?: 0
	}

	override fun getCurrentPosition(): Int {
		return mMediaPlayer?.currentPosition ?: -0
	}

	override fun setLooping(isLoop: Boolean) {
		mMediaPlayer?.isLooping = isLoop
	}

	override fun setVideoPlayerListener(listener: VideoPlayerListener) {
		mVideoPlayerListener = listener
	}

	override fun release() {
		mMediaPlayer?.run {
			setOnPreparedListener(null)
			setOnCompletionListener(null)
			setOnErrorListener(null)
			setOnInfoListener(null)
			setOnBufferingUpdateListener(null)
			setOnVideoSizeChangedListener(null)
			mVideoPlayerListener = null
			mIsPreparing = false
			runCatching { mMediaPlayer?.release() }
		}
		mMediaPlayer = null
	}
}