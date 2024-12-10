package com.volcengine.effectone.auto.templates.launch

import android.app.Activity
import android.os.Build
import com.bytedance.creativex.mediaimport.repository.api.IMaterialItem
import com.bytedance.creativex.mediaimport.repository.api.isVideo
import com.cutsame.solution.source.ComposeSourceListener
import com.cutsame.solution.source.CutSameSource
import com.cutsame.solution.template.model.TemplateItem
import com.ss.android.ugc.cut_log.LogUtil
import com.ss.android.ugc.cut_ui.MediaItem
import com.volcengine.ck.album.api.IAlbumFinish
import com.volcengine.ck.album.base.AlbumConfig
import com.volcengine.effectone.auto.common.cutsame.CutSameContract
import com.volcengine.effectone.auto.templates.cutsame.CutSameContext
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * @author tyx
 * @description:
 * @date :2024/5/9 16:46
 */
class CutSameAlbumFinishImp : IAlbumFinish {

	companion object {
		private const val TAG = "CutSameAlbumFinishImp"
	}

	/**
	 * TODO 暂时未跟相册联调，这里仅为测试，未跳转ComposeActivity逻辑
	 */
	override suspend fun finishAction(activity: Activity, mediaList: List<IMaterialItem>, albumConfig: AlbumConfig) {
		val data = activity.intent
		val mutableMediaItemList = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			data?.getParcelableArrayListExtra(CutSameContract.ARG_DATA_PICK_MEDIA_ITEMS, MediaItem::class.java)
		} else {
			data?.getParcelableArrayListExtra(CutSameContract.ARG_DATA_PICK_MEDIA_ITEMS)
		}) ?: arrayListOf<MediaItem>()

		if (mediaList.size < mutableMediaItemList.size) {
			activity.finish()
			return
		}
		val templateItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			data.getParcelableExtra(CutSameContract.ARG_TEMPLATE_ITEM, TemplateItem::class.java)
		} else {
			data.getParcelableExtra(CutSameContract.ARG_TEMPLATE_ITEM)
		}
		if (templateItem == null) {
			activity.finish()
			return
		}
		val cutSameSource = CutSameContext.getCutSameSource(templateItem.zipPath)
		if (cutSameSource == null) {
			activity.finish()
			return
		}
		//将prepare后未填充的数据 与 从相册选择的数据进行填充
		mutableMediaItemList.forEachIndexed { index, mediaItem ->
			val item = mediaList[index]
			mutableMediaItemList[index] = mediaItem.copy(
				source = item.path,
				mediaSrcPath = item.path,
				type = if (item.isVideo()) MediaItem.TYPE_VIDEO else MediaItem.TYPE_PHOTO,
				oriDuration = if (item.isVideo()) item.duration else mediaItem.duration
			)
		}

		//等compose结束后再finish相册页
		runCatching {
			compose(cutSameSource, mutableMediaItemList)
		}.onSuccess {
			data.putParcelableArrayListExtra(CutSameContract.ARG_DATA_COMPRESS_RESULT_MEDIA_ITEMS, it)
			activity.setResult(Activity.RESULT_OK, data)
		}.onFailure {
			LogUtil.d(TAG, it.message ?: "compose fail")
			data.putParcelableArrayListExtra(CutSameContract.ARG_DATA_COMPRESS_RESULT_MEDIA_ITEMS, mutableMediaItemList)
			activity.setResult(Activity.RESULT_OK, data)
		}
		activity.finish()
	}

	private suspend fun compose(cutSameSource: CutSameSource, mediaItems: ArrayList<MediaItem>) = suspendCancellableCoroutine<ArrayList<MediaItem>> {
		cutSameSource.composeSource(mediaItems, object : ComposeSourceListener {
			override fun onError(errorCode: Int, message: String?) {
				LogUtil.d(TAG, "compose error,code:$errorCode,msg:$message")
				it.resumeWith(Result.failure(Throwable(message)))
			}

			override fun onProgress(progress: Float) {
				LogUtil.d(TAG, "compose progress:$progress")
			}

			override fun onSuccess(mediaItems: ArrayList<MediaItem>?) {
				LogUtil.d(TAG, "compose success,media size:${mediaItems?.size}")
				val list = mediaItems ?: arrayListOf()
				it.resumeWith(Result.success(list))
			}
		})
	}
}