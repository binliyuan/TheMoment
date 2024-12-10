package com.volcengine.effectone.auto.templates.ui

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cutsame.solution.template.model.TemplateItem
import com.gyf.immersionbar.ktx.immersionBar
import com.ss.android.ugc.cut_ui.MediaItem
import com.volcengine.ck.logkit.LogKit
import com.volcengine.effectone.auto.templates.bean.BaseResultData
import com.volcengine.effectone.auto.templates.bean.ComposeResult
import com.volcengine.effectone.auto.templates.bean.PrepareResult
import com.volcengine.effectone.auto.templates.bean.TemplateByMedias
import com.volcengine.effectone.auto.templates.cutsame.CutSameContext
import com.volcengine.effectone.auto.templates.event.CutSameEventReport
import com.volcengine.effectone.auto.templates.event.TrackNodeEvent
import com.volcengine.effectone.auto.templates.vm.CutSameViewModel
import com.volcengine.effectone.auto.templates.widget.LoadingDialog
import kotlin.math.roundToInt

/**
 * @author tyx
 * @description:
 * @date :2024/4/25 16:05
 */
abstract class BaseAutoCutSameActivity : AppCompatActivity() {
	companion object {
		private const val TAG = "BaseAutoCutSameActivity"
		private const val SELECT_INDEX = "select_index"
		const val TEMPLATES_BY_MEDIAS = "templates_by_medias"
		const val TEMPLATE_KEY = "auto_template_key"
	}

	private val mLoadingDialog by lazy { LoadingDialog(this) }
	protected val mCutSameViewModel by lazy { CutSameViewModel.get(this) }
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		immersionBar {
			transparentStatusBar()
			navigationBarColor(com.gyf.immersionbar.R.color.abc_decor_view_status_guard)
			statusBarDarkFont(false)
		}
		handlerIntentData()
		setEventReport()
		startObserver()
	}

	open fun setEventReport() {
		TrackNodeEvent.setEventReport(CutSameEventReport())
	}

	private fun handlerIntentData() {
		val templateItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			intent.getParcelableExtra(TEMPLATE_KEY, TemplateItem::class.java)
		} else {
			intent.getParcelableExtra(TEMPLATE_KEY) as? TemplateItem
		}
		val templatesByMedias = if (templateItem != null) {
            listOf(TemplateByMedias(templateItem, emptyList()))
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayListExtra(TEMPLATES_BY_MEDIAS, TemplateByMedias::class.java)
            } else intent.getParcelableArrayListExtra(TEMPLATES_BY_MEDIAS)
        }
		if (!templatesByMedias.isNullOrEmpty()) {
			val firstSelectIndex = minOf(intent.getIntExtra(SELECT_INDEX, 0), (templatesByMedias.size.minus(1)))
			mCutSameViewModel.inject(templatesByMedias, firstSelectIndex)
			prepareSource(templatesByMedias[firstSelectIndex].templateItem)
		} else {
			LogKit.d(TAG, "template is empty")
			finish()
		}
	}

	open fun prepareSource(templateItem: TemplateItem) {
		mCutSameViewModel.prepareSource(templateItem)
	}

	open fun startObserver() {
		mCutSameViewModel.mPrepareLiveData.observe(this) {
			it ?: return@observe
			handlePrepareResult(it)
		}
		mCutSameViewModel.mComposeLiveData.observe(this) {
			it ?: return@observe
			handleComposeResult(it)
		}
	}

	open fun handlePrepareResult(prepareResult: PrepareResult) {
		val isShowLoading = true
		when (prepareResult.state) {
			BaseResultData.START -> onPrepareStart(prepareResult.hasSwitch)
			BaseResultData.END -> {
				onPrepareEnd(prepareResult)
				prepareResult.resultData?.onSuccess { result ->
					onPrepareSuccess(result.mediaItemList.toMutableList(), result.templateItem, prepareResult.hasSwitch)
				}?.onFailure {
					LogKit.d(TAG, "prepare fail msg = ${it.message}")
					if (!prepareResult.hasSwitch) finish() else
						Toast.makeText(this, "切换模板失败！", Toast.LENGTH_SHORT).show()
				} ?: finish()
			}
			BaseResultData.PROGRESS -> {
				if (isShowLoading) mLoadingDialog.setMessage(String.format("视频处理中%d%%...", prepareResult.progress.roundToInt()))
			}
		}
	}

	open fun onPrepareStart(hasSwitch: Boolean) {
		TrackNodeEvent.onPrepareSourceStart(hasSwitch)
		showLoading()
	}

	open fun onPrepareEnd(prepareResult: PrepareResult) {
		TrackNodeEvent.onPrepareSourceEnd(prepareResult.hasSwitch)
		highLoading()
	}

	open fun onPrepareSuccess(mutableMediaList: MutableList<MediaItem>, templateItem: TemplateItem, switch: Boolean) {
		LogKit.d(TAG, "prepare success:mutable size = ${mutableMediaList.size},temp = ${templateItem.id},switch:$switch")
	}

	open fun handleComposeResult(composeResult: ComposeResult) {
		when (composeResult.state) {
			BaseResultData.START -> onComposeStart()
			BaseResultData.PROGRESS -> mLoadingDialog.setMessage(String.format("视频处理中%d%%...", composeResult.progress.roundToInt()))
			BaseResultData.END -> {
				onComposeEnd(composeResult)
				composeResult.resultData?.onSuccess {
					onComposeSuccess(it)
				}?.onFailure {
					Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
				} ?: LogKit.d(TAG, "handleComposeResult result is null")
			}
		}
	}

	open fun onComposeStart() {
		TrackNodeEvent.onComposeStart()
		showLoading()
	}

	open fun onComposeEnd(composeResult: ComposeResult) {
		TrackNodeEvent.onComposeEnd()
		highLoading()
	}

	open fun onComposeSuccess(mediaItems: ArrayList<MediaItem>) {
		LogKit.d(TAG, "compose success mediaItems size = ${mediaItems.size}")
	}

	open fun showLoading() {
		if (mLoadingDialog.isShowing) return
		mLoadingDialog.setMessage(String.format("视频处理中%d%%...", 0))
		mLoadingDialog.show()
	}

	open fun highLoading() {
		if (mLoadingDialog.isShowing) mLoadingDialog.dismiss()
	}

	override fun onBackPressed() {
		onBackPressedDispatcher.onBackPressed()
	}

	override fun onDestroy() {
		mCutSameViewModel.release()
		CutSameContext.release()
		super.onDestroy()
	}
}