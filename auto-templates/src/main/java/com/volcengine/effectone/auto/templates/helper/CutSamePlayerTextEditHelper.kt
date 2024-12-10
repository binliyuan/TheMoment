package com.volcengine.effectone.auto.templates.helper

import android.annotation.SuppressLint
import android.graphics.Rect
import android.graphics.RectF
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ss.android.ugc.aweme.views.setDebounceOnClickListener
import com.ss.android.ugc.tools.view.activity.AVActivityOnKeyDownListener
import com.ss.ugc.android.editor.track.utils.runOnUiThread
import com.ss.ugc.android.editor.track.utils.visible
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.adapter.CutSamePlayerTextEditAdapter
import com.volcengine.effectone.auto.templates.bean.PlayerTextEditItem
import com.volcengine.effectone.auto.templates.helper.textedit.IPlayerTextEditExtraListener
import com.volcengine.effectone.auto.templates.helper.textedit.IPlayerTextEditListener
import com.volcengine.effectone.auto.templates.helper.textedit.PlayerTextEditExtraView
import com.volcengine.effectone.auto.templates.vm.CutSamePlayerViewModel
import com.volcengine.effectone.auto.templates.widget.CustomButtonLayout
import com.volcengine.effectone.auto.templates.widget.TipsDialog
import com.volcengine.effectone.utils.SizeUtil

/**
 * @author tyx
 * @description:
 * @date :2024/5/8 16:15
 */
class CutSamePlayerTextEditHelper(
	override val activity: FragmentActivity,
	override val owner: LifecycleOwner
) : IUIHelper, IPlayerTextEditExtraListener, AVActivityOnKeyDownListener {

	companion object {
		private const val TAG = "CutSamePlayerTextEditHelper"
	}

	private val mCutSamePlayerViewModel by lazy { CutSamePlayerViewModel.get(activity) }
	private lateinit var mBottomPanelRoot: ConstraintLayout
	private lateinit var mTextEditRoot: ConstraintLayout
	private lateinit var mIvBack: ImageView
	private lateinit var mRv: RecyclerView
	private lateinit var mLyConfirm: CustomButtonLayout
	private lateinit var mPlayerTextEditExtraView: PlayerTextEditExtraView
	private lateinit var mAdapter: CutSamePlayerTextEditAdapter
	private var mTextEditListener: IPlayerTextEditListener? = null
	private val mTextRectF by lazy { RectF() }

	private val mBackPressedCallback by lazy {
		object : OnBackPressedCallback(false) {
			override fun handleOnBackPressed() {
				mIvBack.performClick()
			}
		}
	}

	init {
		activity.onBackPressedDispatcher.addCallback(owner, mBackPressedCallback)
	}

	@SuppressLint("InflateParams")
	override fun initView(rootView: ViewGroup) {
		val rootFrame = rootView.findViewById<ViewGroup>(R.id.text_edit_frame)
		val inf = LayoutInflater.from(rootView.context)
		val view = inf.inflate(R.layout.layout_cut_same_edit_text, rootFrame, false)
		rootFrame.addView(view)
		findView(view)
		initAdapter()
		startObserver()
		setVisible(false)
	}

	@SuppressLint("ClickableViewAccessibility")
	private fun findView(view: View) {
		mTextEditRoot = view.findViewById(R.id.layout_text_edit_root)
		mBottomPanelRoot = view.findViewById(R.id.ly_text_list_panel_container)
		mIvBack = view.findViewById(R.id.iv_back)
		mRv = view.findViewById(R.id.rv_text)
		mLyConfirm = view.findViewById(R.id.ly_confirm)
		mPlayerTextEditExtraView = view.findViewById(R.id.view_player_text_edit_extra)
		mPlayerTextEditExtraView.setOnPlayerTextEditExtraListener(this)
		mIvBack.setDebounceOnClickListener {
			val isChange = mAdapter.getData().any { it.isChange() }
			if (isChange) {
				showCancelDialog()
			} else {
				cancel()
			}
		}
		mLyConfirm.setDebounceOnClickListener {
			mCutSamePlayerViewModel.mShowEditTextPanel.value = false
		}
		mTextEditRoot.setOnTouchListener { _, _ ->
			mPlayerTextEditExtraView.showOrHideEditView(false)
			false
		}
	}

	private fun initAdapter() {
		mAdapter = CutSamePlayerTextEditAdapter()
		mRv.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
		mRv.adapter = mAdapter
		mRv.addItemDecoration(object : RecyclerView.ItemDecoration() {
			override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
				super.getItemOffsets(outRect, view, parent, state)
				outRect.right = SizeUtil.dp2px(16f)
			}
		})
		mAdapter.clickBlock = { view, _, data ->
			when (view.id) {
				R.id.text_edit_bg -> {
					mPlayerTextEditExtraView.showOrHideEditView(true)
					mPlayerTextEditExtraView.updateTextEdit(data.editText)
				}

				R.id.iv_cover -> selectTextItem(data)
			}
		}
	}

	private fun selectTextItem(data: PlayerTextEditItem) {
		mCutSamePlayerViewModel.setTextAnimSwitch(data.textItem.materialId, false)
		mCutSamePlayerViewModel.seekTo(maxOf(0, data.startTime.toInt()), false) {
			runOnUiThread {
				mCutSamePlayerViewModel.pause()
				mPlayerTextEditExtraView.updateTextEdit(data.editText)
				updateTextBoxViewLocation(data)
			}
		}
	}

	private fun updateTextBoxViewLocation(data: PlayerTextEditItem) {
		mCutSamePlayerViewModel.getTextSegment(data.textItem.materialId, mTextRectF)
		runOnUiThread {
			val canvasSize = mTextEditListener?.getCanvasSize()
			val surfaceViewSize = mTextEditListener?.getSurfaceSize()
			mPlayerTextEditExtraView.updateTextItemRect(mTextRectF, canvasSize, surfaceViewSize, data.textItem)
		}
	}

	private fun startObserver() {
		mCutSamePlayerViewModel.mShowEditTextPanel.observe(activity) { visible ->
			if (visible) {
				initData()
				mAdapter.updateSelectIndex(0)
				mRv.scrollToPosition(0)
				selectTextItem(mAdapter.getData().first())
			} else {
				mAdapter.getData().forEach {
					mCutSamePlayerViewModel.setTextAnimSwitch(it.textItem.materialId, true)
				}
			}
			setVisible(visible)
		}
		mCutSamePlayerViewModel.mFrameBitmap.observe(activity) { result ->
			result ?: return@observe
			mAdapter.addThumbBitmap(result)
		}
	}

	private fun initData() {
		val textItems = mCutSamePlayerViewModel.getTextItems(true)
		if (textItems.isNullOrEmpty()) return
		val items = textItems.map { PlayerTextEditItem(it).apply { startTime = it.targetStartTime + 100 } }
		mAdapter.updateItems(items)
		mTextEditListener?.run {
			val canvasSize = getCanvasSize()
			val array = calculateSize(canvasSize)
			val list = items.map { it.startTime.toInt() }.toIntArray()
			mCutSamePlayerViewModel.getItemFrameBitmap(list, array.first(), array.last())
		}
	}

	private fun calculateSize(canvasSize: IntArray): IntArray {
		val canvasWidth = canvasSize.first()
		val canvasHeight = canvasSize.last()
		var itemWidth = SizeUtil.dp2px(80f)
		var itemHeight = SizeUtil.dp2px(80f)
		if (canvasWidth > canvasHeight) {
			itemHeight = (itemWidth / (canvasWidth / 1f / canvasHeight)).toInt()
		} else {
			itemWidth = (itemHeight / (canvasHeight / 1f / canvasWidth)).toInt()
		}
		return intArrayOf(itemWidth, itemHeight)
	}

	fun setPlayerTextEditListener(listener: IPlayerTextEditListener?) {
		this.mTextEditListener = listener
	}

	override fun onEditTextChange(text: String) {
		mAdapter.getCurrentSelectData()?.let {
			it.editText = text
			mAdapter.refreshCurrentItemText(text)
			mCutSamePlayerViewModel.updateText(it.textItem.materialId, text)
			updateTextBoxViewLocation(it)
		}
	}

	override fun onEditTextComplete(text: String) {}

	private fun showCancelDialog() {
		TipsDialog.Builder()
			.setTitleText("放弃编辑")
			.setMessage("确定放弃所有的文字?")
			.setCancelText("取消")
			.setSureText("放弃")
			.setDialogOperationListener(object : TipsDialog.DialogOperationListener {
				override fun onClickSure() {
					cancel()
				}

				override fun onClickCancel() {}
			}).create(activity).show()
	}

	private fun cancel() {
		mAdapter.getData().forEach {
			it.restoreText()
			mCutSamePlayerViewModel.updateText(it.textItem.materialId, it.originText)
		}
		mCutSamePlayerViewModel.mShowEditTextPanel.value = false
	}

	private fun setVisible(visible: Boolean) {
		mBackPressedCallback.isEnabled = visible
		if (mTextEditRoot.visible == visible) return
		val height = maxOf(mBottomPanelRoot.measuredHeight.toFloat(), SizeUtil.dp2px(142f).toFloat())
		val translationY = if (visible) 0f else height
		mBottomPanelRoot.animate()
			.withStartAction {
				mPlayerTextEditExtraView.showOrHideTextBox(false)
				mTextEditRoot.visible = true
				mBottomPanelRoot.visible = true
			}
			.withEndAction {
				mBottomPanelRoot.visible = visible
				mTextEditRoot.visible = visible
			}
			.translationY(translationY).setDuration(200).start()
	}

	override fun onKeyDown(p0: Int, p1: KeyEvent?): Boolean {
		if (mTextEditRoot.visible) {
			mIvBack.performClick()
		}
		return false
	}

	@OnLifecycleEvent(value = Lifecycle.Event.ON_DESTROY)
	fun onDestroy() {
		mBackPressedCallback.remove()
		mAdapter.releaseBitmap()
		mPlayerTextEditExtraView.release()
		mTextEditListener = null
	}
}