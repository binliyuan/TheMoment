package com.volcengine.effectone.auto.templates.ui.layer

import android.content.Context
import android.util.AttributeSet
import com.ss.android.ugc.cut_ui.TextItem
import com.volcengine.effectone.auto.templates.widget.player.PlayerControlView

/**
 * @author tyx
 * @description:
 * @date :2024/5/7 16:20
 */
class CutSamePlayerControlView : PlayerControlView<ICutSamePlayerControl, ICutSamePlayerLayer> {
	constructor(context: Context) : super(context)
	constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

	/**
	 * 是否显示切换模板面板
	 */
	fun onShowTemplatePanel(isShow: Boolean) {
		mControlLayers.forEach {
			it.value.onShowTemplatePanel(isShow)
		}
	}

	/**
	 * 是否显示编辑文字面板
	 */
	fun onShowEditTextPanel(isShow: Boolean) {
		mControlLayers.forEach {
			it.value.onShowEditTextPanel(isShow)
		}
	}

	/**
	 * 是否显示音乐面板
	 */
	fun onShowChangeMusicPanel(isShow: Boolean) {
		mControlLayers.forEach {
			it.value.onShowMusicPanel(isShow)
		}
	}

	/**
	 * 是否显示编辑面板
	 */
	fun onShowEditPanel(isShow: Boolean) {
		mControlLayers.forEach {
			it.value.onShowEditPanel(isShow)
		}
	}

	/**
	 * 视频合成后的textItems
	 */
	fun onTextItems(textItems: ArrayList<TextItem>?) {
		mControlLayers.forEach {
			it.value.onTextItems(textItems)
		}
	}

	/**
	 * 是否显示修改音乐
	 */
	fun onChangeMusicVisible(visible:Boolean) {
		mControlLayers.forEach {
			it.value.onChangeMusicVisible(visible)
		}
	}
}