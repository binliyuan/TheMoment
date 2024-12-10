package com.volcengine.effectone.auto.moment.hl.vm

import android.os.Bundle
import android.util.Size
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.google.android.exoplayer2.MediaItem
import com.volcengine.effectone.auto.moment.hl.config.HLExtConfig
import com.volcengine.effectone.auto.moment.hl.player.AutoMaterialPlayerConfig

class AutoMaterialHighLightPlayVM : ViewModel() {
    val releasePlayerAction = MutableLiveData<Unit>()
    var highLightStartTime = -1
    var highLightEndTime = -1
    var highLightCategoryList = emptyList<String>()
    val mediaItems = mutableListOf<MediaItem>()
    var videoDuration = -1L
    var videoWith = -1
    var videoHeight = -1
    private var videoPath = ""
    val highLightPlayAction = MutableLiveData<Unit>()

    fun parseArguments(arguments: Bundle): Boolean? {
        arguments.run {
            getString(HLExtConfig.VIDEO_PATH)?.takeIf {
                it.isEmpty().not()
            }?.also {
                videoPath = it
                mediaItems.add(MediaItem.fromUri(it))
            } ?: return null
            getInt(HLExtConfig.HL_START_TIME, -1).takeIf { it >= 0 }?.also {
                highLightStartTime = it
            } ?: return null
            getInt(HLExtConfig.HL_END_TIME, -1).takeIf { it > highLightStartTime }?.also {
                highLightEndTime = it
            } ?: return null
            getStringArrayList(HLExtConfig.HL_CATEGORY_LIST)?.also {
                highLightCategoryList = it
            }
            getLong(AutoMaterialPlayerConfig.HL_VIDEO_DURATION,-1L).takeIf { it >= highLightEndTime  }?.also {
                videoDuration = it
            }
            getInt(AutoMaterialPlayerConfig.HL_VIDEO_WIDTH,-1).takeIf { it > 0  }?.also {
                videoWith = it
            }
            getInt(AutoMaterialPlayerConfig.HL_VIDEO_HEIGHT,-1).takeIf { it > 0  }?.also {
                videoHeight = it
            }
        }
        return true
    }

    fun updateVideoSize(videoSie: Size) {
        videoWith.takeIf { it == -1 && videoSie.width > 0 }?.let { videoWith = videoSie.width }
        videoHeight.takeIf { it == -1 && videoSie.height > 0 }
            ?.let { videoHeight = videoSie.height }
    }
    fun updateVideoDuration(duration :Long){
        videoDuration.takeIf { it == -1L && duration > 0 }?.let {
            videoDuration = duration
        }
    }

    companion object {
        private const val TAG = "AutoMaterialHighLightPlayVM"
        fun get(owner: ViewModelStoreOwner): AutoMaterialHighLightPlayVM {
            return ViewModelProvider(owner).get(AutoMaterialHighLightPlayVM::class.java)
        }
    }
}