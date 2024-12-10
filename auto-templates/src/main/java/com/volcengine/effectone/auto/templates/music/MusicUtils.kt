package com.volcengine.effectone.auto.templates.music

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import com.volcengine.effectone.singleton.AppSingleton
import com.volcengine.effectone.utils.EOUtils
import java.io.File

/**
 *Author: gaojin
 *Time: 2023/12/25 20:06
 */

object MusicUtils {

    private const val NAME = "auto_music"
    private const val VERSION = "versionCode"

    private val mSp: SharedPreferences =
        AppSingleton.instance.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    val musicResRootPath by lazy {
        File(AppSingleton.instance.filesDir, "auto_music").apply {
            if (!exists()) {
                mkdir()
            }
        }
    }

    fun copyMusicRes() {
        musicResRootPath.deleteRecursively()
        EOUtils.fileUtil.copyAssets(AppSingleton.instance.assets, "music", "", musicResRootPath.absolutePath)
        setVersion(getVersionCode(AppSingleton.instance))
    }

    private fun setVersion(version: Int) {
        mSp.edit().putInt(VERSION, version).apply()
    }

    fun musicResIsReady(): Boolean {
        val savedVersionCode = mSp.getInt(VERSION, 0)
        val currentVersionCode: Int = getVersionCode(AppSingleton.instance)
        return savedVersionCode >= currentVersionCode
    }

    private fun getVersionCode(context: Context): Int {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            -1
        }
    }
}