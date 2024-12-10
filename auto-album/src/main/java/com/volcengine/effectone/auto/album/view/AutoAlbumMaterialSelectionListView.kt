package com.volcengine.effectone.auto.album.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.creativex.mediaimport.repository.api.IMaterialItem
import com.bytedance.creativex.mediaimport.view.internal.IMaterialSelectorViewModel
import com.bytedance.creativex.mediaimport.view.internal.MaterialSelectedState
import com.bytedance.creativex.mediaimport.view.internal.base.BaseMaterialSelectionListView
import com.bytedance.creativex.mediaimport.view.internal.base.BaseSelectorViewHolder
import com.bytedance.creativex.mediaimport.view.internal.viewholder.SelectionListViewHolder
import com.volcengine.ck.album.holder.AlbumBottomSelectionListViewHolder
import com.volcengine.effectone.auto.album.R

/**
 * @author sunjingkai
 * @since 1/14/21
 * @lastModified by sunjingkai on 1/14/21
 */

class AutoAlbumMaterialSelectionListView(
    context: Context,
    lifecycle: LifecycleOwner,
    private val selectorViewModel: IMaterialSelectorViewModel<IMaterialItem>?,
    root: ViewGroup? = null,
    attachToRoot: Boolean = false,
    viewConfigureBuilder: ((ViewConfigure) -> Unit)? = null
) : BaseMaterialSelectionListView<IMaterialItem>(
    context, lifecycle, selectorViewModel, root, attachToRoot, viewConfigureBuilder
) {

    override fun init() {
        super.init()
        selectorViewModel?.selectedMaterials?.observe(lifecycle) {
            if (it.isEmpty()) {
                contentView.visibility = View.GONE
            } else {
                contentView.visibility = View.VISIBLE
            }
        }
    }

    override fun createSelectorViewHolder(
        parent: ViewGroup,
        viewType: Int,
        contentClickListener: (data: IMaterialItem, position: Int, state: MaterialSelectedState) -> Unit,
        cancelClickListener: (data: IMaterialItem, position: Int, state: MaterialSelectedState) -> Unit
    ): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.auto_album_import_preview_selection_item_view, parent, false)
        val cancelView = itemView.findViewById<View>(R.id.delete_layout)
        return AlbumBottomSelectionListViewHolder(itemView, cancelView, contentClickListener, cancelClickListener)
    }

    override fun bindSelectorViewHolder(holder: RecyclerView.ViewHolder, position: Int, data: IMaterialItem, state: BaseSelectorViewHolder.State) {
        (holder as? SelectionListViewHolder)?.bind(data, position, state)
    }

    override fun provideRecyclerView(content: View): RecyclerView {
        return content.findViewById<RecyclerView>(R.id.auto_bottom_docker_media_list).apply {
        }
    }

    override fun provideContentView(root: ViewGroup?): View {
        return LayoutInflater.from(context).inflate(R.layout.auto_album_import_selection_list_view, root, true)
    }
}