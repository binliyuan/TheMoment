package com.volcengine.effectone.auto.templates.ui.layer

import com.volcengine.effectone.auto.templates.widget.player.IPlayerControl

/**
 * @author tyx
 * @description:用于layer层来控制control
 * @date :2024/5/8 20:24
 */
interface ICutSamePlayerControl : IPlayerControl {
	fun onCheckEditText(check: Boolean)
	fun onChangeMusic(check: Boolean)
}