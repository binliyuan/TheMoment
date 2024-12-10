package com.volcengine.effectone.auto.templates.helper.textedit

import android.text.InputFilter
import android.text.Spanned
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author tyx
 * @description:
 * @date :2024/5/24 16:11
 */
class EmojiFilter : InputFilter {
	override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
		if (isEmoji(source.toString())) return ""
		return null
	}


	private fun isEmoji(input: String): Boolean {
		val p: Pattern = Pattern.compile(
			"[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\ud83e\udc00-\ud83e\udfff]" +
					"|[\u2100-\u32ff]|[\u0030-\u007f][\u20d0-\u20ff]|[\u0080-\u00ff]"
		)
		val m: Matcher = p.matcher(input)
		return m.find()
	}
}