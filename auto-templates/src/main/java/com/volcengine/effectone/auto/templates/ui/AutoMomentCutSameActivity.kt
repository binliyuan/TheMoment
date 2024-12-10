package com.volcengine.effectone.auto.templates.ui

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.cutsame.solution.template.model.TemplateItem
import com.ss.android.ugc.cut_log.LogUtil
import com.ss.android.ugc.cut_ui.MediaItem
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.bean.ComposeResult
import com.volcengine.effectone.auto.templates.bean.PrepareResult
import com.volcengine.effectone.auto.templates.bean.TemplateByMedias
import com.volcengine.effectone.auto.templates.event.CutSameMomentEventReport
import com.volcengine.effectone.auto.templates.event.TrackNodeEvent
import com.volcengine.effectone.auto.templates.utils.showToast

/**
 * @author tyx
 * @description:
 * @date :2024/6/5 15:30
 */
class AutoMomentCutSameActivity : AutoCutSameActivity() {

	private var mIsShowProgressUi = false

	companion object {
		private const val TAG = "AutoMomentCutSamePage"
		fun createIntent(context: Context, data: ArrayList<TemplateByMedias>): Intent {
			val intent = createIntent(context, data, true)
			return intent.apply {
				setClass(context, AutoMomentCutSameActivity::class.java)
			}
		}
	}

	override fun setEventReport() {
		TrackNodeEvent.setEventReport(CutSameMomentEventReport())
	}

	override fun onPrepareStart(hasSwitch: Boolean) {
		TrackNodeEvent.onPrepareSourceStart(hasSwitch)
		if (hasSwitch) {
			showLoading()
		} else {
			showProgressFragment(mCutSameViewModel.mCurrentTemplateItem)
		}
	}

	override fun onPrepareEnd(prepareResult: PrepareResult) {
		TrackNodeEvent.onPrepareSourceEnd(prepareResult.hasSwitch)
	}

	override fun onComposeStart() {
		TrackNodeEvent.onComposeStart()
	}

	override fun onComposeEnd(composeResult: ComposeResult) {
		super.onComposeEnd(composeResult)
		highProgressFragment()
	}

	private fun showProgressFragment(templateItem: TemplateItem?) {
		if (mIsShowProgressUi) return
		mIsShowProgressUi = true
		LogUtil.d(TAG, "showProgressFragment")
		val fragment = supportFragmentManager.findFragmentByTag(AutoMomentCutSameProgressFragment.TAG)
			?: AutoMomentCutSameProgressFragment.getInstance(templateItem)
		supportFragmentManager.beginTransaction()
			.replace(android.R.id.content, fragment, AutoMomentCutSameProgressFragment.TAG)
			.commitNow()
	}

	private fun highProgressFragment() {
		supportFragmentManager.findFragmentByTag(AutoMomentCutSameProgressFragment.TAG)?.let {
			LogUtil.d(TAG, "highProgressFragment")
			supportFragmentManager.beginTransaction()
				.remove(it)
				.commitNow()
			mIsShowProgressUi = false
		}
	}

	override fun fillMediaItems(mutableMedias: MutableList<MediaItem>, mediaItems: List<MediaItem>): MutableList<MediaItem> {
		val groupMap = mutableMedias.groupBy { mediaItem ->
			if (mediaItem.getGroup() == 0) mediaItem.materialId else "${mediaItem.getGroup()}"
		}
		if (mediaItems.size < groupMap.size) {
			LogUtil.d(TAG, "素材数量:${mediaItems.size}少于槽位所需的数量:${groupMap.size}!")
			return mutableMedias
		}
		groupMap.keys.forEachIndexed { index, s ->
			val mediaItem = mediaItems[index]
			groupMap[s]?.forEach {
				it.source = mediaItem.source
				it.mediaSrcPath = mediaItem.mediaSrcPath
				it.type = mediaItem.type
				it.oriDuration = if (mediaItem.isVideo()) mediaItem.duration else mediaItem.oriDuration
				it.sourceStartTime = mediaItem.sourceStartTime
			}
		}
		return mutableMedias
	}

	override fun onComposeSuccess(mediaItems: ArrayList<MediaItem>) {
		super.onComposeSuccess(mediaItems)
		ContextCompat.getDrawable(this, R.drawable.icon_mark)?.let {
			("视频已合成" to it).showToast(this)
		}
	}
}