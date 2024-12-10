package com.volcengine.effectone.auto.templates.helper.textedit

/**
 * @author tyx
 * @description:
 * @date :2024/5/24 15:58
 */
interface IPlayerTextEditExtraListener {
	fun onEditTextChange(text: String) {}
	fun onEditTextComplete(text: String) {}
}