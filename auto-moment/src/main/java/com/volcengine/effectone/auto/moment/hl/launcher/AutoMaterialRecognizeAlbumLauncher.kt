package com.volcengine.effectone.auto.moment.hl.launcher

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.volcengine.ck.album.AlbumEntrance
import com.volcengine.ck.album.base.AlbumConfig
import com.volcengine.effectone.EffectOneSdk
import com.volcengine.effectone.auto.album.AutoAlbumEntrance
import com.volcengine.effectone.auto.moment.hl.data.AutoMaterialRecognizeMedia
import com.volcengine.effectone.auto.moment.hl.vm.AutoMaterialRecognizeViewModel
import com.volcengine.effectone.auto.moment.utils.toRecognizeMedia
import com.volcengine.effectone.utils.EOUtils

class AutoMaterialRecognizeAlbumLauncher(private val activity: FragmentActivity) : LifecycleObserver {

    private val autoMaterialRecognizeViewModel by lazy { AutoMaterialRecognizeViewModel.get(activity) }
    private val launchAlbumContract by lazy { AutoMaterialRecognizeAlbumContract(activity) }

    private var launchAlbumLauncher: ActivityResultLauncher<AlbumConfig>? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        val resultRegistry = activity.activityResultRegistry
        resultRegistry.registerLaunchAlbum()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        launchAlbumLauncher?.unregister()
        launchAlbumLauncher = null
    }

    private fun ActivityResultRegistry.registerLaunchAlbum() {
        launchAlbumLauncher = this.register("registerLaunchAlbum", launchAlbumContract) {
            it?.let { it ->
                autoMaterialRecognizeViewModel.concatRecognizeMedias(it.map {  AutoMaterialRecognizeMedia(it.toRecognizeMedia()) })
            }
        }
    }

    fun launchAlbum() {
        EOUtils.permission.checkPermissions(activity, AutoAlbumEntrance.albumPermissions, {
            val albumConfig = AlbumConfig(
                allEnable = true,
                imageEnable = true,
                videoEnable = true,
                maxSelectCount = EffectOneSdk.albumMaxSelectedCount,
                finishClazz = StartMaterialRecognizeLauchAlbumFinish::class.java
            )
            launchAlbumLauncher?.launch(albumConfig)
        }, {
            AlbumEntrance.showAlbumPermissionTips(activity)
        })
    }

    companion object{
        const val ARGUMENT_KEY_MEDIA_LIST = "argument_key_media_list"

    }
}