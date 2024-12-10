package com.volcengine.effectone.auto.templates.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.bytedance.ies.cutsame.util.MediaItemUtils
import com.cutsame.solution.template.model.TemplateItem
import com.ss.android.ugc.cut_log.LogUtil
import com.ss.android.ugc.cut_ui.MediaItem
import com.volcengine.ck.logkit.LogKit
import com.volcengine.effectone.auto.templates.bean.TemplateByMedias
import com.volcengine.effectone.auto.templates.launch.CutSameLauncher
import com.volcengine.effectone.auto.templates.vm.CutSamePlayerViewModel

/**
 * @author tyx
 * @description:
 * @date :2024/4/25 18:30
 */
open class AutoCutSameActivity : BaseAutoCutSameActivity() {

	private var mHasSwitch = false

	companion object {
		private const val TAG = "AutoCutSameActivity"
		private const val HAS_SWITCH = "hasSwitch"
		fun createIntent(context: Context, data: ArrayList<TemplateByMedias>, hasSwitch: Boolean = false): Intent {
			return Intent().apply {
				setClass(context, AutoCutSameActivity::class.java)
				putParcelableArrayListExtra(TEMPLATES_BY_MEDIAS, data)
				putExtra(HAS_SWITCH, hasSwitch)
			}
		}
	}

	private var isInitializedUI = false
	protected val mCutSameLauncher by lazy { CutSameLauncher(this) }
	private val mCutSamePlayerViewModel by lazy { CutSamePlayerViewModel.get(this) }
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		mHasSwitch = intent.getBooleanExtra(HAS_SWITCH, mHasSwitch)
		lifecycle.addObserver(mCutSameLauncher)
	}

	override fun startObserver() {
		super.startObserver()
		mCutSameViewModel.mPlayerDataReady.observe(this) { result ->
			if (result.isEmpty()) {
				LogUtil.d(TAG, "player data is empty")
				finish()
				return@observe
			}
			renderView()
		}
		mCutSamePlayerViewModel.mLaunchPicker.observe(this) { result ->
			result ?: return@observe
			mCutSameViewModel.mCurrentTemplateItem?.let { templateItem ->
				launchAlbum(ArrayList(result.first), templateItem, result.second)
			}
		}
		mCutSamePlayerViewModel.mLaunchClip.observe(this) { result ->
			result ?: return@observe
			mCutSameLauncher.launchClip(result)
		}
	}

	override fun onPrepareSuccess(mutableMediaList: MutableList<MediaItem>, templateItem: TemplateItem, switch: Boolean) {
		super.onPrepareSuccess(mutableMediaList, templateItem, switch)
		val mediasByTemplate = mCutSameViewModel.getMediasByTemplate(templateItem)
		if (mediasByTemplate != null && mediasByTemplate.mediaList.isNotEmpty()) {
			val composeList = fillMediaItems(mutableMediaList, mediasByTemplate.mediaList)
			composeSource(templateItem, ArrayList(composeList))
		} else {
			launchAlbum(ArrayList(mutableMediaList), templateItem)
		}
	}

	private fun launchAlbum(mutableMediaList: ArrayList<MediaItem>, templateItem: TemplateItem, replace: Boolean = false) {
		LogKit.d(TAG, "launch picker,mutable size = ${mutableMediaList.size}")
		if (replace) {
			mCutSameLauncher.launchReplace(mutableMediaList, templateItem)
		} else {
			mCutSameLauncher.launchPicker(mutableMediaList, templateItem)
		}
	}

	open fun composeSource(templateItem: TemplateItem, composeList: ArrayList<MediaItem>) {
		mCutSameViewModel.composeSource(composeList)
	}

	override fun onComposeSuccess(mediaItems: ArrayList<MediaItem>) {
		super.onComposeSuccess(mediaItems)
		mCutSameViewModel.setFinalMediaItems(mediaItems)
		MediaItemUtils.mergePickList(mCutSameViewModel.mMutableMediaItems, mediaItems)
		mCutSameViewModel.mUpdateBottomVideoList.value = mediaItems
		mCutSameViewModel.mPlayerDataReady.value = mediaItems
	}

	private fun renderView() {
		if (isInitializedUI) return
		LogUtil.d(TAG, "renderView")
		isInitializedUI = true
		val fragment = supportFragmentManager.findFragmentByTag(AutoCutSameFragment.TAG) ?: AutoCutSameFragment.getInstance(mHasSwitch)
		supportFragmentManager.beginTransaction()
			.replace(android.R.id.content, fragment, AutoCutSameFragment.TAG)
			.commitNow()
	}

	/**
	 * 将素材填充进槽位
	 */
	open fun fillMediaItems(mutableMedias: MutableList<MediaItem>, mediaItems: List<MediaItem>): MutableList<MediaItem> {
		if (mediaItems.size < mutableMedias.size) {
			LogUtil.d(TAG, "素材少于槽位数量!")
			return mutableMedias
		}
		return mutableMedias.mapIndexed { index, mediaItem ->
			val sourceItem = mediaItems.getOrNull(index) ?: mediaItem
			mediaItem.copy(
				source = sourceItem.source,
				mediaSrcPath = sourceItem.mediaSrcPath,
				type = sourceItem.type,
				oriDuration = if (sourceItem.isVideo()) sourceItem.duration else mediaItem.oriDuration
			)
		}.toMutableList()
	}
}