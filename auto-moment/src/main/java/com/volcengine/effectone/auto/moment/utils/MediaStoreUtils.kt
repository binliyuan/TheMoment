package com.volcengine.effectone.auto.moment.utils

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.text.TextUtils
import com.volcengine.ck.highlight.config.HLMediaType
import com.volcengine.ck.highlight.data.RecognizeMedia
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern


const val MIME_TYPE_OTHER = -1
const val MIME_TYPE_UNKNOWN = 0
const val MIME_TYPE_JPEG = 1
const val MIME_TYPE_GIF = 2
const val MIME_TYPE_PNG = 3
const val MIME_TYPE_WEBP = 4
const val MIME_TYPE_BMP = 5
const val MIME_TYPE_MP4 = 6
const val MIME_TYPE_HEIF = 7
const val MIME_TYPE_QUICKTIME = 9
const val MIME_TYPE_3GPP = 10
const val MIME_TYPE_H264 = 11
const val MIME_TYPE_TIFF = 12

// font
const val MIME_TYPE_TTC = 31
const val MIME_TYPE_TTF = 32    // ttf 包含固定兼容格式的otf
const val MIME_TYPE_OTF = 33

private val PAT_RESOLUTION = Pattern.compile("(\\d+)[xX](\\d+)")


fun queryMedias(context: Context): List<RecognizeMedia> {
    val ret: MutableList<RecognizeMedia> = ArrayList<RecognizeMedia>()
    ret.addAll(queryImages(context))
    ret.addAll(queryVideos(context))
    Collections.sort(ret, Comparator { o1: RecognizeMedia?, o2: RecognizeMedia? ->
        if (o1 == null && o2 == null) {
            return@Comparator 0
        }
        if (o1 == null) {
            return@Comparator 1
        }
        if (o2 == null) {
            return@Comparator -1
        }
        o2.date.compareTo(o1.date)
    })
    return ret
}


private fun queryVideos(context: Context): List<RecognizeMedia> {
    val listOfAllVideos: MutableList<RecognizeMedia> = ArrayList<RecognizeMedia>()

    /**
     * Android 10 以下通过 MediaStore.Video.Media.EXTERNAL_CONTENT_URI 查询 video.
     * 没有 orientation colum, 所以在这不查询 orientation, 给 orientation 默认值 -1
     */
    val projection = arrayOf(
        MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA, MediaStore.Video.Media.DATE_TAKEN, MediaStore.Video.Media.MIME_TYPE, MediaStore.Video.Media.WIDTH, MediaStore.Video.Media.HEIGHT,  //                MediaStore.Video.Media.ORIENTATION,
        MediaStore.Video.Media.RESOLUTION, MediaStore.Video.Media.DURATION, MediaStore.Video.Media.SIZE
    )
    val selection = MediaStore.Video.Media.DURATION + " >= ?"
    val selectionArgs = arrayOf(TimeUnit.MILLISECONDS.convert(0, TimeUnit.SECONDS).toString())
    val targetImportPath = ""
    var cursor: Cursor? = null
    try {
        // maybe throw SecurityException Permission Denial
        cursor = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null
        )
        assert(cursor != null)
        val column_index_id = cursor!!.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        val column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
        val column_index_taken = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN)
        val column_index_mime = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
        val column_index_size = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
        val column_index_width = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
        val column_index_height = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
        val column_index_video_reslution = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RESOLUTION)
        val column_index_video_duration = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
        //            int column_index_video_orientation = cursor.getColumnIndex(MediaStore.Video.Media.ORIENTATION);
        while (cursor.moveToNext()) {
            try {
                val id = cursor.getLong(column_index_id)
                val absolutePathOfImage = cursor.getString(column_index_data)
                if (TextUtils.isEmpty(absolutePathOfImage)) {
                    continue
                }
                if (!TextUtils.isEmpty(targetImportPath) && !absolutePathOfImage.contains(
                        targetImportPath
                    )) {
                    continue
                }
                var taken = cursor.getLong(column_index_taken)
                /**
                 * media store 取的taken有可能是0，从而导致部分照片的导入顺序被延后了。因此发现taken = 0时，使用文件的修改时间。
                 */
                if (taken <= 0L) {
                    val f = File(absolutePathOfImage)
                    taken = f.lastModified()
                }
                val mime = cursor.getString(column_index_mime)
                val mimeIndex: Int = getMimeIndex(mime)
                if (mimeIndex < MIME_TYPE_JPEG) {
                    continue
                }
                val type = HLMediaType.VIDEO
                val orientation = -1
                var width = cursor.getInt(column_index_width)
                var height = cursor.getInt(column_index_height)
                val resolution = cursor.getString(column_index_video_reslution)
                if (resolution != null) {
                    val m: Matcher = PAT_RESOLUTION.matcher(resolution)
                    if (m.find()) {
                        width = m.group(1).toInt()
                        height = m.group(2).toInt()
                    }
                }
                val size = cursor.getLong(column_index_size)
                val latitude = 0.0
                val longitude = 0.0
                val duration = cursor.getLong(column_index_video_duration).toInt()
                val localMedia = RecognizeMedia(
                    id.toString(), absolutePathOfImage, type, width, height, duration.toLong(), taken
                )
                listOfAllVideos.add(localMedia)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    } finally {
        cursor?.close()
    }
    return listOfAllVideos
}

private fun queryImages(context: Context): List<RecognizeMedia> {
    val listOfAllImages: MutableList<RecognizeMedia> = ArrayList<RecognizeMedia>()
    val projection = arrayOf(
        MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_TAKEN, MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.WIDTH, MediaStore.Images.Media.HEIGHT, MediaStore.Images.Media.ORIENTATION, MediaStore.Images.Media.SIZE
    )
    val targetImportPath = ""
    try {
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null
        ).use { cursor ->
            assert(cursor != null)
            val column_index_id = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val column_index_taken = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val column_index_mime = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            val column_index_size = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val column_index_width = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val column_index_height = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val column_index_orientation = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION)
            while (cursor.moveToNext()) {
                try {
                    val id = cursor.getLong(column_index_id)
                    val absolutePathOfImage = cursor.getString(column_index_data)
                    if (TextUtils.isEmpty(absolutePathOfImage)) {
                        continue
                    }
                    if (!TextUtils.isEmpty(targetImportPath) && !absolutePathOfImage.contains(
                            targetImportPath
                        )) {
                        continue
                    }
                    var taken = cursor.getLong(column_index_taken)
                    /**
                     * media store 取的taken有可能是0，从而导致部分照片的导入顺序被延后了。因此发现taken = 0时，使用文件的修改时间。
                     */
                    if (taken <= 0L) {
                        val f = File(absolutePathOfImage)
                        taken = f.lastModified()
                    }
                    val mime = cursor.getString(column_index_mime)
                    val mimeIndex: Int = getMimeIndex(mime)
                    if (mimeIndex < MIME_TYPE_JPEG) {
                        continue
                    }
                    val type = HLMediaType.IMAGE
                    val orientation = cursor.getInt(column_index_orientation)
                    val width = cursor.getInt(column_index_width)
                    val height = cursor.getInt(column_index_height)
                    val size = cursor.getLong(column_index_size)

                    val latitude = 0.0
                    val longitude = 0.0

                    val localMedia = RecognizeMedia(
                        id.toString(), absolutePathOfImage, type, width, height, 0, taken
                    )
                    listOfAllImages.add(localMedia)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }
    return listOfAllImages
}


fun getMimeIndex(mime: String) = when (mime) {
    "" -> MIME_TYPE_UNKNOWN
    MIME_TYPE_STR_JPEG, MIME_TYPE_STR_JPG -> MIME_TYPE_JPEG
    MIME_TYPE_STR_GIF -> MIME_TYPE_GIF
    MIME_TYPE_STR_PNG -> MIME_TYPE_PNG
    MIME_TYPE_STR_WEBP -> MIME_TYPE_WEBP
    MIME_TYPE_STR_BMP, MIME_TYPE_STR_X_MS_BMP -> MIME_TYPE_BMP
    MIME_TYPE_STR_HEIF -> MIME_TYPE_HEIF
    MIME_TYPE_STR_HEIC -> MIME_TYPE_HEIF
    MIME_TYPE_STR_MP4 -> MIME_TYPE_MP4
    MIME_TYPE_STR_AVC -> MIME_TYPE_H264
    MIME_TYPE_STR_QUICKTIME -> MIME_TYPE_QUICKTIME
    MIME_TYPE_STR_3GPP -> MIME_TYPE_3GPP
    MIME_TYPE_STR_TIFF -> MIME_TYPE_TIFF
    MIME_TYPE_STR_TTC -> MIME_TYPE_TTC
    MIME_TYPE_STR_TTF -> MIME_TYPE_TTF
    MIME_TYPE_STR_OTF -> MIME_TYPE_OTF
    else -> {
        MIME_TYPE_OTHER
    }
}

const val MIME_TYPE_STR_JPEG = "image/jpeg"
const val MIME_TYPE_STR_JPG = "image/jpg"
const val MIME_TYPE_STR_GIF = "image/gif"
const val MIME_TYPE_STR_PNG = "image/png"
const val MIME_TYPE_STR_WEBP = "image/webp"
const val MIME_TYPE_STR_BMP = "image/bmp"
const val MIME_TYPE_STR_X_MS_BMP = "image/x-ms-bmp"
const val MIME_TYPE_STR_HEIF = "image/heif"
const val MIME_TYPE_STR_HEIC = "image/heic"
const val MIME_TYPE_STR_MP4 = "video/mp4"
const val MIME_TYPE_STR_AVC = "video/avc"
const val MIME_TYPE_STR_QUICKTIME = "video/quicktime"
const val MIME_TYPE_STR_3GPP = "video/3gpp"
const val MIME_TYPE_STR_TIFF = "image/tiff"
const val MIME_TYPE_STR_TTC = "font/collection"
const val MIME_TYPE_STR_TTF = "font/ttf"
const val MIME_TYPE_STR_OTF = "font/otf"

const val MIME_PREFIX_IMAGE = "image/"
const val MIME_PREFIX_VIDEO = "video/"
const val MIME_PREFIX_FONT = "font/"