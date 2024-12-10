package com.volcengine.effectone.auto.moment.hl.player

import android.content.Context
import android.util.Log
import android.util.Size
import android.view.Gravity
import android.view.SurfaceView
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.util.EventLogger
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.math.max
import kotlin.math.roundToInt

class AutoMaterialPlayerWrapper {

    var playerFirstFrameAction: (() -> Unit)? = null
    var playErrorAction: ((code: Int, msg: String) -> Unit)? = null
    var playStateAction: ((playbackState: @Player.State Int) -> Unit)? = null
    var playerProgressAction: ((duration:Long, currentPosition:Long, contentPosition:Long, contentDuration:Long, contentBufferedPosition:Long) -> Unit)? = null
    var playerPlayingAction:((isPlaying:Boolean)-> Unit)?= null

    private val mainScope by lazy { object :CoroutineScope{
        override val coroutineContext: CoroutineContext
            get() = Dispatchers.Main + SupervisorJob() + CoroutineName(TAG)

    } }

    private lateinit var config: AutoMaterialPlayerConfig
    private var player: ExoPlayer? = null
    private var surfaceView: SurfaceView? = null
    private var isPlaying = false
    private var progressJobAction: Job? = null

    fun initializePlayer(context: Context, config: AutoMaterialPlayerConfig): Boolean {
        this.config = config
        val mediaItems = config.mediaItems

        if (player == null) {
            if (mediaItems.isEmpty()) {
                return false
            }
            val videoContainer = config.videoContainer
            if (surfaceView == null) {
                surfaceView = SurfaceView(context)
            }
            videoContainer.addView(surfaceView)
            val playerBuilder = ExoPlayer.Builder(context)
                .setMediaSourceFactory(DefaultMediaSourceFactory(context))

            player = playerBuilder.build().apply {
                addListener(PlayerEventListener())
                addAnalyticsListener(EventLogger())
                setAudioAttributes(AudioAttributes.DEFAULT,  /* handleAudioFocus= */true)
                playWhenReady = config.playWhenReady
                repeatMode = config.repeatMode
                setVideoSurfaceView(surfaceView)
            }
        }
        val hasStartItemPos = config.startItemPos in mediaItems.indices
        if (hasStartItemPos) {
            player?.seekTo(config.startItemPos, max(0, config.startPos))
        }
        player?.setMediaItems(mediaItems,  /* resetPosition= */!hasStartItemPos)
        player?.prepare()
        return true
    }

    fun releasePlayer() {
        player?.let {
            it.setVideoSurfaceView(null)
            it.release()
            player = null
            surfaceView = null
        }
    }

    fun play(forcePlay: Boolean = false) {
        player?.takeIf { forcePlay && it.playbackState == Player.STATE_ENDED }?.seekTo(0)
        player?.play()
    }

    fun pause() {
        player?.pause()
    }

    fun isPlaying() = player?.isPlaying == true || isPlaying
    fun getVideoSize(): Size? {
        return player?.videoSize?.let { Size(it.width, it.height) }
    }

    fun getVideoDuration(): Long {
        return player?.duration ?: 0
    }

    fun seekTo(progress: Int) {
        player?.seekTo(progress.toLong())
    }

    fun togglePlay() {
        if (isPlaying()) {
            pause()
        }else{
            play(true)
        }
    }

    fun adapterSurfaceView(initialized: Boolean, videoWith: Int, videoHeight: Int) {
        if (videoHeight <= 0) return
        val surfaceView = surfaceView ?: return
        val params = surfaceView.layoutParams as? LayoutParams ?: FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT)
        params.gravity = Gravity.CENTER
        val surfaceWidth = surfaceView.width
        val surfaceHeight = surfaceView.height

        if (videoWith > videoHeight) {
            //横屏
            val videoRatio = videoWith.toFloat()/ videoHeight
            val surfaceViewRatio = surfaceWidth.toFloat() / surfaceHeight
            if (videoRatio > surfaceViewRatio) {
                params.width = surfaceWidth
                params.height = (surfaceWidth / videoRatio).roundToInt()
            } else {
                params.width = (surfaceWidth * videoRatio).roundToInt()
                params.height = surfaceHeight
            }
        } else {
            //竖屏
            val videoRatio = videoHeight.toFloat() / videoWith
            val surfaceViewRatio = surfaceHeight.toFloat() / surfaceWidth
            if (videoRatio > surfaceViewRatio) {
                params.height = surfaceHeight
                params.width = (surfaceHeight/videoRatio).roundToInt()
            }else{
                params.height = (surfaceWidth*videoRatio).roundToInt()
                params.width = surfaceWidth
            }
        }
        if(surfaceWidth!= params.width|| surfaceHeight!= params.height){
            surfaceView.layoutParams = params
        }
    }

    private inner class PlayerEventListener : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: @Player.State Int) {
            super.onPlaybackStateChanged(playbackState)
            Log.d(TAG, "onPlaybackStateChanged : playbackState = $playbackState")
            playStateAction?.invoke(playbackState)
            when (playbackState) {
                Player.STATE_READY-> {
                    startProgressAction()
                }
                else -> {
                    cancelProgressAction()
                }
            }
        }

        private fun startProgressAction() {
            cancelProgressAction()
            progressJobAction = mainScope.launch {
                while (isActive){
                    delay(100)
                    updateProgress()
                }
            }
        }

        private fun updateProgress() {
            val player = player?: return
            val duration = player.duration
            val currentPosition = player.currentPosition
            val contentPosition = player.contentPosition
            val contentDuration = player.contentDuration
            val contentBufferedPosition = player.contentBufferedPosition
            playerProgressAction?.invoke(duration,currentPosition,contentPosition,contentDuration,contentBufferedPosition)
        }

        private fun cancelProgressAction() {
            progressJobAction?.cancel()
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            Log.d(TAG, "onPlayerError():  error = ${error.errorCode}, errorName = ${error.errorCodeName}")
            if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                player?.seekToDefaultPosition()
                player?.prepare()
            } else {
                playErrorAction?.invoke(error.errorCode, error.errorCodeName)
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            Log.d(TAG, "onIsPlayingChanged()h: isPlaying = $isPlaying")
            this@AutoMaterialPlayerWrapper.isPlaying = isPlaying
            playerPlayingAction?.invoke(isPlaying)
        }

        override fun onRenderedFirstFrame() {
            super.onRenderedFirstFrame()
            Log.d(TAG, "onRenderedFirstFrame() called")
            playerFirstFrameAction?.invoke()
        }

        override fun onEvents(player: Player, events: Player.Events) {
            super.onEvents(player, events)
            if (events.containsAny(Player.EVENT_PLAYBACK_STATE_CHANGED, Player.EVENT_PLAY_WHEN_READY_CHANGED, Player.EVENT_IS_PLAYING_CHANGED, Player.EVENT_AVAILABLE_COMMANDS_CHANGED)) {

            }

            if (events.containsAny(Player.EVENT_POSITION_DISCONTINUITY, Player.EVENT_TIMELINE_CHANGED, Player.EVENT_AVAILABLE_COMMANDS_CHANGED)) {

            }
        }

    }

    companion object {
        private const val TAG = "AutoPlayerWrapper"
    }
}