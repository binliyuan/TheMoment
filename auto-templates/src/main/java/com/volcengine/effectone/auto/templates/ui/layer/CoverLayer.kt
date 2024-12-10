package com.volcengine.effectone.auto.templates.ui.layer

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import com.cutsame.solution.template.model.TemplateItem
import com.ss.ugc.android.editor.track.utils.visible
import com.volcengine.effectone.EffectOneSdk
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.widget.player.BaseVideoView
import com.volcengine.effectone.auto.templates.widget.player.IPlayerControl
import com.volcengine.effectone.auto.templates.widget.player.IPlayerLayer
import com.volcengine.effectone.image.ImageOption

/**
 * @author tyx
 * @description:
 * @date :2024/5/22 17:55
 */
class CoverLayer(private val templateItem: TemplateItem?) : IPlayerLayer<IPlayerControl> {

	private var mView: View? = null
	private var mImageView: ImageView? = null

	companion object {
		const val TAG = "CoverLayer"
	}

	override fun bindPlayerControl(playControl: IPlayerControl) {}

	override fun unBindPlayerControl() {}

	override fun tag(): String {
		return TAG
	}

	@SuppressLint("InflateParams")
	override fun getView(context: Context): View {
		val inf = LayoutInflater.from(context)
		return mView ?: inf.inflate(R.layout.layout_cover, null).also {
			mView = it
			mImageView = it.findViewById(R.id.iv_cover)
			val op = ImageOption.Builder().build()
			EffectOneSdk.imageLoader.loadImageView(mImageView!!, templateItem?.cover?.url, op)
		}
	}

	override fun onProgress(progress: Long, duration: Long) {}

	override fun onStateChange(state: Int) {
		onVisible(state == BaseVideoView.STATE_IDLE || state == BaseVideoView.STATE_PREPARING)
	}

	override fun onVisible(visible: Boolean) {
		mImageView?.visible = visible
	}
}