package com.volcengine.effectone.auto.templates.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.ss.android.ugc.cut_log.LogUtil

/**
 * @author tyx
 * @description:
 * @date :2024/5/27 18:22
 */
class CutSamePlayerSurfaceView : SurfaceView, SurfaceHolder.Callback {

	companion object {
		private const val TAG = "CutSamePlayerSurfaceView"
	}

	constructor(context: Context?) : this(context, null)
	constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
	constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
		holder.addCallback(this)
	}

	override fun surfaceCreated(holder: SurfaceHolder) {
		LogUtil.d(TAG, "surfaceCreated,width:$width,height:$height")
	}

	override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
		LogUtil.d(TAG, "surfaceChanged format:$format,width:$width,height:$height")
	}

	override fun surfaceDestroyed(holder: SurfaceHolder) {
		LogUtil.d(TAG, "surfaceDestroyed")
	}
}