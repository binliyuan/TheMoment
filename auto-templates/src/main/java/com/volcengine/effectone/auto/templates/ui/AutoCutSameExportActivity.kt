package com.volcengine.effectone.auto.templates.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bytedance.ies.nle.editor_jni.NLEModel
import com.cutsame.solution.player.CutSamePlayer
import com.gyf.immersionbar.ktx.immersionBar
import com.ss.android.medialib.common.LogUtil
import com.ss.android.ugc.aweme.views.setDebounceOnClickListener
import com.ss.ugc.android.editor.track.utils.visible
import com.volcengine.effectone.auto.common.cutsame.CutSameContract
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.bean.BaseResultData
import com.volcengine.effectone.auto.templates.cutsame.CutSameContext
import com.volcengine.effectone.auto.templates.event.TrackNodeEvent
import com.volcengine.effectone.auto.templates.vm.AutoCutSameExportViewModel
import com.volcengine.effectone.auto.templates.widget.ExportView
import com.volcengine.effectone.utils.SizeUtil
import java.io.File

/**
 * @author tyx
 * @description:
 * @date :2024/5/20 10:00
 */
class AutoCutSameExportActivity : AppCompatActivity() {

	companion object {
		private const val TAG = "AutoCutSameExportActivity"
		fun createIntent(context: Context, cacheKey: String): Intent {
			val intent = Intent(context, AutoCutSameExportActivity::class.java)
			intent.putExtra(CutSameContract.ARG_CUT_TEMPLATE_CACHE_KEY, cacheKey)
			return intent
		}
	}

	private val mAutoCutSameExportViewModel by lazy { AutoCutSameExportViewModel.create(this) }
	private lateinit var mExportView: ExportView
	private lateinit var mExportCover: ImageView
	private lateinit var mBtnComplete: Button
	private var mCutSamePlayer: CutSamePlayer? = null

	private val mExportFile by lazy {
		val rootFile = File(externalCacheDir, "cutSameExport")
		if (!rootFile.exists()) rootFile.mkdirs()
		File(rootFile, "CutSame-${System.currentTimeMillis()}.mp4")
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		immersionBar {
			transparentStatusBar()
			navigationBarColor(com.gyf.immersionbar.R.color.abc_decor_view_status_guard)
			statusBarDarkFont(false)
			fitsSystemWindows(true)
		}

		val cacheKey = intent.getStringExtra(CutSameContract.ARG_CUT_TEMPLATE_CACHE_KEY)
		if (cacheKey.isNullOrEmpty()) {
			finish()
			LogUtil.d(TAG, "cacheKey is empty")
			return
		}

		mCutSamePlayer = CutSameContext.getCutSamePlayer(cacheKey)
		if (mCutSamePlayer == null) {
			LogUtil.d(TAG, "cutSamePlayer is null")
			finish()
			return
		}
		setContentView(R.layout.activity_cut_same_export)

		mExportCover = findViewById(R.id.export_cover)
		mExportView = findViewById(R.id.export_progress)
		mBtnComplete = findViewById(R.id.btn_complete)
		findViewById<ImageView>(R.id.iv_back).setDebounceOnClickListener { finish() }
		mBtnComplete.setDebounceOnClickListener { finish() }
		startObserver()
		initExportView()
	}

	private fun initExportView() {
		val canvasSize = mCutSamePlayer?.getConfigCanvasSize()
		val videoWidth = canvasSize?.width ?: 0
		val videoHeight = canvasSize?.height ?: 0
		LogUtil.d(TAG, "videoW:$videoWidth,videoH:$videoHeight")
		if (videoWidth == 0 || videoHeight == 0) {
			finish()
			return
		}
		val previewWidth: Int
		val previewHeight: Int
		if (videoWidth >= videoHeight) {
			previewWidth = SizeUtil.dp2px(446f)
			previewHeight = videoHeight * previewWidth / videoWidth
		} else {
			previewHeight = SizeUtil.dp2px(446f)
			previewWidth = videoWidth * previewHeight / videoHeight
		}
		val params = mExportCover.layoutParams
		mExportCover.layoutParams = params.apply {
			width = previewWidth
			height = previewHeight
		}
		mExportView.updateCanvasSize(previewWidth, previewHeight)
		mAutoCutSameExportViewModel.getSpecificImage(mCutSamePlayer, 100, videoWidth, videoHeight)
	}

	private fun startObserver() {
		mAutoCutSameExportViewModel.mSpecificImage.observe(this) { result ->
			result ?: return@observe
			mExportCover.setImageBitmap(result)
			mCutSamePlayer?.let { player ->
				mAutoCutSameExportViewModel.export(player, mExportFile.absolutePath, getModel(player), player.getConfigCanvasSize())
			}
		}
		mAutoCutSameExportViewModel.mExportResult.observe(this) { result ->
			result ?: return@observe
			when (result.state) {
				BaseResultData.START -> {
					TrackNodeEvent.onExportStart()
					mExportView.updateState(false)
				}

				BaseResultData.END -> {
					TrackNodeEvent.onExportEnd()
					mBtnComplete.visible = true
					mExportView.updateState(true)
					result.resultData?.onSuccess {
						mAutoCutSameExportViewModel.saveAlbum(it)
					}?.onFailure {
						LogUtil.d(TAG, "导出失败 msg:${it.message}")
						Toast.makeText(this, "导出失败!", Toast.LENGTH_SHORT).apply {
							setGravity(Gravity.CENTER, 0, 0)
						}.show()
					}
				}

				BaseResultData.PROGRESS -> mExportView.updateProgress(result.progress)
			}
		}
		mAutoCutSameExportViewModel.mSaveAlbumResult.observe(this) { result ->
			Toast.makeText(this, result.second, Toast.LENGTH_SHORT)
				.apply {
					setGravity(Gravity.CENTER, 0, 0)
				}.show()
		}
	}

	private fun getModel(player: CutSamePlayer): NLEModel {
		return player.draft.getModel()
	}

	override fun onDestroy() {
		super.onDestroy()
		mCutSamePlayer?.let {
			mExportFile.delete()
			mAutoCutSameExportViewModel.cancelExport(it)
		}
	}
}