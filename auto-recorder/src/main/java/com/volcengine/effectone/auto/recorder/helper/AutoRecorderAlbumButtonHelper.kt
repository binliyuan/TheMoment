package com.volcengine.effectone.auto.recorder.helper

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.volcengine.ck.album.AlbumEntrance
import com.volcengine.ck.album.base.AlbumConfig
import com.volcengine.effectone.EffectOneSdk
import com.volcengine.effectone.auto.album.AutoAlbumEntrance
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.recorder.R
import com.volcengine.effectone.auto.recorder.base.EO_RECORD_MODEL_HIGH_LIGHT
import com.volcengine.effectone.auto.recorder.viewmodel.AutoImageViewModel
import com.volcengine.effectone.auto.recorder.viewmodel.AutoRecordUIViewModel
import com.volcengine.effectone.image.ImageOption
import com.volcengine.effectone.recorderui.base.EO_RECORD_MODEL_30S
import com.volcengine.effectone.recorderui.base.EO_RECORD_MODEL_PHOTO

/**
 *Author: gaojin
 *Time: 2023/12/18 17:12
 */

class AutoRecorderAlbumButtonHelper(
    override val activity: FragmentActivity, override val owner: LifecycleOwner
) : IUIHelper {

    private val firstImageViewModel by lazy { AutoImageViewModel.get(activity) }
    private val recordUIViewModel by lazy { AutoRecordUIViewModel.get(activity) }

    private var albumImage: ImageView? = null
    private var albumContainer: View? = null

    override fun initView(rootView: ViewGroup) {

        albumContainer = rootView.findViewById(R.id.auto_recorder_album_container)

        albumContainer?.setOnClickListener {
            firstImageViewModel.checkPermissions({
                val autoRecordAlbumConfig = AlbumConfig(
                    allEnable = true, imageEnable = true, videoEnable = true, enableAddToDocker = false
                )
                AutoAlbumEntrance.startChooseMedia(
                    activity, 1001, autoRecordAlbumConfig
                )
            }, {
                AlbumEntrance.showAlbumPermissionTips(activity)
            })
        }

        recordUIViewModel.recordModelLiveData.observe(owner) {
            it?.let { modelItem ->
                when (modelItem.id) {
                    EO_RECORD_MODEL_PHOTO -> {
                        albumContainer?.visibility = View.VISIBLE
                    }

                    EO_RECORD_MODEL_30S -> {
                        albumContainer?.visibility = View.VISIBLE
                    }

                    EO_RECORD_MODEL_HIGH_LIGHT -> {
                        albumContainer?.visibility = View.INVISIBLE
                    }
                }
            }
        }

        albumImage = rootView.findViewById(R.id.auto_recorder_album_icon)

        firstImageViewModel.albumButtonMaterialItem.observe(owner) {
            it?.let { path ->
                albumImage?.let { view ->
                    EffectOneSdk.imageLoader.loadImageView(
                        view, path, ImageOption.Builder().width(view.measuredWidth).height(view.measuredHeight).build()
                    )
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        firstImageViewModel.queryFirstMedia(owner)
    }
}