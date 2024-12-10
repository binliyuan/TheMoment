package com.volcengine.effectone.auto.templates.vm

import android.app.Application
import android.view.KeyEvent
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import com.bytedance.creativex.mediaimport.preview.api.IPreviewView
import com.bytedance.creativex.mediaimport.preview.api.PreviewEntranceParam
import com.bytedance.creativex.mediaimport.preview.internal.main.PreviewView
import com.bytedance.creativex.mediaimport.repository.api.IMaterialItem
import com.bytedance.creativex.mediaimport.repository.api.MaterialCategoryType
import com.bytedance.creativex.mediaimport.view.internal.IMaterialSelectorUpdater
import com.bytedance.creativex.mediaimport.view.internal.MaterialSelectedState
import com.bytedance.creativex.mediaimport.view.internal.viewmodel.MediaSelectViewModel
import com.ss.android.ugc.cut_ui.MediaItem
import com.ss.android.ugc.tools.view.base.TransitionViewState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

/**
 *Author: gaojin
 *Time: 2024/3/22 16:50
 */
class CutSameAlbumViewModel(application: Application) : AndroidViewModel(application) {


    var mediaSelectViewModel: MediaSelectViewModel? = null
    var previewView: PreviewView? = null
    var selectorUpdater: IMaterialSelectorUpdater<IMaterialItem>? = null

    private var preClipMediaItemMap: HashMap<Int, MediaItem> = HashMap()
    private var previewViewDisposable: Disposable? = null
    private var previewViewIsShown = false
    private var previewViewEventDisposable: Disposable? = null

    fun showPreview(
        materialItem: IMaterialItem,
        processPickMediaData: List<IMaterialItem>,
        realIndex: Int,
        enterFrom: MaterialCategoryType?
    ) {
        previewView?.enter(
            PreviewEntranceParam(
                data = materialItem,
                list = processPickMediaData,
                selectionIndex = realIndex,
                enterFrom = enterFrom,
                state = MaterialSelectedState.SELECTED,
            )
        )
    }

    fun onKeyDown(keyEvent: KeyEvent): Boolean {
        previewView?.run {
            if (previewViewIsShown && keyEvent.keyCode == KeyEvent.KEYCODE_BACK) {
                exit()
                return true
            }
        }
        return false
    }
    fun observerSubscribe() {
        //previewView show / hide
        previewViewDisposable = previewView
            ?.observeVisibleState()
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe { transitionViewState ->
                when (transitionViewState) {
                    TransitionViewState.SHOWN ->
                        previewViewIsShown = true

                    TransitionViewState.PRE_HIDE ->
                        previewViewIsShown = false

                    else -> {}
                }
            }
        //previewView leftTop backBtn
        previewViewEventDisposable = previewView
            ?.observeViewEvent()
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe{viewEvent->
                if (viewEvent == IPreviewView.ViewEvent.Close) {
                   previewView?.takeIf { previewViewIsShown }?.exit()
                }
            }
    }
    override fun onCleared() {
        super.onCleared()
        previewViewDisposable?.takeIf { it.isDisposed.not() }?.dispose()
        previewViewEventDisposable?.takeIf { it.isDisposed.not() }?.dispose()
    }
    companion object {
        fun get(activity: FragmentActivity): CutSameAlbumViewModel {
            return ViewModelProvider(
                activity,
                ViewModelProvider.AndroidViewModelFactory.getInstance(activity.application)
            ).get(CutSameAlbumViewModel::class.java)
        }
    }
}



