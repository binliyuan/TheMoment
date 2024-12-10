package com.volcengine.effectone.auto.templates.widget.player

import android.view.Surface

/**
 * @author tyx
 * @description:
 * @date :2024/5/22 10:04
 */
interface IVideoPlayer {
	fun initPlayer()
	fun setDataSource(path: String)
	fun setDataSource(path: String, heads: Map<String, String>)
	fun setSurface(surface: Surface)
	fun prepareAsync()
	fun play()
	fun seekTo(time: Int)
	fun pause()
	fun isPlaying(): Boolean
	fun getDuration(): Int
	fun getCurrentPosition(): Int
	fun setLooping(isLoop: Boolean)
	fun setVideoPlayerListener(listener: VideoPlayerListener)
	fun release()
}

interface VideoPlayerListener {
	fun onPrepared()
	fun onInfo(state: Int, extra: Int): Boolean
	fun onError(what: Int, extra: Int): Boolean
	fun onBufferingUpdate(percent: Int)
	fun onCompletion()
	fun onVideoSizeChanged(width: Int, height: Int)
}