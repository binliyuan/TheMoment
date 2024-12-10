package com.volcengine.effectone.auto.album.view

import androidx.lifecycle.LifecycleOwner
import android.content.Context
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.creativex.mediaimport.repository.api.IFolderItem
import com.bytedance.creativex.mediaimport.view.internal.IMediaFolderListViewModel
import com.bytedance.creativex.mediaimport.view.internal.folder.MediaSelectFolderListView
import com.volcengine.ck.album.holder.AlbumFolderItemViewHolder
import com.volcengine.effectone.auto.album.R

/**
 *Author: gaojin
 *Time: 2021/11/18 6:35 PM
 */
class AutoAlbumSelectFolderListView(
    context: Context,
    lifecycle: LifecycleOwner,
    dataViewModel: IMediaFolderListViewModel<IFolderItem>?,
    root: ViewGroup? = null,
    private val attachToRoot: Boolean = false,
    viewConfigureBuilder: ((ViewConfigure) -> Unit)? = null
) : MediaSelectFolderListView(context, lifecycle, dataViewModel, root, attachToRoot, viewConfigureBuilder) {

    override fun createFolderViewHolder(parent: ViewGroup, viewType: Int, folderClickListener: (IFolderItem, Int) -> Unit): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.auto_album_import_folder_item_view, parent, false)
        return AlbumFolderItemViewHolder(itemView, folderClickListener)
    }

    override fun bindFolderViewHolder(holder: ViewHolder, position: Int, data: IFolderItem, isPartLoad: Boolean) {
        (holder as? AlbumFolderItemViewHolder)?.bind(data, position, isPartLoad)
    }

    override fun provideRecyclerView(content: View): RecyclerView {
        return super.provideRecyclerView(content).apply {
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                ContextCompat.getDrawable(context, R.drawable.auto_album_folder_list_divider_layer)?.let {
                    setDrawable(it)
                }
            })
        }
    }

    override fun provideContentView(root: ViewGroup?): View {
        return LayoutInflater.from(context).inflate(R.layout.auto_media_import_folder_list_view, root, attachToRoot)
    }
}