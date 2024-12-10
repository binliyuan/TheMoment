package com.volcengine.effectone.auto.templates.widget.player

import android.view.View
import com.volcengine.effectone.auto.templates.widget.player.IRenderView

/**
 * @author tyx
 * @description:
 * @date :2024/5/22 13:53
 */
class MeasureHelper {

	private var mVideoWidth = 0
	private var mVideoHeight = 0
	private var mCurrentScreenScale = 0
	private var mVideoRotationDegree = 0

	fun setVideoRotation(videoRotationDegree: Int) {
		mVideoRotationDegree = videoRotationDegree
	}

	fun setVideoSize(width: Int, height: Int) {
		mVideoWidth = width
		mVideoHeight = height
	}

	fun setScreenScale(screenScale: Int) {
		mCurrentScreenScale = screenScale
	}

	/**
	 * 注意：VideoView的宽高一定要定死，否者以下算法不成立
	 */
	fun doMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int): IntArray {
		var widthSpec = widthMeasureSpec
		var heightSpec = heightMeasureSpec

		if (mVideoRotationDegree == 90 || mVideoRotationDegree == 270) {
			val tempSpec = widthSpec
			widthSpec = heightSpec
			heightSpec = tempSpec
		}

		var width = View.MeasureSpec.getSize(widthSpec)
		var height = View.MeasureSpec.getSize(heightSpec)

		if (mVideoHeight == 0 || mVideoWidth == 0) {
			return intArrayOf(width, height)
		}

		//如果设置了比例
		when (mCurrentScreenScale) {
			IRenderView.SCREEN_SCALE_DEFAULT -> if (mVideoWidth * height < width * mVideoHeight) {
				width = height * mVideoWidth / mVideoHeight
			} else if (mVideoWidth * height > width * mVideoHeight) {
				height = width * mVideoHeight / mVideoWidth
			}
			IRenderView.SCREEN_SCALE_ORIGINAL -> {
				width = mVideoWidth
				height = mVideoHeight
			}
			IRenderView.SCREEN_SCALE_16_9 -> if (height > width / 16 * 9) {
				height = width / 16 * 9
			} else {
				width = height / 9 * 16
			}
			IRenderView.SCREEN_SCALE_4_3 -> if (height > width / 4 * 3) {
				height = width / 4 * 3
			} else {
				width = height / 3 * 4
			}

			IRenderView.SCREEN_SCALE_MATCH_PARENT -> {
				width = widthMeasureSpec
				height = heightMeasureSpec
			}
			IRenderView.SCREEN_SCALE_CENTER_CROP -> if (mVideoWidth * height > width * mVideoHeight) {
				width = height * mVideoWidth / mVideoHeight
			} else {
				height = width * mVideoHeight / mVideoWidth
			}

			else -> if (mVideoWidth * height < width * mVideoHeight) {
				width = height * mVideoWidth / mVideoHeight
			} else if (mVideoWidth * height > width * mVideoHeight) {
				height = width * mVideoHeight / mVideoWidth
			}
		}
		return intArrayOf(width, height)
	}
}