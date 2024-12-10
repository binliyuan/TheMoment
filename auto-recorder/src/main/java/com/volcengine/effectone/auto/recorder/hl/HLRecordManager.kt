package com.volcengine.effectone.auto.recorder.hl

import android.graphics.Bitmap
import androidx.annotation.MainThread
import com.ss.android.vesdk.VEImageUtils
import com.volcengine.ck.highlight.data.HLResult
import com.volcengine.ck.highlight.ila.ILASDKInit
import com.volcengine.ck.logkit.LogKit
import com.volcengine.effectone.utils.EOUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executors

/**
 *Author: gaojin
 *Time: 2023/10/10 15:52
 */

class HLRecordManager(
    private val hlManagerScope: CoroutineScope,
    private val hlDependAbility: HLDependAbility
) {

    companion object {
        private const val TAG = "HLRecordManager"
    }

    private val singleThreadDispatcher by lazy {
        Executors.newSingleThreadExecutor { r ->
            Thread(r).apply {
                name = "eo-auto-highlight"
            }
        }.asCoroutineDispatcher()
    }

    private val config = HLRecordConfig().initConfig()

    private val recognizeQueue = ArrayList<HLResult>(config.checkFrameTotalCount)

    @Volatile
    private var isRecord = false

    @Volatile
    private var isBreakWhile = true

    fun recordingTime(endFrameTime: Long) {
        if (endFrameTime >= config.maxVideoLength && isRecord) {
            stopRecord()
        }
    }

    fun getMaxVideoLength(): Long {
        return config.maxVideoLength
    }

    fun start() {
        if (!isBreakWhile) {
            return
        }
        LogKit.i(TAG, "start")
        recognizeQueue.clear()
        isRecord = false
        isBreakWhile = false

        hlManagerScope.launch(singleThreadDispatcher) {
            while (true) {
                if (isBreakWhile) {
                    break
                }
                recognizeSingleFrame(recognizeQueue)
                if (!isRecord) {
                    val canRecord = checkResult(recognizeQueue)
                    withContext(Dispatchers.Main) {
                        if (canRecord) {
                            startRecord()
                        }
                    }
                }
                delay(config.checkTimeInterval)
            }
        }
    }

    @MainThread
    private fun startRecord() {
        LogKit.i(TAG, "startRecord")
        if (isRecord) {
            LogKit.i(TAG, "already startRecord")
            return
        }
        isRecord = true
        hlDependAbility.startRecord()
    }

    /**
     * 停止拍摄是异步的
     * 这个方法只是触发停止拍摄，拍摄真正停止时由外部回调「realStop」
     */
    @MainThread
    private fun stopRecord() {
        LogKit.i(TAG, "stopRecord")
        if (!isRecord) {
            LogKit.i(TAG, "already stopRecord")
            return
        }
        hlDependAbility.stopRecord()
    }

    /**
     * 这里由外部回调触发，停止拍摄是异步的
     */
    fun realStop() {
        LogKit.i(TAG, "realStop")
        hlManagerScope.launch(singleThreadDispatcher) {
            //防止出现拍摄结束后又立刻开始拍摄的情况
            delay(1100L)
            isRecord = false
        }
    }

    private fun checkResult(recognizeQueue: ArrayList<HLResult>): Boolean {
        if (recognizeQueue.size < config.checkFrameLegalCount) {
            return false
        }
        var index = 0
        recognizeQueue.forEach {
            val scoreCondition = (it.score?.score ?: 0F) > config.minScoreRequire
            var c3Condition = false
            if (config.c3Category.isEmpty()) {
                c3Condition = true
            } else {
                config.c3Category.forEach { category ->
                    it.c3.forEach { hlCategory ->
                        if (category == hlCategory.name) {
                            c3Condition = true
                        }
                    }
                }
            }
            if (scoreCondition && c3Condition) {
                index++
            }
        }
        return index >= config.checkFrameLegalCount
    }

    fun stop() {
        LogKit.i(TAG, "stop")
        isBreakWhile = true
        hlDependAbility.onFrameRecognized(null)
        if (isRecord) {
            stopRecord()
        }
        recognizeQueue.clear()
    }

    private suspend fun recognizeSingleFrame(recognizeQueue: ArrayList<HLResult>) {
        val data = hlDependAbility.getRecordFrameData() ?: return
        val result = ILASDKInit.recognizeFrameData(data.width, data.height, data.ptsMs, rgbaIntArrayToByteArray(data.intArray))
        result?.let {
            withContext(Dispatchers.Main) {
                hlDependAbility.onFrameRecognized(it)
            }
            if (recognizeQueue.isNotEmpty() && recognizeQueue.size >= config.checkFrameTotalCount) {
                recognizeQueue.removeFirst()
            }
            recognizeQueue.add(it)
//            saveFrameData(data)
        }
    }

    private fun rgbaIntArrayToByteArray(intArray: IntArray): ByteArray {
        val byteArray = ByteArray(intArray.size * 4)
        for (i in intArray.indices) {
            val pixel = intArray[i]
            byteArray[i * 4] = (pixel shr 0 and 0xFF).toByte() // R
            byteArray[i * 4 + 1] = (pixel shr 8 and 0xFF).toByte() // G
            byteArray[i * 4 + 2] = (pixel shr 16 and 0xFF).toByte() // B
            byteArray[i * 4 + 3] = (pixel shr 24 and 0xFF).toByte() // A
        }
        return byteArray
    }

    private fun saveFrameData(previewData: PreviewData) {
        previewData.run {
            val createBitmap: Bitmap =
                Bitmap.createBitmap(intArray, width, height, Bitmap.Config.ARGB_8888)
            VEImageUtils.compressToJPEG(
                createBitmap,
                100,
                File(EOUtils.pathUtil.internalCache("hl_image"), "${System.currentTimeMillis()}.jpeg").absolutePath
            )
        }
    }
}