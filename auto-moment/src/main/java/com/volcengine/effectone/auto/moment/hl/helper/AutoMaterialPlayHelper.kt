package com.volcengine.effectone.auto.moment.hl.helper

import android.os.Build
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.common.utils.DurationFormatUtil
import com.volcengine.effectone.auto.moment.R
import com.volcengine.effectone.auto.moment.hl.player.AutoMaterialPlayerConfig
import com.volcengine.effectone.auto.moment.hl.player.AutoMaterialPlayerWrapper
import com.volcengine.effectone.auto.moment.hl.vm.AutoMaterialHighLightPlayVM
import com.volcengine.effectone.auto.moment.hl.widget.AutoHighLightSeekBar
import com.volcengine.effectone.extensions.setNoDoubleClickListener
import kotlin.math.roundToInt


class AutoMaterialPlayHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
) : IUIHelper {
    private val autoMaterialHighLightPlayVM by lazy { AutoMaterialHighLightPlayVM.get(activity) }
    private val autoMaterialPlayerWrapper by lazy { AutoMaterialPlayerWrapper() }


    private lateinit var videoContainer: FrameLayout
    private lateinit var config: AutoMaterialPlayerConfig
    private var playButton: ImageView? = null
    private var seekBar:AutoHighLightSeekBar? =null
    private var resumePlay = false

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate(){
        autoMaterialPlayerWrapper.apply {
            playerFirstFrameAction = {
                autoMaterialPlayerWrapper.getVideoSize()?.let {
                    Log.d(TAG, "onCreate() playerFirstFrameAction   size $it ")
                    autoMaterialHighLightPlayVM.updateVideoSize(it)
                }
                autoMaterialPlayerWrapper.getVideoDuration().let {
                    Log.d(TAG, "onCreate() playerFirstFrameAction   duration $it ")
                    autoMaterialHighLightPlayVM.updateVideoDuration(it)
                }
            }
            playErrorAction = { code, msg ->

            }
            playStateAction = {playbackState ->

            }
            playerProgressAction = { duration: Long, currentPosition: Long, _: Long, _: Long, _: Long ->
                seekBar?.progress = (currentPosition.toFloat() / duration * 100).roundToInt()
            }
            playerPlayingAction = {isPlaying ->
                playButton?.setImageResource(if (isPlaying) R.drawable.auto_material_recognize_pause_button_icon else R.drawable.auto_material_recognize_play_button_icon)
            }
        }
    }


    override fun initView(rootView: ViewGroup) {
        val videoDuration = autoMaterialHighLightPlayVM.videoDuration
        val durationView =  rootView.findViewById<TextView>(R.id.auto_material_play_bottom_controller_duration)?.apply {
            val textWidth =
                paint.measureText(" / ${DurationFormatUtil.stringForTime(autoMaterialHighLightPlayVM.videoDuration)}")
            layoutParams = LinearLayout.LayoutParams((textWidth*2).toInt(),ViewGroup.LayoutParams.MATCH_PARENT)
        }
        updateDuration(durationView,0,videoDuration)
        videoContainer = rootView.findViewById(R.id.auto_material_play_surface_container)
        playButton = rootView.findViewById<ImageView>(R.id.auto_material_play_bottom_controller_play)?.apply {
            setNoDoubleClickListener {
                autoMaterialPlayerWrapper.togglePlay()
            }
        }
        seekBar=  rootView.findViewById<AutoHighLightSeekBar>(R.id.auto_material_play_bottom_controller_seekBar)?.apply {
            setHighLightRange(
                autoMaterialHighLightPlayVM.highLightStartTime.toFloat()/videoDuration,
                autoMaterialHighLightPlayVM.highLightEndTime.toFloat()/videoDuration
            )
            setOnSeekBarChangeListener(object :OnSeekBarChangeListener{
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    seekBar?.takeIf { fromUser && videoDuration> 0  }?.let {
                        autoMaterialPlayerWrapper.seekTo((it.progress.toFloat() / 100 * videoDuration).roundToInt())
                    }
                    seekBar?.let {
                        updateDuration(durationView,it.progress,videoDuration)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) =Unit

                override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

            })
        }

        config = AutoMaterialPlayerConfig(
            videoContainer,
            autoMaterialHighLightPlayVM.mediaItems,
            false,
            startPos = autoMaterialHighLightPlayVM.highLightStartTime.toLong() //seek到hl start
        )

        initObserver()

        if (autoMaterialHighLightPlayVM.videoWith != -1 && autoMaterialHighLightPlayVM.videoHeight != -1) {
            videoContainer.post {
                adapterSurfaceView()
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        if (Build.VERSION.SDK_INT > 23) {
            autoMaterialPlayerWrapper.initializePlayer(activity,config)
        }
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        if (Build.VERSION.SDK_INT <= 23 ) {
            autoMaterialPlayerWrapper.initializePlayer(activity,config)
        }
        if (resumePlay) {
            resumePlay = false
            autoMaterialPlayerWrapper.play()
        }
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        if (Build.VERSION.SDK_INT <= 23) {
            autoMaterialPlayerWrapper.pause()
            resumePlay =  autoMaterialPlayerWrapper.isPlaying()
        }
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        if (Build.VERSION.SDK_INT > 23) {
            autoMaterialPlayerWrapper.pause()
            resumePlay =  autoMaterialPlayerWrapper.isPlaying()
        }
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        autoMaterialPlayerWrapper.releasePlayer()
    }

    private fun initObserver() {
        autoMaterialHighLightPlayVM.releasePlayerAction.observe(owner){
            it?.let {

            }
        }
        autoMaterialHighLightPlayVM.highLightPlayAction.observe(owner){
            it?.let {
                autoMaterialPlayerWrapper.seekTo(autoMaterialHighLightPlayVM.highLightStartTime)
                if (!autoMaterialPlayerWrapper.isPlaying()) {
                    autoMaterialPlayerWrapper.play()
                }
            }
        }
    }
    private fun updateDuration(durationView:TextView?,progress: Int, videoDuration: Long) {
        durationView?.run {
            text = SpannableStringBuilder().apply {
                SpannableString(DurationFormatUtil.stringForTime((progress.toFloat() / 100 * videoDuration).toLong(), 0)).apply {
                    setSpan(ForegroundColorSpan(ContextCompat.getColor(activity,com.volcengine.effectone.auto.common.R.color.color_EEEEEE)), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    append(this)
                }
                SpannableString(" / ${DurationFormatUtil.stringForTime(videoDuration)}").apply {
                    setSpan(ForegroundColorSpan(ContextCompat.getColor(activity,com.volcengine.effectone.auto.common.R.color.color_60EEEEEE)), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    append(this)
                }
            }
        }
    }

    private fun adapterSurfaceView() {
        val videoWith = autoMaterialHighLightPlayVM.videoWith
        val videoHeight = autoMaterialHighLightPlayVM.videoHeight
        autoMaterialPlayerWrapper.adapterSurfaceView(this::videoContainer.isInitialized,videoWith,videoHeight)
    }

    companion object {
        private const val TAG = "AutoMaterialPlayHelper"

    }
}