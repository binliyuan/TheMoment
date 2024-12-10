package com.volcengine.effectone.auto.templates.vm

import android.content.ContentValues
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import bytedance.io.BdMediaFileSystem
import com.bytedance.ies.cutsame.util.Size
import com.bytedance.ies.cutsame.veadapter.CompileListener
import com.bytedance.ies.nle.editor_jni.NLEModel
import com.cutsame.solution.compile.CompileParam
import com.cutsame.solution.compile.ExportResolution
import com.cutsame.solution.player.CutSamePlayer
import com.ss.android.vesdk.VEVideoEncodeSettings
import com.volcengine.effectone.auto.templates.bean.BaseResultData
import com.volcengine.effectone.auto.templates.bean.ExportResult
import com.volcengine.effectone.singleton.AppSingleton
import java.io.File

/**
 * @author tyx
 * @description:
 * @date :2024/5/20 10:40
 */
open class AutoCutSameExportViewModel : ViewModel() {

	companion object {
		fun create(owner: ViewModelStoreOwner): AutoCutSameExportViewModel {
			return ViewModelProvider(owner).get(AutoCutSameExportViewModel::class.java)
		}
	}

	val mSpecificImage = MutableLiveData<Bitmap>()
	val mExportResult = MutableLiveData<ExportResult>()
	val mSaveAlbumResult = MutableLiveData<Pair<Boolean, String>>()

	fun getSpecificImage(player: CutSamePlayer?, timeStamp: Int, width: Int, height: Int) {
		player?.getSpecificImage(timeStamp, width, height) { bitmap ->
			mSpecificImage.postValue(bitmap)
		}
	}

	fun export(player: CutSamePlayer, targetFilePath: String, nelModel: NLEModel, size: Size) {
		val exportResult = ExportResult().reset().setState(BaseResultData.START)
		mExportResult.postValue(exportResult)
		player.compileNLEModel(nelModel, targetFilePath, createCompileParam(), size,
			object : CompileListener {
				override fun onCompileError(p0: Int, p1: Int, p2: Float, p3: String?) {
					exportResult.setResultData(Result.failure(Throwable(p3))).setState(BaseResultData.END)
					mExportResult.postValue(exportResult)
				}

				override fun onCompileDone() {
					exportResult.setResultData(Result.success(targetFilePath)).setState(BaseResultData.END)
					mExportResult.postValue(exportResult)
				}

				override fun onCompileProgress(p0: Float) {
					exportResult.setProgress(p0).setState(BaseResultData.PROGRESS)
					mExportResult.postValue(exportResult)
				}
			}
		)
	}

	private var mCurrentFile: File? = null

	fun saveAlbum(path: String, del: Boolean = true) {
		val context = AppSingleton.instance
		val videoFile = File(path)
		runCatching {
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
				val resolver = context.contentResolver
				val contentValues = ContentValues()
				contentValues.run {
					val time = System.currentTimeMillis()
					put(MediaStore.MediaColumns.DISPLAY_NAME, path)
					put(MediaStore.MediaColumns.MIME_TYPE, "video/*")
					put(MediaStore.MediaColumns.DATE_ADDED, time)
					put(MediaStore.MediaColumns.DATE_MODIFIED, time)
					put(MediaStore.MediaColumns.IS_PENDING, 1)
					put(MediaStore.MediaColumns.DURATION, getLocalVideoDuration(path))
				}
				resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)?.run {
					resolver.openOutputStream(this)?.use {
						videoFile.inputStream().copyTo(it)
					}
					contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
					resolver.update(this, contentValues, null, null)
				}
			} else {
				val picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
				val target = File(picDir, videoFile.name)
				videoFile.copyTo(target, true)
				BdMediaFileSystem.updateMediaStore(context, target.absolutePath)
			}
		}.onSuccess {
			mSaveAlbumResult.value = true to "已保存至本地相册"
			if (del) videoFile.delete()
		}.onFailure {
			mSaveAlbumResult.value = false to "保存到相册失败!"
			videoFile.delete()
		}
	}

	private fun getLocalVideoDuration(videoPath: String): Int {
		val duration: Int
		var mRetriever: MediaMetadataRetriever? = null
		val result = runCatching {
			mRetriever = MediaMetadataRetriever()
			mRetriever?.setDataSource(videoPath)
			mRetriever?.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt() ?: 0
		}
		mRetriever?.release()
		duration = result.getOrNull() ?: 0
		return duration
	}

	fun cancelExport(player: CutSamePlayer) {
		mCurrentFile?.delete()
		player.cancelCompile()
	}

	private fun createCompileParam(): CompileParam {
		val spManager = PreferenceManager.getDefaultSharedPreferences(AppSingleton.instance)
		val supportHwEncoder = spManager.getBoolean("eo_cutsame_hw_encode", true)
		val bps = 16 * 1024 * 1024
		val fps = 30
		val gopSize = 35
		val swMaxRate = 1024 * 1024 * 30L
		val swCRF = 21
		return CompileParam(
			ExportResolution.V_1080P,
			supportHwEncoder,
			bps,
			fps,
			gopSize,
			swMaxRate,
			swCRF,
			VEVideoEncodeSettings.ENCODE_BITRATE_MODE.ENCODE_BITRATE_CRF
		)
	}
}