package com.volcengine.effectone.auto.templates.widget

import android.app.Dialog
import android.content.Context
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.utils.EOUtils

class CutSameReplaceNoticeDialog(context: Context, theme: Int) : Dialog(context, theme) {

    class Builder(private var context: Context) {
        private var title: String = ""
        private var subTitle: String = ""
        private var confirmText: String = context.getString(R.string.eo_cutsame_dialog_confirm)
        private var cancelText: String = context.getString(R.string.eo_cutsame_dialog_cancel)
        private var dialogOperationListener: DialogOperationListener? = null

        fun setTitleText(title: String): Builder {
            this.title = title
            return this
        }

        fun setSubTitleText(subTitle: String): Builder {
            this.subTitle = subTitle
            return this
        }

        fun setConfirmText(confirmText: String): Builder {
            this.confirmText = confirmText
            return this
        }

        fun setCancelText(cancelText: String): Builder {
            this.cancelText = cancelText
            return this
        }

        fun setDialogOperationListener(dialogOperationListener: DialogOperationListener): Builder {
            this.dialogOperationListener = dialogOperationListener
            return this
        }

        fun create(): CutSameReplaceNoticeDialog {
            val dialog = CutSameReplaceNoticeDialog(context, R.style.Dialog)
            val view: View = LayoutInflater.from(context).inflate(R.layout.eo_cutsame_dialog_replace_notice, null)
            initTitle(view)
            initSubTitle(view)
            initConfirmText(view)
            initCancelText(view)
            initListener(dialog, view)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(view)
            val window = dialog.window
            val attributes = window?.attributes
            attributes?.width = EOUtils.sizeUtil.dp2px(435f)
            attributes?.height = EOUtils.sizeUtil.dp2px(256f)
            window?.attributes = attributes
            return dialog
        }

        private fun initListener(dialog: Dialog, view: View) {
            view.findViewById<TextView>(R.id.cancelBtn).setOnClickListener {
                dialogOperationListener?.onClickCancel()
                dialog.dismiss()
            }

            view.findViewById<TextView>(R.id.sureBtn).setOnClickListener {
                dialogOperationListener?.onClickSure()
                dialog.dismiss()
            }
        }

        private fun initTitle(view: View) {
            view.findViewById<TextView>(R.id.titleView).text = title
        }

        private fun initSubTitle(view: View) {
            view.findViewById<TextView>(R.id.subTitleView).text = subTitle
        }

        private fun initConfirmText(view: View) {
            view.findViewById<TextView>(R.id.sureBtn).text = confirmText
        }

        private fun initCancelText(view: View) {
            view.findViewById<TextView>(R.id.cancelBtn).text = cancelText
        }

    }

    interface DialogOperationListener{
        fun onClickSure()
        fun onClickCancel()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            dismiss()
        }
        return super.onKeyDown(keyCode, event)
    }
}