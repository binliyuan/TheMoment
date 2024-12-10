package com.volcengine.effectone.auto.business.hl

import com.volcengine.auth.api.EOAuthorizationInternal
import com.volcengine.ck.highlight.config.HLSDKConfig
import com.volcengine.ck.highlight.ila.ILASDKInit
import com.volcengine.ck.logkit.LogKit
import com.volcengine.effectone.singleton.AppSingleton
import com.volcengine.effectone.utils.EOUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean

/**
 *Author: gaojin
 *Time: 2024/4/24 20:07
 */

object ILASDKInitHelper {

    private const val TAG = "ILASDKInitHelper"

    private var isInitialized = AtomicBoolean(false)


    suspend fun init(): Boolean {
        if (isInitialized.get()) {
            LogKit.d(TAG, "ILASDK redundant initialization")
            return true
        }
        return if (isInitialized.compareAndSet(false, true)) {
            val modelFileDir = EOUtils.pathUtil.internalResource("ilamodel")
            withContext(Dispatchers.IO) {
                checkLocalModel(modelFileDir.absolutePath)
            }
            val config = HLSDKConfig.Builder()
                .setTemplateProvider(AutoTemplateProvider())
                .setFrameExtractor(HLMIFrameExtractor())
                .modelPath(modelFileDir.absolutePath)
                .licensePath(EOAuthorizationInternal.getVELicensePath() ?: "")
                .build()
            ILASDKInit.init(config)
        } else {
            true
        }
    }

    private fun checkLocalModel(modelPath: String) {
        fun checkModelFile(name: String) {
            val input = AppSingleton.instance.assets.open("model" + File.separator + name)
            val dest = File(modelPath, name)
            copyToFileOrThrow(input, dest)
        }
        checkModelFile("lens_vida_aes_v1_0_model")
        checkModelFile("nodehub_c3_300_ilasdk_v1_0_model")
        checkModelFile("tt_after_effect_v6_0_size0_model")

        checkModelFile("tt_face_extra_v15_0_size0_model")
        checkModelFile("tt_face_v11_2_size0_model")
    }

    @Throws(IOException::class)
    private fun copyToFileOrThrow(inputStream: InputStream, destFile: File) {
        if (destFile.exists()) {
            return
        }
        val file = destFile.parentFile
        if (file != null && !file.exists()) {
            file.mkdirs()
        }
        inputStream.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}