package com.volcengine.effectone.auto.templates.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.utils.SizeUtil

/**
 * @author tyx
 * @description:
 * @date :2024/5/7 15:10
 */
class CustomButtonLayout : LinearLayout {

	private var mIvIcon: ImageView
	private var mTvName: TextView
	private var mViewBg: View
	private var mBg = ContextCompat.getDrawable(context, R.drawable.te_button_bg)
	private var mNormalIcon = ContextCompat.getDrawable(context, R.drawable.icon_music)
	private var mSelectIcon = ContextCompat.getDrawable(context, R.drawable.icon_select_music)
	private var mNormalTvColor = ContextCompat.getColor(context, R.color.color_EEE)
	private var mSelectTvColor = ContextCompat.getColor(context, R.color.color_FF53BB)
	private var mName = ""
	private var mTextMarginTop = SizeUtil.dp2px(6f)
	private var mCheckState: Boolean = false

	constructor(context: Context) : this(context, null)
	constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
		val inf = LayoutInflater.from(context)
		inf.inflate(R.layout.layout_check_layout, this, true)
		mIvIcon = findViewById(R.id.iv_icon)
		mTvName = findViewById(R.id.tv_name)
		mViewBg = findViewById(R.id.view_bg)
		val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomButtonLayout, defStyleAttr, 0)
		try {
			val bgId = typedArray.getResourceId(R.styleable.CustomButtonLayout_te_bg_res, -1)
			if (bgId != -1) mBg = ContextCompat.getDrawable(context, bgId) ?: mBg
			val normalId = typedArray.getResourceId(R.styleable.CustomButtonLayout_te_normal_icon_res, -1)
			if (normalId != -1) mNormalIcon = ContextCompat.getDrawable(context, normalId) ?: mNormalIcon
			val selectId = typedArray.getResourceId(R.styleable.CustomButtonLayout_te_select_icon_res, -1)
			if (selectId != -1) mSelectIcon = ContextCompat.getDrawable(context, selectId) ?: mSelectIcon
			mNormalTvColor = typedArray.getColor(R.styleable.CustomButtonLayout_te_normal_tv_color, mNormalTvColor)
			mSelectTvColor = typedArray.getColor(R.styleable.CustomButtonLayout_te_select_tv_color, mSelectTvColor)
			mName = typedArray.getString(R.styleable.CustomButtonLayout_te_text_name) ?: mName
			mTextMarginTop = typedArray.getDimensionPixelSize(R.styleable.CustomButtonLayout_te_text_margin_top, mTextMarginTop)
			mCheckState = typedArray.getBoolean(R.styleable.CustomButtonLayout_te_checked, mCheckState)
		} finally {
			typedArray.recycle()
		}
		val params = mTvName.layoutParams as MarginLayoutParams
		mTvName.layoutParams = params.apply {
			topMargin = mTextMarginTop
		}
		mViewBg.background = mBg
		setCheckState(mCheckState)
	}

	fun setCheckState(check: Boolean) {
		mCheckState = check
		val drawable = if (check) mSelectIcon else mNormalIcon
		val color = if (check) mSelectTvColor else mNormalTvColor
		mTvName.setTextColor(color)
		mTvName.text = mName
		mIvIcon.setImageDrawable(drawable)
	}

	fun getCheckState() = mCheckState
}