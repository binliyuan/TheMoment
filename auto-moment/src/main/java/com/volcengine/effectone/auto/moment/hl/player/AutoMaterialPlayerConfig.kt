package com.volcengine.effectone.auto.moment.hl.player

import android.widget.FrameLayout
import androidx.annotation.IntRange
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.RepeatMode

data class AutoMaterialPlayerConfig(
    val videoContainer: FrameLayout,
    val mediaItems: List<MediaItem>,
    val playWhenReady:Boolean = true,
    @IntRange(from = 0)val startItemPos:Int = 0,
    val startPos:Long = 0,
    @RepeatMode val repeatMode: Int = Player.REPEAT_MODE_OFF,
) {
    companion object {
       const val HL_VIDEO_DURATION = "HL_VIDEO_DURATION"
       const val HL_VIDEO_WIDTH = "HL_VIDEO_WIDTH"
       const val HL_VIDEO_HEIGHT = "HL_VIDEO_HEIGHT"
    }
}
