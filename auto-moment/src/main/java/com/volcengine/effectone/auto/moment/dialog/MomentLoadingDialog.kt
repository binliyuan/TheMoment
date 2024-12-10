package com.volcengine.effectone.auto.moment.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.volcengine.effectone.auto.moment.R

/**
 * @author tyx
 * @description:
 * @date :2024/4/25 17:50
 */
class MomentLoadingDialog(context: Context) : Dialog(context, R.style.moment_dialog_fullscreen) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.moment_loading_dialog)
        setCanceledOnTouchOutside(false)
    }
}