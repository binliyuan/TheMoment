package com.volcengine.effectone.auto.templates.vm

import android.graphics.Bitmap
import android.graphics.RectF
import android.view.SurfaceView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.bytedance.ies.nle.editor_jni.INLEMediaRuntime
import com.cutsame.solution.CutSameSolution
import com.cutsame.solution.player.CutSamePlayer
import com.cutsame.solution.player.GetImageListener
import com.cutsame.solution.player.PlayerStateListener
import com.cutsame.solution.player.audio.ICutSameAudio
import com.cutsame.solution.player.config.CutSamePlayerInitConfig
import com.cutsame.solution.source.CutSameSource
import com.ss.android.ugc.cut_log.LogUtil
import com.ss.android.ugc.cut_ui.MediaItem
import com.ss.android.ugc.cut_ui.TextItem
import com.volcengine.ck.logkit.LogKit
import com.volcengine.effectone.auto.templates.event.TrackNodeEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

/**
 * @author tyx
 * @description: 播放器相关
 * @date :2024/4/28 18:01
 */
class CutSamePlayerViewModel : ViewModel() {

	companion object {
		private const val TAG = "CutSamePlayerViewModel"
		const val CREATE = 1
		const val PREPARED = 2
		fun get(owner: ViewModelStoreOwner): CutSamePlayerViewModel {
			return ViewModelProvider(owner).get(CutSamePlayerViewModel::class.java)
		}
	}

	private val mScope by lazy { CoroutineScope(Dispatchers.Main + SupervisorJob()) }
	private var mCutSamePlayer: CutSamePlayer? = null
	val mPlayerState = MutableLiveData<Int>()
	val mCurrentEditMediaItem = MutableLiveData<MediaItem?>(null)
	val mShowEditTextPanel = MutableLiveData(false)
	val mShowChangeMusic = MutableLiveData(false)
	val mShowVoicePanel = MutableLiveData(false)
	val mLaunchPicker = MutableLiveData<Pair<List<MediaItem>, Boolean>>()
	val mLaunchClip = MutableLiveData<MediaItem>()
	val mFrameBitmap = MutableLiveData<MutableMap<Int, Bitmap?>>()

	fun initPlayer(cutSameSource: CutSameSource?, surfaceView: SurfaceView): CutSamePlayer? {
		LogUtil.d(TAG, "initPlayer")
		mCutSamePlayer?.release()
		val config = CutSamePlayerInitConfig().apply {
			previewSurfaceSize = 1920 to 1080
		}
		CutSameSolution.createCutSamePlayer(
			surfaceView,
			cutSameSource,
			config
		).also { player ->
			mCutSamePlayer = player
		}
		mPlayerState.value = CREATE
		return mCutSamePlayer
	}

	fun setPlayerDisplayState(scaleW: Float, scaleH: Float, transX: Int, transY: Int) {
		mCutSamePlayer?.getMediaRuntimeApi()?.setDisplayState(scaleW, scaleH, 0f, transX, transY)
	}

	fun getMediaRuntimeApi(): INLEMediaRuntime? {
		return mCutSamePlayer?.getMediaRuntimeApi()
	}

	fun playerPrepare(finalMediaItems: List<MediaItem>, textItems: List<TextItem>?) {
		LogUtil.d(TAG, "playerPrepare")
		mCutSamePlayer?.preparePlay(finalMediaItems, textItems)
		mPlayerState.value = PREPARED
	}

	fun registerPlayerStateListener(playerStateListener: PlayerStateListener) {
		LogUtil.d(TAG, "registerPlayerStateListener")
		unRegisterPlayerStateListener(playerStateListener)
		mCutSamePlayer?.registerPlayerStateListener(playerStateListener)
	}

	fun unRegisterPlayerStateListener(playerStateListener: PlayerStateListener) {
		LogUtil.d(TAG, "unRegisterPlayerStateListener")
		mCutSamePlayer?.unRegisterPlayerStateListener(playerStateListener)
	}

	fun start() {
		LogUtil.d(TAG, "start")
		mCutSamePlayer?.start()
	}

	fun pause() {
		LogUtil.d(TAG, "pause")
		mCutSamePlayer?.pause()
	}

	fun seekTo(position: Int, autoPlay: Boolean, callback: (Int) -> Unit = {}) {
		LogUtil.d(TAG, "seekTo position:$position,autoPlay:$autoPlay")
		mCutSamePlayer?.seekTo(position, autoPlay) {
			callback.invoke(it)
		}
	}

	fun updateMedia(materialId: String, mediaItem: MediaItem) {
		LogUtil.d(TAG, "updateMedia materialId:$materialId")
		mCutSamePlayer?.pause()
		mCutSamePlayer?.updateMedia(materialId, mediaItem)
	}

	fun getVolume(materialId: String): Float? {
		return mCutSamePlayer?.getVolume(materialId).also {
			LogUtil.d(TAG, "getVolume materialId:$materialId,value = $it")
		}
	}

	fun setVolume(materialId: String, volume: Float) {
		LogUtil.d(TAG, "updateMedia materialId:$materialId,volume:$volume")
		mCutSamePlayer?.setVolume(materialId, volume)
	}

	fun getTextItems(needFilterMutable: Boolean = true): ArrayList<TextItem>? {
		return mCutSamePlayer?.getTextItems(needFilterMutable)
	}

	fun getAudioManager(): ICutSameAudio? {
		return mCutSamePlayer?.getAudioManager()
	}

	fun getCanvasSize(): IntArray? {
		var width = 0
		var height = 0
		mCutSamePlayer?.getMediaRuntimeApi()?.canvasSize?.also {
			width = it.first
			height = it.second
		}
		if (width == 0 || height == 0) {
			val configCanvasSize = mCutSamePlayer?.getConfigCanvasSize()
			width = configCanvasSize?.width ?: width
			height = configCanvasSize?.height ?: height
		}
		return if (width == 0 || height == 0) null else intArrayOf(width, height)
	}

	fun getItemFrameBitmap(times: IntArray, width: Int, height: Int) {
		mScope.launch(Dispatchers.IO) {
			mCutSamePlayer?.cancelGetVideoFrames()
			val bitmapMap = hashMapOf<Int, Bitmap?>()
			var count = 0
			mCutSamePlayer?.getVideoFrameWithTime(times, width, height, object : GetImageListener {
				override fun onGetImageData(bytes: ByteArray?, pts: Int, width: Int, height: Int, score: Float) {
					count++
					if (bytes != null) {
						val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
						bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(bytes))
						bitmapMap[pts] = bitmap
					} else {
						mCutSamePlayer?.cancelGetVideoFrames()
						bitmapMap[pts] = null
					}
					if (count == times.size) {
						LogUtil.d(TAG, "getItemFrameBitmap complete,size:$count")
						mFrameBitmap.postValue(bitmapMap)
					}
				}
			})
		}
	}

	fun setTextAnimSwitch(materialId: String?, enable: Boolean) {
		if (materialId.isNullOrEmpty()) return
		mCutSamePlayer?.setTextAnimSwitch(materialId, enable)
	}

	fun getTextSegment(materialId: String, rectF: RectF) {
		mCutSamePlayer?.getTextSegment(materialId, rectF)
	}

	fun updateText(materialId: String, text: String) {
		mCutSamePlayer?.updateText(materialId, text)
	}

	private var mPlayFpsJob: Job? = null
	fun onStartPlayFps() {
		mPlayFpsJob?.cancel()
		mPlayFpsJob = mScope.launch {
			while (isActive) {
				getMediaRuntimeApi()?.let { api ->
					TrackNodeEvent.onPlayFps(api.playFps)
					delay(1000)
				}
			}
		}
	}

	fun onStopPlayFps() {
		mPlayFpsJob?.cancel()
	}

	override fun onCleared() {
		super.onCleared()
		mScope.cancel()
		mCutSamePlayer?.cancelGetVideoFrames()
		//这里不要再release了，已经在playerControl中release，重复release native层闪退
		mCutSamePlayer = null
		LogKit.d(TAG, "onCleared")
	}
}