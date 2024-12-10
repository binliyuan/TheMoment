package com.volcengine.effectone.auto.templates.bean

import com.ss.android.ugc.cut_ui.TextItem

/**
 * @author tyx
 * @description:
 * @date :2024/5/24 10:00
 */
data class PlayerTextEditItem(var textItem: TextItem) {
	var startTime = 0L
	val originText = textItem.text
	var editText = textItem.text

	fun restoreText() {
		editText = originText
	}

	fun isChange(): Boolean {
		return editText != originText
	}
}