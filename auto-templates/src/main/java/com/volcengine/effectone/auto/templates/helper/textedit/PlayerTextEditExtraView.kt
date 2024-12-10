package com.volcengine.effectone.auto.templates.helper.textedit

import android.app.Activity
import android.content.Context
import android.graphics.RectF
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.ss.android.ugc.aweme.views.setDebounceOnClickListener
import com.ss.android.ugc.cut_log.LogUtil
import com.ss.android.ugc.cut_ui.TextItem
import com.ss.ugc.android.editor.track.utils.visible
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.utils.KeyBoardUtils

/**
 * @author tyx
 * @description:文字槽位编辑输入 + 文字选中框
 * @date :2024/5/24 15:43
 */
class PlayerTextEditExtraView : FrameLayout {

	companion object {
		private const val TAG = "PlayerTextEditExtraView"
	}

	private lateinit var mEdInput: EditText
	private lateinit var mLyEd: ConstraintLayout
	private lateinit var mBtnComplete: Button
	private lateinit var mViewBox: View
	private var mListener: IPlayerTextEditExtraListener? = null

	constructor(context: Context) : this(context, null)
	constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
		LayoutInflater.from(context).inflate(R.layout.layout_cutsame_text_edit_extra_view, this, true)
		initView()
		registerKeyboardChangeListener()
	}

	private fun initView() {
		mEdInput = findViewById(R.id.ed_input)
		mLyEd = findViewById(R.id.ly_edit)
		mBtnComplete = findViewById(R.id.btn_complete)
		mViewBox = findViewById(R.id.text_box_view)
		mBtnComplete.setDebounceOnClickListener {
			mListener?.onEditTextComplete(mEdInput.text.toString())
			showOrHideEditView(false)
		}
		mEdInput.filters = arrayOf(EmojiFilter())
		mEdInput.addTextChangedListener(object : TextWatcher {
			override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
			override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
			override fun afterTextChanged(s: Editable?) {
				mListener?.onEditTextChange(s.toString())
			}
		})
	}

	fun setOnPlayerTextEditExtraListener(listener: IPlayerTextEditExtraListener?) {
		mListener = listener
	}

	fun showOrHideEditView(isShow: Boolean) {
		mLyEd.visible = isShow
		if (isShow) {
			mEdInput.requestFocus()
			showSoftInput()
		} else {
			mEdInput.clearFocus()
			hideSoftInput()
		}
	}

	fun updateTextEdit(text: String) {
		mEdInput.setText(text)
		mEdInput.setSelection(mEdInput.text.length)
	}

	fun showOrHideTextBox(isShow: Boolean) {
		mViewBox.visible = isShow
	}

	fun updateTextItemRect(mTextRectF: RectF, canvasSize: IntArray?, surfaceViewSize: IntArray?, textItem: TextItem) {
		showOrHideTextBox(true)
		val angle = -(textItem.rotation % 360)
		mViewBox.rotation = angle.toFloat()
		val params = mViewBox.layoutParams as MarginLayoutParams
		val canvasWidth = canvasSize?.first() ?: 0
		val canvasHeight = canvasSize?.last() ?: 0
		val surfaceWidth = surfaceViewSize?.first() ?: 0
		val surfaceHeight = surfaceViewSize?.last() ?: 0

		val leftMargin = (surfaceWidth - canvasWidth) / 2f
		val left = canvasWidth * mTextRectF.left
		params.leftMargin = maxOf((leftMargin + left).toInt(), leftMargin.toInt())

		val topMargin = (surfaceHeight - canvasHeight) / 2f
		val top = canvasHeight * mTextRectF.top
		params.topMargin = maxOf((topMargin + top).toInt(), topMargin.toInt())

		params.width = minOf((canvasWidth * mTextRectF.width()).toInt(), canvasWidth)
		params.height = minOf((canvasHeight * mTextRectF.height()).toInt(), canvasHeight)

		mViewBox.layoutParams = params
	}

	private fun onKeyboardHeight(height: Int) {
		LogUtil.d(TAG, "height:$height")
		val params = mLyEd.layoutParams as MarginLayoutParams
		mLyEd.layoutParams = params.apply { bottomMargin = height }
		val visible = height > 0
		if (mLyEd.visible == visible) return
		mLyEd.visible = visible
	}

	private fun showSoftInput() {
		KeyBoardUtils.showSoftInput(mEdInput)
	}

	private fun hideSoftInput() {
		KeyBoardUtils.hideSoftInput(mEdInput)
	}

	private fun registerKeyboardChangeListener() {
		(context as? Activity)?.window?.let {
			KeyBoardUtils.registerSoftInputChangedListener(it, object : KeyBoardUtils.OnSoftInputChangedListener {
				override fun onSoftInputChanged(var1: Int) {
					onKeyboardHeight(var1)
				}
			})
		}
	}

	private fun unRegisterKeyboardChangeListener() {
		(context as? Activity)?.window?.let {
			KeyBoardUtils.unregisterSoftInputChangedListener(it)
		}
	}

	fun release() {
		mListener = null
		unRegisterKeyboardChangeListener()
	}
}