package com.volcengine.effectone.auto.album.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.bytedance.creativex.mediaimport.repository.api.IFolderItem
import com.bytedance.creativex.mediaimport.repository.api.IMaterialItem
import com.bytedance.creativex.mediaimport.repository.api.MaterialCategoryType
import com.bytedance.creativex.mediaimport.view.api.IMediaSelectFolderListView
import com.bytedance.creativex.mediaimport.view.api.IMediaSelectListView
import com.bytedance.creativex.mediaimport.view.internal.IMaterialSelectionListView
import com.bytedance.creativex.mediaimport.view.internal.IMaterialSelectorView
import com.bytedance.creativex.mediaimport.view.internal.IMediaSelectFolderEntranceView
import com.bytedance.creativex.mediaimport.view.internal.IMediaSelectTitleView
import com.bytedance.creativex.mediaimport.view.internal.IMediaSelectViewModel
import com.volcengine.ck.album.AlbumMaterialSelectView
import com.volcengine.ck.album.R.id
import com.volcengine.ck.album.base.AlbumConfig
import com.volcengine.effectone.auto.album.R

/**
 *Author: gaojin
 *Time: 2024/4/25 19:17
 */

class AutoAlbumMaterialSelectView(
    root: ViewGroup,
    private val albumConfig: AlbumConfig,
    private val lifecycleOwner: LifecycleOwner,
    private val viewModel: IMediaSelectViewModel<IMaterialItem, IFolderItem>? = null,
    viewConfigureBuilder: ((ViewConfigure) -> Unit)? = null,
) : AlbumMaterialSelectView(root, albumConfig, lifecycleOwner, viewModel, viewConfigureBuilder) {
    /**
     * 根布局，换成自己的，方便自定义
     */
    override fun provideContentView(root: ViewGroup): ViewGroup {
        return LayoutInflater.from(root.context)
            .inflate(R.layout.auto_album_import_root_view, root, true) as ViewGroup
    }

    /**
     * 顶部TitleView 修改返回键样式
     */
    override fun provideTitleView(content: ViewGroup): IMediaSelectTitleView {
        return AutoAlbumSelectTitleView(
            content.findViewById(id.title_content),
            viewConfigure.titleViewConfigure.showCloseView
        ).also { it.init() }
    }

    /**
     * 相册头部文件夹的样式
     */
    override fun provideFolderEntranceView(content: ViewGroup): IMediaSelectFolderEntranceView {
        val entranceRoot = content.findViewById<ViewGroup>(R.id.title_layout)
        val folderListContentView = content.findViewById<ViewGroup>(R.id.folder_list_content)
        return AutoAlbumFolderEntranceView(
            entranceRoot,
            folderListContentView,
            enableFolderList = viewConfigure.enableFolderListView,
            viewConfigureBuilder = viewConfigure.folderViewConfigure.entranceViewConfigureBuilder
        ).also {
            it.init()
        }
    }

    /**
     * 相册顶部文件夹下拉列表
     */
    override fun provideFolderListView(content: ViewGroup): IMediaSelectFolderListView<IFolderItem> {
        val folderListRoot = content.findViewById<ViewGroup>(R.id.folder_list_content)
        return AutoAlbumSelectFolderListView(
            context = content.context,
            lifecycle = lifecycleOwner,
            dataViewModel = viewModel?.mediaFolderViewModel,
            root = folderListRoot,
            attachToRoot = true,
            viewConfigureBuilder = viewConfigure.folderViewConfigure.listViewConfigureBuilder
        ).also {
            it.init()
        }
    }

    override fun providePageListView(
        content: ViewGroup,
        categoryType: MaterialCategoryType
    ): IMediaSelectListView<IMaterialItem> {
        val listViewConfigureBuilder = viewConfigure.contentListConfigureBuilders?.invoke(categoryType)
        return AutoAlbumDuplicatedListView(
            context = content.context,
            lifecycle = lifecycleOwner,
            category = categoryType,
            selectorViewModel = viewModel?.mediaSelectorViewModel,
            dataViewModel = viewModel?.mediaSelectListViewModel(categoryType),
            pagerViewModel = viewModel?.mediaPagerViewModel,
            listViewConfigureBuilder = listViewConfigureBuilder,
            albumConfig = albumConfig
        ).also {
            it.init()
        }
    }

    //Docker栏
    override fun provideSelectorView(content: ViewGroup): IMaterialSelectorView<IMaterialItem> {
        return AutoAlbumMediaSelectorBottomView(
            root = content.findViewById(R.id.auto_selector_view_content),
            lifecycleOwner = lifecycleOwner,
            albumConfig = albumConfig,
            selectorViewModel = viewModel?.mediaSelectorViewModel,
            enableIndexedConfirm = false,
            confirmText = content.context.getString(com.volcengine.ck.album.R.string.eo_album_confirm),
        ).also {
            it.init()
        }
    }

    /**
     * Docker栏的列表View
     */
    override fun provideSelectionListView(content: ViewGroup): IMaterialSelectionListView<IMaterialItem> {
        return AutoAlbumMaterialSelectionListView(
            context = content.context,
            lifecycle = lifecycleOwner,
            selectorViewModel = viewModel?.mediaSelectorViewModel,
            root = content.findViewById(id.selection_list_view_container),
            viewConfigureBuilder = viewConfigure.selectorViewConfigure.listViewConfigureBuilder
        ).also {
            it.init()
        }
    }
}