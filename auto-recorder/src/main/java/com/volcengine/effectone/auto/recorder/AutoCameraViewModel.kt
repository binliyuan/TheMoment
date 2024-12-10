package com.volcengine.effectone.auto.recorder

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.os.SystemClock
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.ss.android.vesdk.VEUtils
import com.volcengine.ck.logkit.LogKit
import com.volcengine.effectone.auto.recorder.base.AutoRecordScope
import com.volcengine.effectone.auto.recorder.config.AutoRecordConstant
import com.volcengine.effectone.recordersdk.RecordManager
import com.volcengine.effectone.recordersdk.RecordMediaItem
import com.volcengine.effectone.recordersdk.RecordMediaType
import com.volcengine.effectone.recordersdk.base.RecordAction
import com.volcengine.effectone.recorderui.vesdk.VESDKRecordManager
import com.volcengine.effectone.recorderui.viewmodel.CameraViewModel
import com.volcengine.effectone.singleton.AppSingleton
import com.volcengine.effectone.utils.EOUtils
import com.volcengine.effectone.utils.runOnUiThread
import com.volcengine.effectone.viewmodel.EffectOneViewModelFactory
import com.volcengine.effectone.widget.EOCommonDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume

/**
 *Author: gaojin
 *Time: 2024/4/16 17:53
 */

open class AutoCameraViewModel(activity: FragmentActivity) : CameraViewModel(activity) {
    companion object {
        private const val TAG = "AutoCameraViewModel"
        private const val MEDIA_PATTERN = "_yyyyMMdd_HHmmss"
        private const val MEDIA_JPG_EXTENSION = ".jpg"
        private const val MEDIA_MP4_EXTENSION = ".mp4"
        private const val MEDIA_JPG_PREFIX = "IMG"
        private const val MEDIA_MP4_PREFIX = "VID"
        fun get(activity: FragmentActivity): AutoCameraViewModel {
            return EffectOneViewModelFactory.viewModelProvider(activity).get(AutoCameraViewModel::class.java)
        }
    }

    var type: Int = AutoRecordConstant.ORDINARY_SHOOT

    //记录导出状态和结果 first value is loading state, second scan Uri
    val concatVideoStateWithResult = MutableLiveData<Pair<Boolean, Uri?>>()

    private val mainScope by lazy { AutoRecordScope() }

    private val simpleDateFormat by lazy { SimpleDateFormat(MEDIA_PATTERN, Locale.ENGLISH) }
    override fun getEORecordManager(): RecordManager {
        return VESDKRecordManager()
    }

    fun saveRecordMedias(context: Context, recordMediaItems: List<RecordMediaItem>) {
        mainScope.launch {
            concatVideoStateWithResult.value = true to null
            //车机可能没有权限，保护一下，不让Crash
            try {
                val result = withContext(Dispatchers.IO) {
                    concatMediaWithFilePath(context, recordMediaItems) //使用fs方式
                }
                LogKit.d(TAG, "saveRecordMedias() result = $result ")
                concatVideoStateWithResult.value = false to result
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(AppSingleton.instance, "视频保存失败，请检查权限", Toast.LENGTH_SHORT).show()
                }
                LogKit.e(TAG, "saveRecordMedias failed", e)
            }
        }
    }

    //使用FS方式写入
    private suspend fun concatMediaWithFilePath(
        context: Context,
        recordMediaItems: List<RecordMediaItem>
    ): Uri? {
        val startTimestamp = SystemClock.uptimeMillis()
        val inputVideoPaths = recordMediaItems.map { it.path }
        val type = recordMediaItems.firstNotNullOfOrNull { it.type } ?: return null
        val disPlayName = if (type == RecordMediaType.VIDEO)
            "$MEDIA_MP4_PREFIX${simpleDateFormat.format(Date())}$MEDIA_MP4_EXTENSION"
        else
            "$MEDIA_JPG_PREFIX${simpleDateFormat.format(Date())}$MEDIA_JPG_EXTENSION"
        val exportDir = File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)}${File.separator}TheMoment")
        try {
            if (exportDir.exists().not()) {
                exportDir.mkdirs()
            }
        } catch (e: Exception) {
            LogKit.e(TAG, "concatMedia() mkdirs failed ,dir = $exportDir", e)
            return null
        }
        val outVideoPath = if (type == RecordMediaType.VIDEO)
            "$exportDir/$disPlayName"
        else
            "$exportDir/$disPlayName"
        val mineType = if (type == RecordMediaType.VIDEO) "video/mp4"
        else "image/jpg"

        return suspendCancellableCoroutine { cancellableContinuation ->
            val concatVideoResult = VEUtils.concatVideo(
                inputVideoPaths.toTypedArray(),
                outVideoPath
            )
            LogKit.d(TAG, "concatMediaWithFilePath: outVideoPath = $outVideoPath, concatVideoResultCode = $concatVideoResult, cost = ${SystemClock.uptimeMillis() - startTimestamp}")
            val concatResult = concatVideoResult == 0
            //合成失败，不需要扫描了
            if (concatResult.not()) cancellableContinuation.resume(null)
            mainScope.launch(Dispatchers.IO) {
                val scanPath = scanPath(context, outVideoPath, mineType)
                withContext(Dispatchers.Main) {
                    cancellableContinuation.resume(scanPath)
                }
                inputVideoPaths.forEach {
                    LogKit.d(TAG, "concatMediaWithFilePath() inputVideoPaths delete $ it")
                    runCatching {
                        File(it).deleteRecursively()
                    }
                }
            }
        }
    }

    //扫描
    private suspend fun scanPath(context: Context, path: String, mimeType: String): Uri? {
        return suspendCancellableCoroutine { continuation ->
            MediaScannerConnection.scanFile(
                context,
                arrayOf(path),
                arrayOf(mimeType)
            ) { _, scannedUri ->
                LogKit.d(TAG, "scanPath  path = $path, scannedUri = $scannedUri")
                continuation.resume(scannedUri)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mainScope.cancel()
    }

    fun existRecordPage(
        activity: FragmentActivity,
        dialogShowAction: () -> Unit,
        dialogDismissAction: () -> Unit
    ) {
        if (recordAction.value == RecordAction.START_RECORD) return
        if (!isRecordListEmpty()) {
            EOCommonDialog.Builder(activity)
                .setTitle(com.volcengine.effectone.recorderui.R.string.eo_camera_discard)
                .setContent(R.string.auto_recorder_exist_page_tip)
                .setConfirmText(com.volcengine.effectone.baseui.R.string.eo_base_popout_confirm)
                .setCancelText(com.volcengine.effectone.baseui.R.string.eo_base_popout_no)
                .setConfirmListener(object :
                    EOCommonDialog.OnConfirmListener {
                    override fun onClick() {
                        deleteAllVideos()
                        activity.finish()
                    }
                }).create().apply {
                    setOnDismissListener {
                        dialogDismissAction()
                    }
                    show()
                    dialogShowAction()
                }
        } else {
            activity.finish()
        }
    }

    fun clearWorkSpace() {
        mainScope.launch(Dispatchers.IO) {
            EOUtils.fileUtil.deleteFilesAndFoldersInFolder(EOUtils.pathUtil.internalResource("vesdk"))
        }
    }

    fun isHLShoot() = type == AutoRecordConstant.HIGHLIGHT_SHOOT
}