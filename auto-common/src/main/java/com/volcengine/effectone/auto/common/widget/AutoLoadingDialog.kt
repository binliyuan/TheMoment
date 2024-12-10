package com.volcengine.effectone.auto.common.widget

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.StringRes
import com.airbnb.lottie.LottieAnimationView
import com.volcengine.effectone.auto.common.R
import com.volcengine.effectone.widget.BaseDialog

class AutoLoadingDialog(context: Context, theme: Int = R.style.AutoTranslucentDialog) :
    BaseDialog(context, theme) {
    private var loadingView: LottieAnimationView? = null
    private var loadingTipView: TextView? = null

    var buider: Builder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.auto_common_layout_dialog_loading)
        loadingView = findViewById(R.id.auto_common_loading_view)
        loadingTipView = findViewById(R.id.auto_common_loading_tip)
        initBuilder()
    }

    private fun initBuilder() {
        buider?.run {
            this@AutoLoadingDialog.setCanceledOnTouchOutside(this.mCanceledOnTouchOutside)
            this@AutoLoadingDialog.setCancelable(this.mCancelable)
            loadingTipView?.text = this.mTipMsg
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        loadingView?.playAnimation()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        loadingView?.pauseAnimation()
    }

    fun setTipMessage(tipMsg: String): AutoLoadingDialog {
        loadingTipView?.text = tipMsg
        return this
    }

    fun setTipMessage(@StringRes tipMsgRes: Int): AutoLoadingDialog {
        loadingTipView?.text = context.getText(tipMsgRes)
        return this
    }

    override fun show() {
        super.show()
        loadingView?.playAnimation()
    }

    override fun dismiss() {
        super.dismiss()
        loadingView?.pauseAnimation()
    }

    class Builder(val context: Context) {
        internal var mTipMsg = ""
        internal var mCancelable = false
        internal var mCanceledOnTouchOutside = false
        fun setTipMsg(tipMsg: String): Builder {
            this.mTipMsg = tipMsg
            return this
        }

        fun setCancelable(cancelable: Boolean): Builder {
            this.mCancelable = cancelable
            return this
        }

        fun setCanceledOnTouchOutside(cancel: Boolean): Builder {
            this.mCanceledOnTouchOutside = cancel
            return this
        }

        fun create(): AutoLoadingDialog {
            return AutoLoadingDialog(context).apply {
                buider = this@Builder
            }
        }

        fun show(): AutoLoadingDialog {
            return create().apply {
                show()
            }
        }

    }
}