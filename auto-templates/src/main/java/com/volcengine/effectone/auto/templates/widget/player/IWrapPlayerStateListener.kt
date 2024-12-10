package com.volcengine.effectone.auto.templates.widget.player

import com.cutsame.solution.player.PlayerStateListener

/**
 * @author tyx
 * @description:
 * @date :2024/5/7 20:20
 */
interface IWrapPlayerStateListener : PlayerStateListener {
	override fun onChanged(state: Int) {}
	override fun onFirstFrameRendered() {}
	override fun onPlayEof() {}
	override fun onPlayError(what: Int, extra: String) {}
	override fun onPlayProgress(process: Long) {}
}