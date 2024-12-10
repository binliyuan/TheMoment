package com.volcengine.effectone.auto.templates.widget

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.widget.EOLoadingImageView

/**
 * @author tyx
 * @description:
 * @date :2024/4/25 17:50
 */
class LoadingDialog(context: Context) : Dialog(context, R.style.dialog_fullscreen) {

	private lateinit var mTextView: TextView
	private lateinit var mProgress: EOLoadingImageView
	private var mCreated: Boolean = false
	private var mMessage: String = "加载中..."

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.layout_progress_loading_dialog)
		mTextView = findViewById(R.id.tv_content)
		mProgress = findViewById(R.id.progress)
		mProgress.tag = "LoadingDialog"
		mCreated = true
		setMessage(mMessage)
		setCanceledOnTouchOutside(false)
		setCancelable(false)
	}

	fun setMessage(message: String) {
		if (mCreated) {
			mTextView.text = message
		}
		mMessage = message
	}
}