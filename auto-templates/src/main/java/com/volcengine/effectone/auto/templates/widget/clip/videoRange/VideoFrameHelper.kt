package com.volcengine.effectone.auto.templates.widget.clip.videoRange

import android.graphics.Bitmap
import com.bytedance.ies.cutsame.util.VEUtils
import com.ss.android.ugc.cut_log.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * @author tyx
 * @description:
 * @date :2024/5/10 17:45
 */
class VideoFrameHelper {

	companion object {
		private const val TAG = "VideoFrameHelper"
	}

	private val mScope by lazy { CoroutineScope(Dispatchers.IO + SupervisorJob()) }
	private var mListener: VideoFrameListener? = null
	private val mBitmapCache by lazy { ConcurrentHashMap<String, Bitmap>() }
	private val mFrameChannel by lazy { Channel<Triple<Boolean, String, Bitmap>>() }

	fun setFrameListener(listener: VideoFrameListener) {
		this.mListener = listener
	}

	init {
		mScope.launch(Dispatchers.Main) {
			for (data in mFrameChannel) {
				mBitmapCache.put(data.second, data.third)
				if (data.first) mListener?.onRefresh()
			}
		}
	}

	fun startGetFrame(path: String, ptsMsList: IntArray, width: Int, height: Int, isRough: Boolean) {
		mScope.launch {
			var loadCount = 0
			VEUtils.getVideoFrames(path, ptsMsList, width, height, isRough) { frame, width, height, ptsMs ->
				loadCount++
				LogUtil.d(TAG, "count:$loadCount,ptsMs:$ptsMs")
				val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
				bitmap.copyPixelsFromBuffer(frame.position(0))
				val key = getKey(path, ptsMs)
				if (isActive) mBitmapCache[key] = bitmap
				if (loadCount == ptsMsList.size || loadCount % 10 == 0) {
					LogUtil.d(TAG, "refresh")
					mListener?.onRefresh()
				}
				loadCount != ptsMsList.size && isActive
			}
		}
	}

	fun getFrame(path: String, pts: Int): Bitmap? {
		return mBitmapCache[getKey(path, pts)]
	}

	private fun getKey(path: String, pts: Int) = "$path#$pts"

	fun release() {
		mListener = null
		mScope.cancel()
		mBitmapCache.forEach {
			it.value.recycle()
		}
		mBitmapCache.clear()
	}

	interface VideoFrameListener {
		fun onRefresh()
	}
}