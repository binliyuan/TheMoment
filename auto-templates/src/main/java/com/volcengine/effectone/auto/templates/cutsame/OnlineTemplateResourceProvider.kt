package com.volcengine.effectone.auto.templates.cutsame

import android.preference.PreferenceManager
import com.cutsame.solution.template.model.TemplateItem
import com.ss.android.ugc.cut_log.LogUtil
import com.volcengine.effectone.auto.templates.download.DownloadManager
import com.volcengine.effectone.auto.templates.download.OnProgressListener
import com.volcengine.effectone.singleton.AppSingleton
import com.volcengine.effectone.utils.EOUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * @author tyx
 * @description:
 * @date :2024/6/11 16:55
 */
class OnlineTemplateResourceProvider : ITemplateResourceProvider {

    companion object {
        private const val TAG = "OnlineTemplateResourceProvider"
        private const val HOST_URL = "https://cvsdk.volccdn.com/auto/cutsame"
        private const val RESOURCE_JSON_URL = "${HOST_URL}/config/1.0"
        private const val DEFAULT_JSON_NAME = "auto_templates_0619.json"
    }


    override fun getTemplateList(): List<TemplateItem> {
        val resources = mutableListOf<TemplateItem>()
        val defaultSp = PreferenceManager.getDefaultSharedPreferences(AppSingleton.instance)
        var jsonFileName = defaultSp.getString("auto_cutsame_remote_json_name", DEFAULT_JSON_NAME) ?: DEFAULT_JSON_NAME
        if (jsonFileName.isEmpty()) {
            jsonFileName = DEFAULT_JSON_NAME
        }
        runCatching {
            val json = DownloadManager.readUrlJson("${RESOURCE_JSON_URL}/${jsonFileName}")
            AutoTemplateResourceHelper.readJsonStringToTemplateResource(json)
        }.onSuccess { data ->
            resources.addAll(data)
        }.onFailure {
            LogUtil.d(TAG, "getOnlineTempRes fail:${it.message}")
        }
        return resources
    }

    override suspend fun loadTemplatesResource(templateItem: TemplateItem, callback: ILoadTemplatesResourceCallback?): TemplateItem {
        var zipPath = templateItem.zipPath
        if (zipPath.startsWith("file://")) {
            val zipUrl = zipPath.substringAfter("file://")
            val rootFile = EOUtils.pathUtil.internalResource("template")
            val saveFile = File(rootFile, zipUrl)
            if (saveFile.exists().not()) {
                withContext(Dispatchers.IO) {
                    val url = "$HOST_URL/template/$zipUrl"
                    LogUtil.d(TAG, "startDownload url:$url")
                    val result = DownloadManager.download(url, saveFile, object : OnProgressListener {
                        override fun onProgress(process: Int) {
                            callback?.onProgress(process)
                        }
                    })
                    if (result.file != null) {
                        zipPath = result.file.absolutePath
                    } else {
                        if (saveFile.isFile) saveFile.delete()
                        LogUtil.d(TAG, "onlineLoadTemplatesResource fail:${result.e}")
                    }
                }
            } else zipPath = saveFile.absolutePath
        }
        return templateItem.copy(
            templateUrl = zipPath,
            zipPath = zipPath
        )
    }
}