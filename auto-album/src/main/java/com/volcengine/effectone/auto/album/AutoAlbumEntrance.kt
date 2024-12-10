package com.volcengine.effectone.auto.album

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import com.volcengine.ck.album.base.ALBUM_CONFIG_KEY
import com.volcengine.ck.album.base.AlbumConfig

/**
 *Author: gaojin
 *Time: 2023/1/28 17:16
 */

object AutoAlbumEntrance {

    val albumPermissions by lazy {
        mutableListOf<String>().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
                add(Manifest.permission.READ_MEDIA_VIDEO)
                add(Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    fun startChooseMedia(activity: Activity, requestCode: Int, albumConfig: AlbumConfig? = null) {
        val intent = Intent(activity, AutoAlbumActivity::class.java)
        albumConfig?.let {
            intent.putExtra(ALBUM_CONFIG_KEY, it)
        }
        activity.startActivityForResult(intent, requestCode)
    }
}