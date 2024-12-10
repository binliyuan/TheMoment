package com.volcengine.effectone.auto.templates.widget.player

import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout

/**
 * @author tyx
 * @description:
 * @date :2024/5/22 10:34
 */
abstract class BaseVideoView : FrameLayout, VideoPlayerListener {

	companion object {
		const val STATE_ERROR = 0       //错误
		const val STATE_IDLE = 1        //等待
		const val STATE_PREPARING = 2   //准备中
		const val STATE_PREPARED = 3    //准备完毕
		const val STATE_PLAYING = 4     //播放中
		const val STATE_PAUSED = 5      //暂停中
		const val STATE_BUFFERING = 6   //缓冲中
		const val STATE_BUFFERED = 7    //缓冲完毕
		const val STATE_COMPLETED = 8   //播放完成
	}

	protected var mPlayer: IVideoPlayer? = null
	protected var mControlView: PlayerControlView<*, *>? = null
	protected var mControl: BaseVideoControl? = null
	protected var mRenderView: IRenderView? = null
	private lateinit var mPlayerContainer: FrameLayout
	protected var mCurrentPlayState = STATE_IDLE //当前播放器的状态
	private var mCurrentPosition = 0L
	protected var mUrl: String = "" //当前播放视频的地址
	protected var mCurrentScreenScaleType = IRenderView.SCREEN_SCALE_MATCH_PARENT
	private var mBufferPercentage = 0
	private var isLooping = true

	constructor(context: Context) : this(context, null)
	constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
		initView(context)
	}

	abstract fun createPlayer(context: Context): IVideoPlayer
	abstract fun createRenderView(context: Context): IRenderView

	private fun initView(context: Context) {
		setBackgroundColor(Color.BLACK)
		mPlayerContainer = FrameLayout(context)
		addView(mPlayerContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
	}

	fun setDataSource(url: String) {
		mUrl = url
	}

	protected fun isInIdleState() = mCurrentPlayState == STATE_IDLE
	protected fun isInPlaybackState(): Boolean {
		return mPlayer != null
				&& mCurrentPlayState != STATE_ERROR
				&& mCurrentPlayState != STATE_IDLE
				&& mCurrentPlayState != STATE_PREPARING
				&& mCurrentPlayState != STATE_COMPLETED
	}

	protected fun startPlay() {
		initPlayer()
		addDisplay()
		startPrepare()
	}

	protected fun startInPlaybackState() {
		mPlayer?.play()
		if (mCurrentPlayState != STATE_BUFFERING) setPlayState(STATE_PLAYING)
	}

	protected fun initPlayer() {
		mPlayer = createPlayer(context)
		mPlayer?.setVideoPlayerListener(this)
		mPlayer?.initPlayer()
		setPlayerConfig()
	}

	protected fun setPlayerConfig() {
		mPlayer?.setLooping(isLooping)
	}

	protected fun addDisplay() {
		mRenderView?.let {
			mPlayerContainer.removeView(it.getView())
			it.release()
		}
		mRenderView = createRenderView(context)
		mRenderView?.run {
			mPlayer?.let { player -> attachToPlayer(player) }
			val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER)
			mPlayerContainer.addView(getView(), 0, lp)
		}
	}

	protected fun startPrepare() {
		if (mUrl.isNotEmpty()) {
			mPlayer?.setDataSource(mUrl)
			mPlayer?.prepareAsync()
			setPlayState(STATE_PREPARING)
		} else {
			onError(-10, -10)
		}
	}

	fun setVideoControl(control: BaseVideoControl) {
		mControl = control
	}

	fun setScreenScaleType(screenScaleType: Int) {
		mCurrentScreenScaleType = screenScaleType
		mRenderView?.setScaleType(screenScaleType)
	}

	fun <P : IPlayerControl, V : IPlayerLayer<P>> setVideoControlView(videoControlView: PlayerControlView<P, V>) {
		mControlView?.let {
			it.unBindPlayerControl()
			mPlayerContainer.removeView(it)
		}
		mControlView = videoControlView
		if (mControl == null) mControl = BaseVideoControl(this)
		mControl?.let { videoControlView.bindPlayerControl(it as P) }
		val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
		mPlayerContainer.addView(videoControlView, lp)
	}

	protected fun setPlayerKeepScreenOn(keep: Boolean) {
		mPlayerContainer.keepScreenOn = keep
	}

	// -------------------------------------------------------------------
	fun start() {
		if (isInIdleState()) {
			startPlay()
		} else if (isInPlaybackState()) {
			startInPlaybackState()
		}
	}

	fun pause() {
		if (mPlayer?.isPlaying() == true) {
			mPlayer?.pause()
			setPlayState(STATE_PAUSED)
			setPlayerKeepScreenOn(false)
		}
	}

	fun getDuration(): Long {
		return mPlayer?.getDuration()?.toLong() ?: 0L
	}

	fun getPosition(): Long {
		mCurrentPosition = mPlayer?.getCurrentPosition()?.toLong() ?: 0L
		return mCurrentPosition
	}

	fun isPlaying(): Boolean {
		return isInPlaybackState() && mPlayer?.isPlaying() == true
	}

	fun seekTo(progress: Int, callback: ((ret: Int) -> Unit)?) {
		mPlayer?.seekTo(progress)
		callback?.invoke(progress)
	}

	fun seeking(value: Int) {
	}
	// -------------------------------------------------------------------

	fun release() {
		mPlayer?.release()
		mPlayer = null
		releaseRenderView()
		releasePlayerControlView()
		setPlayerKeepScreenOn(false)
		mCurrentPosition = 0
		setPlayState(STATE_IDLE)
	}

	private fun releasePlayerControlView() {
		mControlView?.let {
			it.unBindPlayerControl()
			mPlayerContainer.removeView(it)
		}
		mControlView = null
		mControl = null
	}

	private fun releaseRenderView() {
		mRenderView?.let {
			mPlayerContainer.removeView(it.getView())
			it.release()
		}
		mRenderView = null
	}


	override fun onPrepared() {
		setPlayState(STATE_PREPARED)
		if (mCurrentPosition > 0) seekTo(mCurrentPosition.toInt(), null)
	}

	override fun onInfo(state: Int, extra: Int): Boolean {
		when (state) {
			MediaPlayer.MEDIA_INFO_BUFFERING_START -> setPlayState(STATE_BUFFERING)
			MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
				if (mCurrentPlayState != STATE_PAUSED) {
					// seekTo后自动触发播放
					setPlayState(STATE_BUFFERED)
					if (!isPlaying()) {
						start()
					}
				}
			}

			MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
				setPlayState(STATE_PLAYING)
			}
		}
		return true
	}

	override fun onError(what: Int, extra: Int): Boolean {
		setPlayerKeepScreenOn(false)
		setPlayState(STATE_ERROR)
		return true
	}

	override fun onBufferingUpdate(percent: Int) {
		mBufferPercentage = percent
	}

	override fun onCompletion() {
		setPlayerKeepScreenOn(false)
		mCurrentPosition = 0
		setPlayState(STATE_COMPLETED)
	}

	override fun onVideoSizeChanged(width: Int, height: Int) {
		mRenderView?.run {
			setScaleType(mCurrentScreenScaleType)
			setVideoSize(width, height)
		}
	}

	private fun setPlayState(playState: Int) {
		mCurrentPlayState = playState
		mControlView?.onPlayerStateChange(playState)
	}
}