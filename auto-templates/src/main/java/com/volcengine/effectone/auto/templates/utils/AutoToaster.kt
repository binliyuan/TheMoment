package com.volcengine.effectone.auto.templates.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.utils.SizeUtil

/**
 * @author tyx
 * @description:
 * @date :2024/6/5 16:01
 */
fun Int.stringRes(context: Context): String {
	return context.getString(this)
}

fun String.showToast(context: Context) {
	AutoToaster.showToast(context, this)
}

fun Pair<String, Drawable>.showToast(context: Context) {
	AutoToaster.showToast(context, this.first, this.second)
}

object AutoToaster {
	fun createToast(context: Context, msg: String, icon: Drawable? = null): Toast {
		val inf = LayoutInflater.from(context)
		val view = inf.inflate(R.layout.layout_toast, null, false)
		val tvMsg = view.findViewById<TextView>(R.id.tv_msg)
		icon?.let {
			tvMsg.setCompoundDrawablesWithIntrinsicBounds(it, null, null, null)
			tvMsg.compoundDrawablePadding = SizeUtil.dp2px(12f)
		}
		tvMsg.text = msg
		val toast = Toast(context)
		toast.view = view
		return toast
	}

	fun showToast(
		context: Context,
		msg: String,
		icon: Drawable? = null,
		gravity: Int = Gravity.CENTER,
		offsetX: Int = 0,
		offsetY: Int = 0,
		duration: Int = Toast.LENGTH_SHORT
	) {
		val toast = createToast(context, msg, icon)
		toast.setGravity(gravity, offsetX, offsetY)
		toast.duration = duration
		toast.show()
	}
}