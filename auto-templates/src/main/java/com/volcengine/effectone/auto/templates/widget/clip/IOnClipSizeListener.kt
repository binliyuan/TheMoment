package com.volcengine.effectone.auto.templates.widget.clip

/**
 * @author tyx
 * @description:
 * @date :2024/5/16 17:59
 */
interface IOnClipSizeListener {
	fun onMove(isTouch: Boolean, scale: Float, translateX: Float, translateY: Float)
	fun onDown()
	fun onUp()
}