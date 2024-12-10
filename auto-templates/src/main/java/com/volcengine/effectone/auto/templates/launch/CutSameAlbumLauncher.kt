package com.volcengine.effectone.auto.templates.launch

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.cutsame.solution.template.model.TemplateItem
import com.ss.android.ugc.cut_ui.MediaItem
import com.volcengine.ck.logkit.LogKit
import com.volcengine.effectone.auto.common.cutsame.CutSameContract
import com.volcengine.effectone.auto.templates.vm.CutSameBottomDockerViewModel

class CutSameComposeLauncher(private val activity: FragmentActivity): LifecycleObserver {
    companion object {
        private const val TAG = "CutSameComposeLauncher"
        private const val LAUNCH_COMPOSE = "launchCompose"
        private const val LAUNCH_CLIP = "launchClip"
    }

    private val cutSameBottomDockerViewModel by lazy { CutSameBottomDockerViewModel.get(activity) }

    private var mLaunchCompose: ActivityResultLauncher<Pair<ArrayList<MediaItem>, TemplateItem>>? =
        null
    private var mLaunchClip: ActivityResultLauncher<MediaItem?>? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        val registry = activity.activityResultRegistry
        registry.registerCompose()
        registry.registerClip()
    }

    fun launchCompose(composeItem: Pair<ArrayList<MediaItem>, TemplateItem>) {
        mLaunchCompose?.launch(composeItem)
    }

    fun launchClip(mediaItem: MediaItem) {
        mLaunchClip?.launch(mediaItem)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        mLaunchCompose?.unregister()
        mLaunchClip?.unregister()
        mLaunchCompose = null
        mLaunchClip = null
    }

    /**
     * 裁剪
     */
    private fun ActivityResultRegistry.registerClip() {
        mLaunchClip = register("$TAG$LAUNCH_CLIP", activity, LaunchClipContract()) { mediaItem ->
            LogKit.d(TAG, "launchClip result")
            mediaItem?.let {
                cutSameBottomDockerViewModel.updateProcessPickItem(it)
            }
        }
    }
    private fun ActivityResultRegistry.registerCompose() {
        mLaunchCompose = register("$TAG$LAUNCH_COMPOSE", activity, CutSameComposeContract()) {
                LogKit.d(TAG, "launchCompose result")
                val data = activity.intent
                data.putParcelableArrayListExtra(CutSameContract.ARG_DATA_COMPRESS_RESULT_MEDIA_ITEMS, it)
                activity.setResult(Activity.RESULT_OK, data)
                activity.finish()
            }
    }
}
