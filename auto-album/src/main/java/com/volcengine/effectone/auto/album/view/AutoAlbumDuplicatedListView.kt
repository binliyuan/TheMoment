package com.volcengine.effectone.auto.album.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bytedance.creativex.mediaimport.repository.api.IMaterialItem
import com.bytedance.creativex.mediaimport.repository.api.MaterialCategory
import com.bytedance.creativex.mediaimport.repository.api.MaterialCategoryType
import com.bytedance.creativex.mediaimport.view.internal.IMaterialSelectorViewModel
import com.bytedance.creativex.mediaimport.view.internal.IMediaSelectListViewModel
import com.bytedance.creativex.mediaimport.view.internal.IMediaSelectPagerViewModel
import com.bytedance.creativex.mediaimport.view.internal.MaterialSelectedState
import com.volcengine.ck.album.base.AlbumConfig
import com.volcengine.ck.album.holder.AlbumSelectViewHolder
import com.volcengine.ck.album.view.AlbumDuplicatedListView
import com.volcengine.effectone.auto.album.R
import com.volcengine.effectone.utils.SizeUtil
import com.volcengine.effectone.widget.EORoundCornerViewOutlineProvider

/**
 *Author: gaojin
 *Time: 2024/4/25 21:31
 */

open class AutoAlbumDuplicatedListView(
    context: Context,
    lifecycle: LifecycleOwner,
    category: MaterialCategoryType,
    selectorViewModel: IMaterialSelectorViewModel<IMaterialItem>?,
    dataViewModel: IMediaSelectListViewModel<IMaterialItem>?,
    pagerViewModel: IMediaSelectPagerViewModel<MaterialCategory>? = null,
    root: ViewGroup? = null,
    attachToRoot: Boolean = false,
    listViewConfigureBuilder: ((ViewConfigure<IMaterialItem>) -> Unit)? = null,
    private val albumConfig: AlbumConfig,
) : AlbumDuplicatedListView(
    context, lifecycle, category,
    selectorViewModel, dataViewModel, pagerViewModel,
    root, attachToRoot, listViewConfigureBuilder, albumConfig
) {
    override fun createMediaViewHolder(
        parent: ViewGroup,
        viewType: Int,
        contentClickListener: (data: IMaterialItem, position: Int, state: MaterialSelectedState) -> Unit,
        selectorClickListener: (data: IMaterialItem, position: Int, state: MaterialSelectedState) -> Unit
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.auto_album_import_item_view, parent, false)
        itemView.apply {
            clipToOutline = true
            outlineProvider = EORoundCornerViewOutlineProvider(SizeUtil.dp2px(6F))
        }
        val indicatorView = itemView.findViewById<View>(R.id.fl_image_select_indicator)
        return AlbumSelectViewHolder(itemView, indicatorView, contentClickListener, selectorClickListener, albumConfig)
    }

}