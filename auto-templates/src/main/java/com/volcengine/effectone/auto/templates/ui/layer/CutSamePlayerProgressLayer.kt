package com.volcengine.effectone.auto.templates.ui.layer

import android.annotation.SuppressLint
import android.view.ViewGroup.MarginLayoutParams
import com.cutsame.solution.player.PlayerStateListener
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.utils.SizeUtil

/**
 * @author tyx
 * @description:进度条layer
 * @date :2024/4/28 16:16
 */
@SuppressLint("SimpleDateFormat")
class CutSamePlayerProgressLayer : PlayerProgressLayer<ICutSamePlayerControl>(), ICutSamePlayerLayer {

	override fun onStateChange(state: Int) {
		if (state == PlayerStateListener.PLAYER_STATE_PLAYING || state == -1) {
			mPlayerIcon?.setImageResource(R.drawable.icon_player_pause)
		} else {
			mPlayerIcon?.setImageResource(R.drawable.icon_player_play)
		}
	}

	/**
	 * 不显示切换模板后，底部docker显示，需要调整间距
	 */
	override fun onShowTemplatePanel(isShow: Boolean) {
		mViewBg?.let {
			val params = it.layoutParams as MarginLayoutParams
			val marginBottom = if (isShow) 0 else SizeUtil.dp2px(18f)
			params.bottomMargin = marginBottom
			it.layoutParams = params
		}
	}

	override fun onShowEditTextPanel(isShow: Boolean) {
		super.onShowEditTextPanel(isShow)
		onVisible(!isShow)
	}
}