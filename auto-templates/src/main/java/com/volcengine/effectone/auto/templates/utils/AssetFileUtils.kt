package com.volcengine.effectone.auto.templates.utils

import android.content.res.AssetManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * @author tyx
 * @description:
 * @date :2024/4/27 18:16
 */
object AssetFileUtils {

	fun copyAssets(assets: AssetManager, path: String, rootDir: String) {
		if (isAssetsDir(assets, path)) {
			assets.list(path)?.forEach {
				copyAssets(assets, "$path/$it", rootDir)
			}
		} else {
			val input = assets.open(path)
			val dest = File(rootDir, path)
			copyToFileOrThrow(input, dest)
		}
	}

	private fun isAssetsDir(assets: AssetManager, path: String): Boolean {
		try {
			val files = assets.list(path)
			return !files.isNullOrEmpty()
		} catch (e: IOException) {
			e.printStackTrace()
		}
		return false
	}

	@Throws(IOException::class)
	fun copyToFileOrThrow(inputStream: InputStream, destFile: File) {
		if (destFile.exists()) {
			return
		}
		val file = destFile.getParentFile()
		if (file != null && !file.exists()) {
			file.mkdirs()
		}
		val out = FileOutputStream(destFile)
		try {
			val buffer = ByteArray(4096)
			var bytesRead: Int
			while (inputStream.read(buffer).also { bytesRead = it } >= 0) {
				out.write(buffer, 0, bytesRead)
			}
		} finally {
			out.flush()
			try {
				out.getFD().sync()
			} catch (e: IOException) {
			}
			out.close()
		}
	}
}