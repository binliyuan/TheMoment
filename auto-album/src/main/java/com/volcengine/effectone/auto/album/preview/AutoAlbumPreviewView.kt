package com.volcengine.effectone.auto.album.preview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager.widget.ViewPager
import com.bytedance.creativex.mediaimport.preview.api.IPreviewValidator
import com.bytedance.creativex.mediaimport.preview.internal.IPreviewImagePageView
import com.bytedance.creativex.mediaimport.preview.internal.IPreviewMainPagerView
import com.bytedance.creativex.mediaimport.preview.internal.IPreviewPageView
import com.bytedance.creativex.mediaimport.preview.internal.IPreviewSelectorView
import com.bytedance.creativex.mediaimport.preview.internal.IPreviewVideoPageView
import com.bytedance.creativex.mediaimport.preview.internal.IPreviewViewModel
import com.bytedance.creativex.mediaimport.preview.internal.main.PreviewView
import com.bytedance.creativex.mediaimport.repository.api.IMaterialItem
import com.bytedance.creativex.mediaimport.view.internal.IMaterialSelectorUpdater
import com.bytedance.creativex.visibleOrGone
import com.volcengine.ck.album.base.AlbumConfig
import com.volcengine.ck.album.view.preview.AlbumPreviewImagePageView
import com.volcengine.ck.album.view.preview.AlbumPreviewMainPagerView
import com.volcengine.effectone.auto.album.R

/**
 *Author: gaojin
 *Time: 2024/6/6 11:22
 */

open class AutoAlbumPreviewView(
    private val root: ViewGroup,
    private val lifecycle: LifecycleOwner,
    private val viewModel: IPreviewViewModel<IMaterialItem>? = null,
    private val albumConfig: AlbumConfig,
    viewConfigureBuilder: ((ViewConfigure) -> Unit)? = null
) : PreviewView(root, lifecycle, viewModel, viewConfigureBuilder) {

    private var previewTopLayout: View? = null

    /**
     * 根布局，换成自己的，方便自定义
     */
    override fun provideContentView(root: ViewGroup): ViewGroup {
        val rootView = LayoutInflater.from(root.context).inflate(R.layout.auto_album_import_preview_root_view, root, false) as ViewGroup
        previewTopLayout = rootView.findViewById(R.id.btn_back)
        return rootView
    }

    /**
     * 预览界面底部UI,当下会隐藏掉
     */
    override fun provideSelectorView(
        lifecycle: LifecycleOwner,
        content: ViewGroup,
        selectorUpdater: IMaterialSelectorUpdater<IMaterialItem>?
    ): IPreviewSelectorView<IMaterialItem> {
        val selectorConfigure = viewConfigure.selectorViewConfigure
        return AutoAlbumPreviewSelectorBottomView(
            root = content.findViewById(R.id.auto_selector_view_content),
            lifecycleOwner = lifecycle,
            selectorViewModel = viewModel?.mediaSelectorViewModel,
            selectorUpdater = selectorUpdater,
            previewPagerViewModel = viewModel?.mediaPreviewPagerViewModel,
            enableIndexedSelect = selectorConfigure.enableIndexedSelect,
            enableIndexedConfirm = selectorConfigure.actualEnableIndexedConfirm
        ) {
            it.enableDuplicatedSelect = selectorConfigure.enableDuplicatedSelect
            selectorConfigure.viewConfigureBuilder?.invoke(it)
            it.concatConfirmTextWithCount = { res, _ ->
                res
            }
            it.enableDirectlyConfirm = true
        }.also {
            it.init()
        }
    }

    override fun provideVideoPageView(
        content: ViewGroup, data: IMaterialItem,
        enterInterceptors: MutableList<IPreviewValidator<IMaterialItem>>,
        transViewProvider: ((isEnterAnim: Boolean, data: IMaterialItem?) -> Pair<View?, Boolean>)?
    ): IPreviewVideoPageView<IMaterialItem> {
        return AutoAlbumPreviewVideoPageView(
            context = content.context,
            lifecycle = lifecycle,
            pagerViewModel = viewModel?.mediaPreviewPagerViewModel,
            validators = enterInterceptors,
            transViewProvider = transViewProvider,
            viewConfigureBuilder = viewConfigure.pagerViewConfigure.videoPageViewConfigureBuilder
        ) { provideVideoPlayer() }
            .also {
                it.init()
                it.showPreview(data)
            }
    }

    override fun provideImagePageView(
        content: ViewGroup,
        data: IMaterialItem,
        enterInterceptors: MutableList<IPreviewValidator<IMaterialItem>>,
        transViewProvider: ((isEnterAnim: Boolean, data: IMaterialItem?) -> Pair<View?, Boolean>)?
    ): IPreviewImagePageView<IMaterialItem> {
        return AlbumPreviewImagePageView(
            context = content.context, lifecycle = lifecycle,
            pagerViewModel = viewModel?.mediaPreviewPagerViewModel,
            validators = enterInterceptors,
            transViewProvider = transViewProvider,
            viewConfigureBuilder = viewConfigure.pagerViewConfigure.imagePageViewConfigureBuilder
        ).also {
            it.init()
            it.showPreview(data)
        }
    }

    override fun onPreviewPageAlphaChange(percent: Float) {
        super.onPreviewPageAlphaChange(percent)
        previewTopLayout?.visibleOrGone = percent == 1.0f
    }

    override fun onPreviewPageScaleBegin() {
        super.onPreviewPageScaleBegin()
        previewTopLayout?.visibleOrGone = false
    }

    override fun onPreviewPageScaleEnd() {
        super.onPreviewPageScaleEnd()
        previewTopLayout?.visibleOrGone = true
    }

    override fun onPreviewPageSingleClick() {
        //do nothing
    }

    override fun provideMainPagerView(lifecycle: LifecycleOwner, content: ViewGroup, pageViewConfigure: (IPreviewPageView<IMaterialItem>) -> Unit): IPreviewMainPagerView<IMaterialItem> {
        val viewPager = content.findViewById<ViewPager>(R.id.auto_preview_view_pager)
        return AlbumPreviewMainPagerView(innerViewPager = viewPager, pageViewProvider = providePagerViewProvider(content, pageViewConfigure))
    }
}