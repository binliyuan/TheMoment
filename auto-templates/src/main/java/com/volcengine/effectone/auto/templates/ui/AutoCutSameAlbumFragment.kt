package com.volcengine.effectone.auto.templates.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.bytedance.creativex.mediaimport.preview.internal.IPreviewViewModel
import com.bytedance.creativex.mediaimport.preview.internal.main.PreviewView
import com.bytedance.creativex.mediaimport.repository.api.IMaterialItem
import com.volcengine.ck.album.base.AlbumConfig
import com.volcengine.effectone.auto.album.AutoAlbumFragment
import com.volcengine.effectone.auto.album.preview.AutoAlbumPreviewView
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.vm.CutSameAlbumViewModel

/**
 *Author: gaojin
 *Time: 2024/3/22 15:39
 */

class AutoCutSameAlbumFragment : AutoAlbumFragment() {

    companion object {
        private const val ITEM_COLUMN_SPACING = 5F
        private const val LIST_SPAN_COUNT = 4
    }

    private val cutSameViewModel by lazy {
        CutSameAlbumViewModel.get(requireActivity())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cutSameViewModel.mediaSelectViewModel = mediaSelectViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = super.onCreateView(inflater, container, savedInstanceState)
        cutSameViewModel.previewView = previewView
        return rootView
    }

    override fun getPreviewContainer(rootView: View): ViewGroup {
        return requireActivity().findViewById(R.id.auto_cutsame_album_preview_root)
    }
    /**
     * 替换预览View的样式
     */
    override fun getAlbumPreview(
        rootView: View,
        lifecycleOwner: LifecycleOwner,
        previewViewModel: IPreviewViewModel<IMaterialItem>,
        albumConfig: AlbumConfig
    ): PreviewView {
        return AutoAlbumPreviewView(
            root = getPreviewContainer(rootView),
            lifecycle = lifecycleOwner,
            viewModel = previewViewModel,
            albumConfig = albumConfig,
            viewConfigureBuilder = { config ->
                config.enableTransition = false
                config.enableDocker = true
                config.selectorViewConfigure.viewConfigureBuilder = {
                    it.preCheckSelectorValid = true
                }
                config.pagerViewConfigure.imagePageViewConfigureBuilder = {
                    it.needResize = true
                    it.enableScale = false
                }
                config.pagerViewConfigure.videoPageViewConfigureBuilder = {
                    it.enableScale = false
                }
            }
        )
    }
}