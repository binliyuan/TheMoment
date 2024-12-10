package com.volcengine.effectone.auto.templates.widget.player

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView
import android.view.View
import com.ss.android.ugc.cut_log.LogUtil

/**
 * @author tyx
 * @description:
 * @date :2024/5/22 13:48
 */
class TextureRenderView : TextureView, IRenderView, TextureView.SurfaceTextureListener {

	companion object {
		private const val TAG = "TextureRenderView"
	}

	private var mPlayer: IVideoPlayer? = null
	private var mSurface: Surface? = null
	private var mSurfaceTexture: SurfaceTexture? = null
	private var mMeasureHelper: MeasureHelper? = null

	constructor(context: Context) : this(context, null)
	constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
		surfaceTextureListener = this
		mMeasureHelper = MeasureHelper()
	}

	override fun attachToPlayer(player: IVideoPlayer) {
		mPlayer = player
	}

	override fun setVideoSize(videoWidth: Int, videoHeight: Int) {
		if (videoWidth > 0 && videoHeight > 0) {
			mMeasureHelper?.setVideoSize(videoWidth, videoHeight)
			requestLayout()
		}
	}

	override fun setScaleType(scaleType: Int) {
		mMeasureHelper?.setScreenScale(scaleType)
		requestLayout()
	}

	override fun setVideoRotation(degree: Int) {
		mMeasureHelper?.setVideoRotation(degree)
		rotation = degree.toFloat()
	}

	override fun getView(): View {
		return this
	}

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)
		mMeasureHelper?.let {
			val size = it.doMeasure(widthMeasureSpec, heightMeasureSpec)
			setMeasuredDimension(size[0], size[1])
		}
	}

	override fun release() {
		runCatching {
			mSurface?.release()
			mSurfaceTexture?.release()
		}
		mSurface = null
		mSurfaceTexture = null
	}

	override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
		LogUtil.d(TAG, "onSurfaceTextureAvailable,width:$width,height:$height")
		if (mSurfaceTexture != null) {
			mSurfaceTexture?.let {
				setSurfaceTexture(it)
			}
		} else {
			mSurfaceTexture = surface
			mSurface = Surface(surface)
			mSurface?.let {
				mPlayer?.setSurface(it)
			}
		}
	}

	override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
		LogUtil.d(TAG, "onSurfaceTextureSizeChanged,width:$width,height:$height")
	}

	override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
		LogUtil.d(TAG, "onSurfaceTextureDestroyed")
		return false
	}

	override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
		LogUtil.d(TAG, "onSurfaceTextureUpdated")
	}
}