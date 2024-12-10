package com.volcengine.effectone.auto.album.preview

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import com.bytedance.creativex.mediaimport.preview.api.IPreviewValidator
import com.bytedance.creativex.mediaimport.preview.internal.IPreviewPagerViewModel
import com.bytedance.creativex.mediaimport.preview.internal.IVideoPlayer
import com.bytedance.creativex.mediaimport.preview.internal.base.BasePreviewVideoPageView
import com.bytedance.creativex.mediaimport.repository.api.IMaterialItem
import com.bytedance.creativex.mediaimport.view.internal.validator.IMaterialPropertyGetter
import com.bytedance.creativex.visibleOrGone
import com.ss.android.ugc.tools.utils.UIUtils
import com.volcengine.effectone.EffectOneSdk
import com.volcengine.effectone.auto.album.R
import com.volcengine.effectone.image.ImageOption


open class AutoAlbumPreviewVideoPageView(
    context: Context,
    lifecycle: LifecycleOwner,
    pagerViewModel: IPreviewPagerViewModel<IMaterialItem>? = null,
    root: ViewGroup? = null,
    attachToRoot: Boolean = false,
    validators: List<IPreviewValidator<IMaterialItem>> = emptyList(),
    transViewProvider: ((Boolean, data: IMaterialItem?) -> Pair<View?, Boolean>)? = null,
    viewConfigureBuilder: ((ViewConfigure) -> Unit)? = null,
    videoPlayerProvider: () -> IVideoPlayer
) : BasePreviewVideoPageView<IMaterialItem, ImageView>(
    context, lifecycle, pagerViewModel, root, attachToRoot, validators,
    transViewProvider, viewConfigureBuilder, videoPlayerProvider
), IMaterialPropertyGetter<IMaterialItem> by IMaterialPropertyGetter {

    private lateinit var playPauseIcon: View

    override fun showThumbnailActual(uri: Uri) {
        videoThumbnailView?.also {
            if (!it.visibleOrGone) {
                it.visibleOrGone = true
                EffectOneSdk.imageLoader.loadImageView(
                    it,
                    uri,
                    ImageOption.Builder()
                        .format(Bitmap.Config.RGB_565)
                        .scaleType(ImageView.ScaleType.CENTER_CROP)
                        .build()
                )
            }
        }
    }

    override fun init() {
        super.init()
        playPauseIcon = contentView.findViewById(R.id.video_play_pause_view)
        textureView.setOnClickListener {
            videoPlayer?.run {
                if (isPlaying()) {
                    playPauseIcon.visibility = View.VISIBLE
                    pauseVideoActual()
                } else {
                    playPauseIcon.visibility = View.GONE
                    playVideoActual()
                }
            }
        }
    }

    override fun provideContentView(root: ViewGroup?): ViewGroup {
        return (LayoutInflater.from(context).inflate(R.layout.auto_album_import_preview_video_root_view, root, false) as ViewGroup)
    }

    override fun updateRenderSize(videoWidth: Int, videoHeight: Int) {
        if (videoWidth <= 0 || videoHeight <= 0) return
        val resultWidth: Int
        val resultHeight: Int

        val playViewHeight = textureView.height
        val playViewWidth = textureView.width

        var topMargin = 0
        var startMargin = 0

        val playViewRatio = playViewHeight / playViewWidth
        val videoRatio = videoHeight / videoWidth

        if (playViewRatio > videoRatio) {
            //宽度占满播放区域
            resultWidth = playViewWidth
            resultHeight = resultWidth * videoHeight / videoWidth
            topMargin += (playViewHeight - resultHeight) / 2
        } else {
            //高度占满播放区域
            resultHeight = playViewHeight
            resultWidth = resultHeight * videoWidth / videoHeight
            startMargin += (playViewWidth - resultWidth) / 2
        }

        val textureViewParams = textureView.layoutParams as? MarginLayoutParams
        if (textureViewParams != null && (textureViewParams.width != resultWidth || textureViewParams.height != resultHeight)) {
            textureViewParams.width = resultWidth
            textureViewParams.height = resultHeight
            textureViewParams.topMargin = topMargin
            textureViewParams.leftMargin = startMargin
            textureViewParams.marginStart = startMargin
            textureViewParams.bottomMargin = 0
            textureView.layoutParams = textureViewParams
        }
        videoThumbnailView?.let {
            val params = it.layoutParams as? MarginLayoutParams
            if (params != null && (params.width != resultWidth || params.height != resultHeight)) {
                params.width = resultWidth
                params.height = resultHeight
                params.topMargin = topMargin
                params.leftMargin = startMargin
                params.marginStart = startMargin
                params.bottomMargin = 0
                textureView.layoutParams = params
            }
        }
    }
}