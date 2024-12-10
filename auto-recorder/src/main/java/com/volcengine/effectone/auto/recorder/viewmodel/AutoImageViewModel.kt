package com.volcengine.effectone.auto.recorder.viewmodel

import android.Manifest
import android.os.Build
import android.os.Build.VERSION_CODES
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.bytedance.creativex.mediaimport.repository.api.BuiltInMaterialType
import com.bytedance.creativex.mediaimport.repository.api.IMediaItem
import com.bytedance.creativex.mediaimport.repository.internal.cursor.DEFAULT_IMAGE_QUERY_PARAM
import com.bytedance.creativex.mediaimport.repository.internal.cursor.DEFAULT_VIDEO_QUERY_PARAM
import com.bytedance.creativex.mediaimport.repository.internal.main.DefaultMaterialRepositoryFactory
import com.bytedance.creativex.mediaimport.util.getMediaFileAbsolutePath
import com.volcengine.ck.album.init.AlbumInit
import com.volcengine.effectone.singleton.AppSingleton
import com.volcengine.effectone.utils.EOUtils
import com.volcengine.effectone.viewmodel.BaseViewModel
import com.volcengine.effectone.viewmodel.EffectOneViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers

/**
 *Author: gaojin
 *Time: 2023/12/18 16:13
 */

class AutoImageViewModel(activity: FragmentActivity) : BaseViewModel(activity) {

    companion object {
        fun get(activity: FragmentActivity): AutoImageViewModel {
            return EffectOneViewModelFactory.viewModelProvider(activity).get(AutoImageViewModel::class.java)
        }
    }

    private val albumPermissions = mutableListOf<String>().apply {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.READ_MEDIA_IMAGES)
            add(Manifest.permission.READ_MEDIA_VIDEO)
            add(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private val disposable = CompositeDisposable()

    val albumButtonMaterialItem = MutableLiveData<String>()

    fun checkPermissions(successAction: () -> Unit, failedAction: (deniedList: List<String>) -> Unit) {
        activity?.let {
            EOUtils.permission.checkPermissions(it, albumPermissions, successAction, failedAction)
        }
    }

    fun queryFirstMedia(lifecycleOwner: LifecycleOwner) {
        val media = albumButtonMaterialItem.value
        if (media == null) {
            activity?.let {
                val hasPermission = EOUtils.permission.hasPermission(it, albumPermissions)
                if (hasPermission) {
                    queryInternal(lifecycleOwner)
                }
            }
        }
    }

    /**
     * 更新相册图标
     */
    fun updateAlbumIcon(path: String) {
        albumButtonMaterialItem.value = path
    }

    private fun queryInternal(lifecycleOwner: LifecycleOwner) {
        AlbumInit.init(AppSingleton.instance)
        val repo = DefaultMaterialRepositoryFactory(lifecycleOwner).apply {
            setImageQueryParamProvider {
                DEFAULT_IMAGE_QUERY_PARAM
            }
            setVideoQueryParamProvider {
                DEFAULT_VIDEO_QUERY_PARAM
            }
        }.create()
        repo.iterator(BuiltInMaterialType.ALL)
            .next(2)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { materialList ->
                materialList?.let { list ->
                    val item = list.maxByOrNull { it.date } as? IMediaItem
                    item?.let {
                        albumButtonMaterialItem.value = getMediaFileAbsolutePath(it)
                    }
                }
                repo.release()
            }.addTo(disposable)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        disposable.clear()
    }
}