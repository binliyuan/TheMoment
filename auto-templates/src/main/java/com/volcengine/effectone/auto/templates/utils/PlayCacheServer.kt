package com.volcengine.effectone.auto.templates.utils

import com.danikula.videocache.HttpProxyCacheServer
import com.volcengine.effectone.singleton.AppSingleton
import java.io.File

object PlayCacheServer {

    val proxyServer: HttpProxyCacheServer by lazy {
        val externalFilesDir = AppSingleton.instance.getExternalFilesDir("videoCache") ?: File(
            AppSingleton.instance.filesDir.absolutePath,
            "videoCache"
        )
        HttpProxyCacheServer.Builder(AppSingleton.instance)
            .cacheDirectory(externalFilesDir)
            .maxCacheSize(1024 * 1024 * 1024)  // 1 Gb for cache
            .build()
    }
}