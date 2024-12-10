package com.volcengine.effectone.auto.templates.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout

/**
 * @author tyx
 * @description:
 * @date :2024/5/7 20:33
 */
class ClickConstraintLayout @JvmOverloads constructor(
	context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

	@SuppressLint("ClickableViewAccessibility")
	override fun onTouchEvent(event: MotionEvent?): Boolean {
		return true
	}
}