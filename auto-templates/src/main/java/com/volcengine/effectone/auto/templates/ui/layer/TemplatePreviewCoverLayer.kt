package com.volcengine.effectone.auto.templates.ui.layer

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.cutsame.solution.template.model.TemplateItem
import com.ss.android.ugc.aweme.views.setDebounceOnClickListener
import com.ss.ugc.android.editor.track.utils.visible
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.widget.player.BaseVideoView
import com.volcengine.effectone.auto.templates.widget.player.IPlayerControl
import com.volcengine.effectone.auto.templates.widget.player.IPlayerLayer
import java.text.SimpleDateFormat
import java.util.Date

/**
 * @author tyx
 * @description:
 * @date :2024/5/23 9:44
 */
class TemplatePreviewCoverLayer(private var mTemplateItem: TemplateItem) : IPlayerLayer<IPlayerControl> {

	companion object {
		private const val TAG = "TemplatePreviewCoverLayer"
	}

	var mUseBlock: ((TemplateItem) -> Unit)? = null

	private var mPlayerControl: IPlayerControl? = null
	private var mView: View? = null
	private var mIvPlay: ImageView? = null
	private var mTvContent: TextView? = null
	private var mBtnUse: Button? = null
	private var mTvInfo: TextView? = null

	override fun bindPlayerControl(playControl: IPlayerControl) {
		this.mPlayerControl = playControl
	}

	override fun unBindPlayerControl() {
		mPlayerControl = null
	}

	override fun tag(): String {
		return TAG
	}

	override fun getView(context: Context): View {
		val inf = LayoutInflater.from(context)
		return mView ?: inf.inflate(R.layout.layout_template_preview_cover, null).also {
			mView = it
			initView(it)
		}
	}

	private fun initView(view: View) {
		mIvPlay = view.findViewById(R.id.iv_play)
		mBtnUse = view.findViewById(R.id.btn_use)
		mTvContent = view.findViewById(R.id.tv_content)
		mTvInfo = view.findViewById(R.id.tv_info)
		val text = String.format("%d个素材", mTemplateItem.fragmentCount)
		mTvInfo?.text = text
		mTvContent?.text = mTemplateItem.title
		mIvPlay?.setDebounceOnClickListener {
			mPlayerControl?.start()
		}
		mBtnUse?.setDebounceOnClickListener {
			mUseBlock?.invoke(mTemplateItem)
		}
	}

	override fun onProgress(progress: Long, duration: Long) {}

	override fun onStateChange(state: Int) {
		onVisible(
			state == BaseVideoView.STATE_IDLE ||
					state == BaseVideoView.STATE_PAUSED
		)
	}

	override fun onVisible(visible: Boolean) {
		mView?.visible = visible
	}
}