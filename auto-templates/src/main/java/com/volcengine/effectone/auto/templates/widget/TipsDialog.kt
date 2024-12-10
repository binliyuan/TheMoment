package com.volcengine.effectone.auto.templates.widget

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.ss.android.ugc.aweme.views.setDebounceOnClickListener
import com.volcengine.effectone.auto.templates.R

/**
 * @author tyx
 * @description:
 * @date :2024/5/15 10:01
 */
class TipsDialog(context: Context, theme: Int, private val mBuilder: Builder) : Dialog(context, theme) {

	private lateinit var mTvTitle: TextView
	private lateinit var mTvMsg: TextView
	private lateinit var mBtnConfirm: Button
	private lateinit var mBtnCancel: Button

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.dialog_tips)
		mTvTitle = findViewById(R.id.tv_title)
		mTvMsg = findViewById(R.id.tv_msg)
		mBtnConfirm = findViewById(R.id.btn_confirm)
		mBtnCancel = findViewById(R.id.btn_cancel)
		setCanceledOnTouchOutside(mBuilder.canOutSide)
		mTvTitle.text = mBuilder.title
		mTvMsg.text = mBuilder.msg
		mBtnConfirm.text = mBuilder.sure
		mBtnCancel.text = mBuilder.cancel
		mBtnCancel.setDebounceOnClickListener {
			mBuilder.dialogOperationListener?.onClickCancel()
			dismiss()
		}
		mBtnConfirm.setDebounceOnClickListener {
			mBuilder.dialogOperationListener?.onClickSure()
			dismiss()
		}
	}

	class Builder {
		internal var canOutSide = false
		internal var msg: String = ""
		internal var title: String = ""
		internal var sure: String = "确定"
		internal var cancel: String = "取消"
		internal var dialogOperationListener: DialogOperationListener? = null

		fun setCanOutSide(outSide: Boolean): Builder {
			this.canOutSide = outSide
			return this
		}

		fun setTitleText(title: String): Builder {
			this.title = title
			return this
		}

		fun setMessage(message: String): Builder {
			this.msg = message
			return this
		}

		fun setSureText(sure: String): Builder {
			this.sure = sure
			return this
		}

		fun setCancelText(cancel: String): Builder {
			this.cancel = cancel
			return this
		}

		fun setDialogOperationListener(dialogOperationListener: DialogOperationListener): Builder {
			this.dialogOperationListener = dialogOperationListener
			return this
		}

		fun create(context: Context): TipsDialog {
			return TipsDialog(context, R.style.Dialog, this)
		}
	}

	interface DialogOperationListener {
		fun onClickSure()
		fun onClickCancel()
	}
}