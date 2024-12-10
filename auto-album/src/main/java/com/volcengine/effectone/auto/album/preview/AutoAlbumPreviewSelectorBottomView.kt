package com.volcengine.effectone.auto.album.preview

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.bytedance.creativex.mediaimport.preview.internal.IPreviewPagerViewModel
import com.bytedance.creativex.mediaimport.preview.internal.base.BasePreviewSelectorView
import com.bytedance.creativex.mediaimport.repository.api.IMaterialItem
import com.bytedance.creativex.mediaimport.view.internal.IMaterialSelectorUpdater
import com.bytedance.creativex.mediaimport.view.internal.IMaterialSelectorViewModel
import com.bytedance.creativex.mediaimport.view.internal.MaterialSelectedState
import com.bytedance.creativex.mediaimport.view.internal.SelectorEvent
import com.bytedance.creativex.visibleOrGone
import com.volcengine.ck.album.R
import com.volcengine.effectone.singleton.AppSingleton
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

open class AutoAlbumPreviewSelectorBottomView(
    private val root: ViewGroup,
    lifecycleOwner: LifecycleOwner,
    selectorViewModel: IMaterialSelectorViewModel<IMaterialItem>?,
    private val selectorUpdater: IMaterialSelectorUpdater<IMaterialItem>?,
    previewPagerViewModel: IPreviewPagerViewModel<IMaterialItem>? = null,
    enableIndexedSelect: Boolean = true,
    enableIndexedConfirm: Boolean = true,
    viewConfigureBuilder: ((ViewConfigure) -> Unit)? = null
) : BasePreviewSelectorView<IMaterialItem>(
    root,
    lifecycleOwner,
    selectorViewModel,
    selectorUpdater,
    previewPagerViewModel,
    enableIndexedSelect,
    enableIndexedConfirm,
    AppSingleton.instance.getString(R.string.eo_album_confirm),
    viewConfigureBuilder
) {

    private val selectorEventSubject = BehaviorSubject.create<SelectorEvent>()

    override fun provideContentView(root: ViewGroup): ViewGroup {
        return LayoutInflater.from(root.context).inflate(R.layout.album_import_preview_selector_view, root, true) as ViewGroup
    }

    override fun handlePreCheckResult(valid: Boolean, selectionIndex: Int, state: MaterialSelectedState, currentItem: IMaterialItem) {
        //do nothing
    }

    override fun init() {
        contentView = provideContentView(root)
        confirmView = provideConfirmView(root)
        confirmTextView = provideConfirmTextView(root)
    }

    override fun observeSelectorEvent(): Observable<SelectorEvent> {
        return selectorEventSubject.hide()
    }

    override fun provideConfirmTextView(content: ViewGroup): TextView? {
        return contentView.findViewById(R.id.sure_view)
    }

    override fun updateTipText(state: MaterialSelectedState) {
        //do nothing
    }

    override fun updateIndexText(state: MaterialSelectedState, selectionIndex: Int) {
        //do nothing
    }

    override fun updateIndexBackground(state: MaterialSelectedState) {
        //do nothing
    }

    override var visible: Boolean
        get() = false
        set(value) {
            contentView.visibleOrGone = false
        }
}