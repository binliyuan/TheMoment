package com.volcengine.effectone.auto.templates.widget.player

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.analytics.DefaultAnalyticsCollector
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Clock
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.video.VideoSize
import java.io.File

/**
 * @author tyx
 * @description:
 * @date :2024/6/13 16:06
 */
class ExoVideoPlayer(private val mContext: Context) : IVideoPlayer, Player.Listener {

	private var mExoPlayer: ExoPlayer? = null
	private var mCache: Cache? = null
	private var mHttpDataSourceFactory: HttpDataSource.Factory? = null
	private var mMediaSource: MediaSource? = null
	private var mVideoPlayerListener: VideoPlayerListener? = null
	private var isCache = true
	private var mIsPreparing = false

	override fun initPlayer() {
		if (mExoPlayer == null) {
			val playerBuilder = ExoPlayer.Builder(mContext)
				.setMediaSourceFactory(DefaultMediaSourceFactory(mContext))
				.setRenderersFactory(DefaultRenderersFactory(mContext).setEnableDecoderFallback(true))
				.setTrackSelector(DefaultTrackSelector(mContext))
				.setLoadControl(DefaultLoadControl.Builder().build())
				.setAnalyticsCollector(DefaultAnalyticsCollector(Clock.DEFAULT))
				.setBandwidthMeter(DefaultBandwidthMeter.getSingletonInstance(mContext))
			mExoPlayer = playerBuilder.build().apply {
				addListener(this@ExoVideoPlayer)
				addAnalyticsListener(EventLogger())
				setAudioAttributes(AudioAttributes.DEFAULT,  /* p1 = */true)
				playWhenReady = true
			}
		}
	}

	override fun setDataSource(path: String) {
		setDataSource(path, emptyMap())
	}

	override fun setDataSource(path: String, heads: Map<String, String>) {
		initMediaSource(Uri.parse(path), heads)
	}

	private fun initMediaSource(uri: Uri, heads: Map<String, String>) {
		val factory = if (isCache) {
			if (mCache == null) {
				mCache = newCache()
			}
			mCache?.run {
				CacheDataSource.Factory()
					.setCache(this)
					.setUpstreamDataSourceFactory(getDataSourceFactory())
					.setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
			}
		} else {
			getDataSourceFactory()
		}
		if (heads.isNotEmpty()) {
			mHttpDataSourceFactory?.setDefaultRequestProperties(heads)
		}
		mMediaSource = factory?.let { ProgressiveMediaSource.Factory(it).createMediaSource(MediaItem.fromUri(uri)) }
	}

	private fun newCache(): Cache {
		return SimpleCache(
			File(mContext.externalCacheDir, "exo_video_cache"),
			LeastRecentlyUsedCacheEvictor(512L * 1024 * 1024),
			StandaloneDatabaseProvider(mContext)
		)
	}

	private fun getDataSourceFactory(): DataSource.Factory? {
		return getHttpDataSourceFactory()?.let { DefaultDataSource.Factory(mContext, it) }
	}

	private fun getHttpDataSourceFactory(): DataSource.Factory? {
		if (mHttpDataSourceFactory == null) {
			mHttpDataSourceFactory = DefaultHttpDataSource.Factory()
				.setAllowCrossProtocolRedirects(true)
		}
		return mHttpDataSourceFactory
	}

	override fun setSurface(surface: Surface) {
		mExoPlayer?.setVideoSurface(surface)
	}

	override fun prepareAsync() {
		mMediaSource?.run {
			mIsPreparing = true
			mExoPlayer?.setMediaSource(this)
			mExoPlayer?.prepare()
		}
	}

	override fun play() {
		mExoPlayer?.playWhenReady = true
	}

	override fun seekTo(time: Int) {
		mExoPlayer?.seekTo(time.toLong())
	}

	override fun pause() {
		mExoPlayer?.playWhenReady = false
	}

	override fun isPlaying(): Boolean {
		return when (mExoPlayer?.playbackState) {
			Player.STATE_BUFFERING, Player.STATE_READY -> mExoPlayer?.playWhenReady ?: false
			Player.STATE_IDLE, Player.STATE_ENDED -> false
			else -> false
		}
	}

	override fun getDuration(): Int {
		return mExoPlayer?.duration?.toInt() ?: 0
	}

	override fun getCurrentPosition(): Int {
		return mExoPlayer?.currentPosition?.toInt() ?: 0
	}

	override fun setLooping(isLoop: Boolean) {
		mExoPlayer?.repeatMode = if (isLoop) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
	}

	override fun setVideoPlayerListener(listener: VideoPlayerListener) {
		mVideoPlayerListener = listener
	}

	override fun onPlaybackStateChanged(playbackState: Int) {
		super.onPlaybackStateChanged(playbackState)
		if (mIsPreparing) {
			if (playbackState == Player.STATE_READY) {
				mVideoPlayerListener?.onPrepared()
				mVideoPlayerListener?.onInfo(MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START, 0)
				mIsPreparing = false
			}
			return
		}
		when (playbackState) {
			Player.STATE_ENDED -> mVideoPlayerListener?.onCompletion()
			Player.STATE_BUFFERING -> mVideoPlayerListener?.onInfo(MediaPlayer.MEDIA_INFO_BUFFERING_START, getBufferPercentage())
			//缓冲结束
			Player.STATE_READY -> mVideoPlayerListener?.onInfo(MediaPlayer.MEDIA_INFO_BUFFERING_END, getBufferPercentage())
			Player.STATE_IDLE -> {}
		}
	}

	private fun getBufferPercentage(): Int {
		return mExoPlayer?.bufferedPercentage ?: 0
	}

	override fun onPlayerError(error: PlaybackException) {
		super.onPlayerError(error)
		error.printStackTrace()
		mVideoPlayerListener?.onError(-1, -1)
	}

	override fun onVideoSizeChanged(videoSize: VideoSize) {
		super.onVideoSizeChanged(videoSize)
		mVideoPlayerListener?.onVideoSizeChanged(videoSize.width, videoSize.height)
	}

	override fun release() {
		mCache?.release()
		mExoPlayer?.run {
			removeListener(this@ExoVideoPlayer)
			release()
		}
		mIsPreparing = false
		mVideoPlayerListener = null
		mExoPlayer = null
	}
}