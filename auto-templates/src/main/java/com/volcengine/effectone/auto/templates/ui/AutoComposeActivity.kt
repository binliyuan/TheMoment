package com.volcengine.effectone.auto.templates.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cutsame.solution.source.CutSameSource
import com.cutsame.solution.template.model.TemplateItem
import com.gyf.immersionbar.ktx.immersionBar
import com.ss.android.ugc.cut_log.LogUtil
import com.ss.android.ugc.cut_ui.MediaItem
import com.volcengine.effectone.auto.common.cutsame.CutSameContract
import com.volcengine.effectone.auto.templates.bean.BaseResultData
import com.volcengine.effectone.auto.templates.cutsame.CutSameContext
import com.volcengine.effectone.auto.templates.event.TrackNodeEvent
import com.volcengine.effectone.auto.templates.vm.CutSameComposeViewModel

/**
 * @author tyx
 * @description:
 * @date :2024/5/15 14:55
 */
class AutoComposeActivity : AppCompatActivity() {

	companion object {
		private const val TAG = "AutoComposeActivity"
		fun createIntent(context: Context, mediaItems: ArrayList<MediaItem>, templateItem: TemplateItem): Intent {
			val intent = Intent(context, AutoComposeActivity::class.java)
			intent.putExtra(CutSameContract.ARG_TEMPLATE_ITEM, templateItem)
			intent.putParcelableArrayListExtra(CutSameContract.ARG_DATA_COMPRESS_MEDIA_ITEMS, mediaItems)
			return intent
		}
	}

	private val mComposeViewModel by lazy { CutSameComposeViewModel.create(this) }
	private var mCutSameSource: CutSameSource? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		overridePendingTransition(0, 0)
		immersionBar {
			transparentStatusBar()
			navigationBarColor(com.gyf.immersionbar.R.color.abc_decor_view_status_guard)
			statusBarDarkFont(false)
		}
		val mediaItems = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			intent.getParcelableArrayListExtra(CutSameContract.ARG_DATA_COMPRESS_MEDIA_ITEMS, MediaItem::class.java)
		} else intent.getParcelableArrayListExtra(CutSameContract.ARG_DATA_COMPRESS_MEDIA_ITEMS))
		if (mediaItems.isNullOrEmpty()) {
			Toast.makeText(this, "mediaItems is empty", Toast.LENGTH_SHORT).show()
			LogUtil.d(TAG, "mediaItems is empty")
			finish()
			return
		}
		val templateItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			intent.getParcelableExtra(CutSameContract.ARG_TEMPLATE_ITEM, TemplateItem::class.java)
		} else {
			intent.getParcelableExtra(CutSameContract.ARG_TEMPLATE_ITEM)
		}
		val key = templateItem?.zipPath ?: ""
		LogUtil.d(TAG, "template cache key:$key")
		mCutSameSource = CutSameContext.getCutSameSource(key)
		if (mCutSameSource == null) {
			Toast.makeText(this, "cutSameSource is null", Toast.LENGTH_SHORT).show()
			LogUtil.d(TAG, "cutSameSource is null")
			finish()
			return
		}
		startObserver()
		showFragment()
		mComposeViewModel.composeSource(mCutSameSource, mediaItems)
	}

	private fun showFragment() {
		val fragment = supportFragmentManager.findFragmentByTag(AutoComposeFragment.TAG)
			?: AutoComposeFragment().apply { arguments = intent.extras }
		supportFragmentManager.beginTransaction()
			.replace(android.R.id.content, fragment, AutoComposeFragment.TAG)
			.commitNow()
	}

	private fun startObserver() {
		mComposeViewModel.mComposeLiveData.observe(this) { result ->
			result ?: return@observe
			when (result.state) {
				BaseResultData.START -> TrackNodeEvent.onComposeStart()
				BaseResultData.END -> {
					TrackNodeEvent.onComposeEnd()
					result.resultData?.onSuccess {
						val intent = Intent()
						intent.putExtra(CutSameContract.ARG_DATA_COMPRESS_RESULT_MEDIA_ITEMS, it)
						setResult(RESULT_OK, intent)
					}?.onFailure {
						setResult(Activity.RESULT_CANCELED, Intent())
					}
					finish()
				}
			}
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		if (mComposeViewModel.mComposeLiveData.value?.state != BaseResultData.END) {
			mCutSameSource?.cancelCompose()
		}
	}

	override fun finish() {
		super.finish()
		overridePendingTransition(0, 0)
	}
}