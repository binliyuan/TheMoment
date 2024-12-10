package com.volcengine.effectone.auto.templates.launch

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.bytedance.ies.cutsame.util.MediaItemUtils
import com.cutsame.solution.template.model.TemplateItem
import com.ss.android.ugc.cut_log.LogUtil
import com.ss.android.ugc.cut_ui.MediaItem
import com.volcengine.effectone.auto.templates.vm.CutSamePlayerViewModel
import com.volcengine.effectone.auto.templates.vm.CutSameViewModel

/**
 * @author tyx
 * @description:
 * @date :2024/5/9 15:24
 */
class CutSameLauncher(private val mActivity: FragmentActivity) : LifecycleObserver {

	companion object {
		private const val TAG = "CutSameLauncher"
	}

	private val mCutSameViewModel by lazy { CutSameViewModel.get(mActivity) }
	private val mCutSamePlayerViewModel by lazy { CutSamePlayerViewModel.get(mActivity) }
	private var mLaunchPicker: ActivityResultLauncher<LaunchAlbumInput>? = null
	private var mLaunchReplace: ActivityResultLauncher<LaunchAlbumInput>? = null
	private var mLaunchClip: ActivityResultLauncher<MediaItem?>? = null
	private var mLaunchCompose: ActivityResultLauncher<Pair<ArrayList<MediaItem>, TemplateItem>>? = null

	@OnLifecycleEvent(value = Lifecycle.Event.ON_CREATE)
	fun onCreate() {
		val registry = mActivity.activityResultRegistry
		registry.registerPicker()
		registry.registerReplace()
		registry.registerClip()
		registry.registerCompose()
	}

	/**
	 * 从相册选择素材
	 */
	private fun ActivityResultRegistry.registerPicker() {
		mLaunchPicker = register("${TAG}:launchPicker", mActivity, LaunchPickerContract()) { list ->
			LogUtil.d(TAG, "launchPicker:list size : ${list.size}")
			mCutSameViewModel.setFinalMediaItems(list)
			MediaItemUtils.mergePickList(mCutSameViewModel.mMutableMediaItems, list)
			mCutSameViewModel.mUpdateBottomVideoList.value = list
			mCutSameViewModel.mPlayerDataReady.value = list
		}
	}

	/**
	 * 替换素材
	 */
	private fun ActivityResultRegistry.registerReplace() {
		mLaunchReplace = register("${TAG}:launchReplace", mActivity, LaunchReplaceContract()) { list ->
			LogUtil.d(TAG, "launchReplace:list size : ${list.size}")
			list.firstOrNull()?.let { mediaItem ->
				var beforeVolume = 0f
				mCutSamePlayerViewModel.mCurrentEditMediaItem.value?.let {
					beforeVolume = mCutSamePlayerViewModel.getVolume(it.materialId) ?: 0f
				}
				mCutSamePlayerViewModel.mCurrentEditMediaItem.value = null
				replaceMediaItem(mediaItem)
				mCutSamePlayerViewModel.setVolume(mediaItem.materialId, beforeVolume)
			}
		}
	}

	/**
	 * 裁剪
	 */
	private fun ActivityResultRegistry.registerClip() {
		mLaunchClip = register("${TAG}:launchClip", mActivity, LaunchClipContract()) { mediaItem ->
			LogUtil.d(TAG, "launchClip result")
			val beforeVolume: Float = mCutSamePlayerViewModel.getVolume(mediaItem.materialId) ?: 0f
			replaceMediaItem(mediaItem)
			mCutSamePlayerViewModel.setVolume(mediaItem.materialId, beforeVolume)
		}
	}

	private fun ActivityResultRegistry.registerCompose() {
		mLaunchCompose = register("${TAG}:launchCompose", mActivity, CutSameComposeContract()) { mediaItems ->
			mCutSameViewModel.setFinalMediaItems(mediaItems)
			MediaItemUtils.mergePickList(mCutSameViewModel.mMutableMediaItems, mediaItems)
			mCutSameViewModel.mUpdateBottomVideoList.value = mediaItems
			mCutSameViewModel.mPlayerDataReady.value = mediaItems
		}
	}

	private fun replaceMediaItem(mediaItem: MediaItem) {
		val mutableMediaItems = mCutSameViewModel.mMutableMediaItems
		val pickerMediaItems = mCutSameViewModel.mFinalMediaItems
		val index = mutableMediaItems.indexOfFirst { it.materialId == mediaItem.materialId }
		if (index != -1) {
			replace(index, mediaItem, mutableMediaItems)
			replace(index, mediaItem, pickerMediaItems)
			mCutSameViewModel.mUpdateBottomVideoList.value = mutableMediaItems
			mCutSamePlayerViewModel.pause()
			mCutSamePlayerViewModel.updateMedia(mutableMediaItems[index].materialId, mutableMediaItems[index])
			mCutSamePlayerViewModel.seekTo(mutableMediaItems[index].targetStartTime.toInt(), true)
		}
	}

	private fun replace(index: Int, processItem: MediaItem, list: ArrayList<MediaItem>) {
		list[index] = list[index].copy(
			source = processItem.source,
			sourceStartTime = processItem.sourceStartTime,
			crop = processItem.crop,
			type = processItem.type,
			mediaSrcPath = processItem.mediaSrcPath
		)
	}

	fun launchPicker(list: ArrayList<MediaItem>, templateItem: TemplateItem) {
		mLaunchPicker?.launch(LaunchAlbumInput(list, templateItem))
	}

	fun launchReplace(list: ArrayList<MediaItem>, templateItem: TemplateItem) {
		mLaunchReplace?.launch(LaunchAlbumInput(list, templateItem))
	}

	fun launchClip(mediaItem: MediaItem) {
		mLaunchClip?.launch(mediaItem)
	}

	fun launchCompose(list: ArrayList<MediaItem>, templateItem: TemplateItem) {
		mLaunchCompose?.launch(list to templateItem)
	}

	@OnLifecycleEvent(value = Lifecycle.Event.ON_DESTROY)
	fun onDestroy() {
		mLaunchPicker?.unregister()
		mLaunchReplace?.unregister()
		mLaunchClip?.unregister()
		mLaunchCompose?.unregister()
		mLaunchPicker = null
		mLaunchReplace = null
		mLaunchClip = null
		mLaunchCompose = null
	}
}