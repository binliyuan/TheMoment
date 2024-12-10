package com.volcengine.effectone.auto.templates.widget.player

import android.content.Context
import android.util.AttributeSet

/**
 * @author tyx
 * @description:
 * @date :2024/5/22 14:33
 */
class PlayerView : BaseVideoView {
	constructor(context: Context) : super(context)
	constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

	override fun createPlayer(context: Context): IVideoPlayer {
		return ExoVideoPlayer(context)
	}

	override fun createRenderView(context: Context): IRenderView {
		return TextureRenderView(context)
	}
}