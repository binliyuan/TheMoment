package com.volcengine.effectone.auto.templates.launch

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.result.contract.ActivityResultContract
import com.ss.android.ugc.cut_log.LogUtil
import com.ss.android.ugc.cut_ui.MediaItem
import com.volcengine.ck.album.base.ALBUM_CONFIG_KEY
import com.volcengine.ck.album.base.AlbumConfig
import com.volcengine.effectone.auto.common.cutsame.CutSameContract
import com.volcengine.effectone.auto.templates.ui.AutoCutSameAlbumActivity

/**
 * @author tyx
 * @description:
 * @date :2024/5/9 15:38
 */
class LaunchPickerContract : ActivityResultContract<LaunchAlbumInput, ArrayList<MediaItem>>() {

	companion object {
		private const val TAG = "LaunchPickerContract"
	}

	override fun createIntent(context: Context, input: LaunchAlbumInput): Intent {
		val intent = Intent(context, AutoCutSameAlbumActivity::class.java)
		val config = AlbumConfig(
			enableDocker = false,
			allEnable = true,
			imageEnable = true,
			videoEnable = true,
		)
		intent.putParcelableArrayListExtra(CutSameContract.ARG_DATA_PICK_MEDIA_ITEMS, input.mediaItems)
		intent.putExtra(CutSameContract.ARG_TEMPLATE_ITEM, input.templateItem)
		intent.putExtra(ALBUM_CONFIG_KEY, config)
		return intent
	}

	override fun parseResult(resultCode: Int, intent: Intent?): ArrayList<MediaItem> {
		//选择并填充并compose后的数据
		val list = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			intent?.getParcelableArrayListExtra(CutSameContract.ARG_DATA_COMPRESS_RESULT_MEDIA_ITEMS, MediaItem::class.java)
		} else {
			intent?.getParcelableArrayListExtra(CutSameContract.ARG_DATA_COMPRESS_RESULT_MEDIA_ITEMS)
		}
		LogUtil.d(TAG, "parseResult list size:${list?.size}")
		return list ?: arrayListOf()
	}
}