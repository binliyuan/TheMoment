package com.volcengine.effectone.auto.moment.hl.widget

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.volcengine.ck.logkit.LogKit
import com.volcengine.effectone.auto.moment.R
import kotlin.math.max


class AutoDynamicSizeTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatTextView(context, attrs, defStyleAttr) {
    private var maxLengthPerLine = MAX_LENGTH_PER_LINE
    private var measureSingleContent = TEST_CONTENT_ZH
    private val textBounds = Rect()
    private var defaultSingleContentWith = 0

    init {
        setupAttrs(context, attrs, defStyleAttr)
        setupSingleContent()
    }

    private fun setupSingleContent() {
        paint.getTextBounds(measureSingleContent, 0, measureSingleContent.length, textBounds).also {
            defaultSingleContentWith = textBounds.left + textBounds.right
        }
    }

    private fun setupAttrs(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) {
        val array = if (context.theme != null) {
            context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.AutoDynamicSizeTextView,
                defStyleAttr,
                0
            )
        } else {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.AutoDynamicSizeTextView,
                defStyleAttr,
                0
            )
        }

        try {
            array.getInt(R.styleable.AutoDynamicSizeTextView_maxLengthPerLine, MAX_LENGTH_PER_LINE)
                .also { maxLengthPerLine = it }
            array.getString(R.styleable.AutoDynamicSizeTextView_measureSingleContent)
                ?: TEST_CONTENT_ZH.also { measureSingleContent = it }
        } catch (e: Exception) {
            LogKit.e(TAG, "init parse style error", e)
        } finally {
            array.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val text = text.toString()
        val paint = paint
        val textWidth = if (text.length / maxLengthPerLine == 0) {//不足一行
            maxLengthPerLine * defaultSingleContentWith + paddingStart + paddingEnd
        } else {
            var maxLineWith = 0
            //超过一行，maxLengthPerLine截断，测量每一行，求最大的一行
            text.truncateToLines().forEach {
                paint.getTextBounds(it, 0, it.length, textBounds)
                val textWidth = textBounds.left + textBounds.right
                maxLineWith = max(maxLineWith, textWidth)
            }
            maxLineWith + paddingStart + paddingEnd
        }
        setMeasuredDimension(resolveSize(textWidth, widthMeasureSpec), heightMeasureSpec)
    }

    private fun String.truncateToLines(maxLength: Int = 24): List<String> {
        return this.windowed(maxLength, maxLength, true)
            .asSequence()
            .map { it.trim() }
            .filterNot { it.isEmpty() }
            .toList()
    }

    companion object {
        private const val TAG = "AutoDynamicSizeTextView"
        private const val MAX_LENGTH_PER_LINE = 24
        private const val TEST_CONTENT_ZH = "测"
        private const val TEST_CONTENT_EN = "a"
    }
}