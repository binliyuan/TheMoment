package com.volcengine.effectone.auto.moment.test

import android.app.Activity
import cn.everphoto.sdk.extension.BitmapProcessor
import com.bytedance.creativex.mediaimport.repository.api.IMaterialItem
import com.bytedance.creativex.mediaimport.repository.api.IMediaItem
import com.bytedance.creativex.mediaimport.util.getMediaFileAbsolutePath
import com.bytedance.ilasdk.jni.AEPixelFormat
import com.volcengine.ck.album.api.IAlbumFinish
import com.volcengine.ck.album.base.AlbumConfig
import com.volcengine.ck.highlight.ila.ILASDKInit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 *Author: gaojin
 *Time: 2024/6/5 17:14
 */

class RecognizeFinishImpl : IAlbumFinish {

    override suspend fun finishAction(activity: Activity, mediaList: List<IMaterialItem>, albumConfig: AlbumConfig) {
        withContext(Dispatchers.IO) {
            val path = getMediaFileAbsolutePath(mediaList.first() as IMediaItem)
            val bitmap = BitmapProcessor.getBitmap(path, 1080) ?: return@withContext
            val byteArray = BitmapProcessor.getBytes(bitmap)
            ILASDKInit.recognizeFrameData(bitmap.width, bitmap.height, 0L, byteArray, AEPixelFormat.RGBA8UNORM)
        }
    }

}