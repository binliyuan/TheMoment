package com.volcengine.effectone.auto.templates.launch

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.result.contract.ActivityResultContract
import com.ss.android.ugc.cut_ui.MediaItem
import com.volcengine.effectone.auto.templates.ui.AutoCutSameClipActivity

/**
 * @author tyx
 * @description:
 * @date :2024/5/15 10:46
 */
class LaunchClipContract : ActivityResultContract<MediaItem, MediaItem>() {
	override fun createIntent(context: Context, input: MediaItem?): Intent {
		return AutoCutSameClipActivity.createClipIntent(context, input)
	}

	override fun parseResult(resultCode: Int, intent: Intent?): MediaItem? {
		if (resultCode == RESULT_OK) {
			return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				intent?.getParcelableExtra(AutoCutSameClipActivity.ARG_DATA_CLIP_MEDIA_ITEM, MediaItem::class.java)
			} else {
				intent?.getParcelableExtra(AutoCutSameClipActivity.ARG_DATA_CLIP_MEDIA_ITEM)
			}
		}
		return null
	}
}