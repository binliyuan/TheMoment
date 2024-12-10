package com.volcengine.effectone.auto.templates.ui.layer

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.ss.android.ugc.aweme.views.setDebounceOnClickListener
import com.ss.android.ugc.cut_ui.TextItem
import com.ss.ugc.android.editor.track.utils.visible
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.widget.CustomButtonLayout

/**
 * @author tyx
 * @description:
 * @date :2024/5/7 15:05
 */
class CutSamePlayerEditLayer : ICutSamePlayerLayer {

	companion object {
		const val TAG = "CutSamePlayerEditLayer"
	}

	private var mPlayerControl: ICutSamePlayerControl? = null
	private var mView: View? = null
	private lateinit var mEditTextLayout: CustomButtonLayout
	private lateinit var mChangeMusicLayout: CustomButtonLayout
	private var mShowTemplatePanel = false
	private var mShowTextEditPanel = false
	private var mShowEditPanel = false

	override fun bindPlayerControl(playControl: ICutSamePlayerControl) {
		mPlayerControl = playControl
	}

	override fun unBindPlayerControl() {
		mPlayerControl = null
	}

	override fun tag(): String = TAG

	@SuppressLint("InflateParams")
	override fun getView(context: Context): View {
		val inf = LayoutInflater.from(context)
		return mView ?: inf.inflate(R.layout.layout_cutsame_player_edit_layer, null).also {
			mView = it
			onVisible(false)
			initView(it)
		}
	}

	private fun initView(view: View) {
		mChangeMusicLayout = view.findViewById(R.id.layout_change_music)
		mEditTextLayout = view.findViewById(R.id.layout_edit_text)
		mChangeMusicLayout.setDebounceOnClickListener {
			val checkState = !mChangeMusicLayout.getCheckState()
			mChangeMusicLayout.setCheckState(checkState)
			mPlayerControl?.onChangeMusic(checkState)
		}
		mEditTextLayout.setDebounceOnClickListener {
			val checkState = !mEditTextLayout.getCheckState()
			mEditTextLayout.setCheckState(checkState)
			mPlayerControl?.onCheckEditText(checkState)
		}
	}

	override fun onProgress(progress: Long, duration: Long) {

	}

	override fun onStateChange(state: Int) {

	}

	override fun onVisible(visible: Boolean) {
		mView?.visible = visible
	}

	override fun onShowTemplatePanel(isShow: Boolean) {
		mShowTemplatePanel = isShow
		showOrHide()
	}

	override fun onShowEditPanel(isShow: Boolean) {
		mShowEditPanel = isShow
		showOrHide()
	}

	override fun onShowEditTextPanel(isShow: Boolean) {
		super.onShowEditTextPanel(isShow)
		mShowTextEditPanel = isShow
		showOrHide()
		if (isShow == mEditTextLayout.getCheckState()) return
		mEditTextLayout.setCheckState(isShow)
	}

	private fun showOrHide() {
		if (mShowTemplatePanel || mShowTextEditPanel || mShowEditPanel) {
			onVisible(false)
		} else {
			onVisible(true)
		}
	}

	override fun onShowMusicPanel(isShow: Boolean) {
		super.onShowMusicPanel(isShow)
		//因为显示切换音乐pop点击外部事件会穿透，避免重复弹出，pop显示时不可点击，关闭后恢复。
		mChangeMusicLayout.isEnabled = !isShow
		if (isShow == mChangeMusicLayout.getCheckState()) return
		mChangeMusicLayout.setCheckState(isShow)
	}

	override fun onTextItems(textItems: List<TextItem>?) {
		super.onTextItems(textItems)
		mEditTextLayout.visible = textItems?.any { it.mutable } ?: false
	}

	override fun onChangeMusicVisible(visible: Boolean) {
		super.onChangeMusicVisible(visible)
		mChangeMusicLayout.visible = visible
	}
}