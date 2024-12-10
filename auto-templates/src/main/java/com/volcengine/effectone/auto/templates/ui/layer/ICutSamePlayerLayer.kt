package com.volcengine.effectone.auto.templates.ui.layer

import com.ss.android.ugc.cut_ui.TextItem
import com.volcengine.effectone.auto.templates.widget.player.IPlayerLayer

/**
 * @author tyx
 * @description:control层通知layer
 * @date :2024/5/7 16:20
 */
interface ICutSamePlayerLayer : IPlayerLayer<ICutSamePlayerControl> {
	fun onShowTemplatePanel(isShow: Boolean) {}
	fun onShowEditTextPanel(isShow: Boolean) {}
	fun onShowMusicPanel(isShow: Boolean) {}
	fun onShowEditPanel(isShow: Boolean) {}
	fun onTextItems(textItems: List<TextItem>?) {}
	fun onChangeMusicVisible(visible: Boolean) {}
}