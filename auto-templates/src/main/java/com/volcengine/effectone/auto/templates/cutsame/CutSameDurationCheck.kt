package com.volcengine.effectone.auto.templates.cutsame

import androidx.fragment.app.FragmentActivity
import com.bytedance.creativex.mediaimport.repository.api.IMaterialItem
import com.bytedance.creativex.mediaimport.repository.api.isVideo
import com.bytedance.creativex.mediaimport.view.internal.IPostSelectValidator
import com.bytedance.creativex.mediaimport.view.internal.IPostSelectValidator.Result
import com.bytedance.creativex.mediaimport.view.internal.IPreSelectValidator
import com.volcengine.ck.album.R
import com.volcengine.effectone.auto.templates.bean.CutSameMediaItem

import com.volcengine.effectone.auto.templates.vm.CutSameBottomDockerViewModel
import com.volcengine.effectone.singleton.AppSingleton
import com.volcengine.effectone.widget.EOToaster
import java.util.Locale

/**
 *Author: gaojin
 *Time: 2023/2/13 20:27
 */

class CutSameDurationCheck(private val activity: FragmentActivity) : IPreSelectValidator<IMaterialItem>,IPostSelectValidator<IMaterialItem> {

    private val cutSameBottomDockerViewModel by lazy {
        CutSameBottomDockerViewModel.get(activity)
    }

    override suspend fun preCheck(
        incoming: IMaterialItem,
        selected: List<IMaterialItem>,
        selectedImage: List<IMaterialItem>,
        selectedVideo: List<IMaterialItem>,
        visible: Boolean
    ): Boolean {
        if (!visible) {
            return true
        }
        val cutSamePickItems = getCutSamePickItem()
        val cutSamePickItemsSize = getCutSamePickItem().size
        val isFull = isFull()
        if (isFull || selected.size >= cutSamePickItemsSize) {
            EOToaster.show(AppSingleton.instance, AppSingleton.instance.getString(R.string.eo_album_material_limit_tips, cutSamePickItemsSize))
            return false
        }

        if (!incoming.isVideo()) {
            return true
        }
        val duration = cutSamePickItems.firstOrNull { it.selected }?.mediaItem?.duration
        if (duration != null && (incoming.duration) < duration) {
            val second = String.format(Locale.ENGLISH, "%.1f", (duration / 1000F))
            EOToaster.show(AppSingleton.instance, AppSingleton.instance.getString(R.string.eo_album_tips_video_min_duration, second))
            return false
        }
        cutSamePickItems.firstOrNull { it.materialItem == null } ?: return false
        return true
    }

    override suspend fun postCheck(selected: List<IMaterialItem>, selectedImage: List<IMaterialItem>, selectedVideo: List<IMaterialItem>): Result {
        var itemEnable = true
        var confirmEnable = false
        val itemSize = selected.size
        val cutSamePickItemsSize = getCutSamePickItem().size
        val isFull = isFull()
        if (isFull || itemSize >= cutSamePickItemsSize) {
            itemEnable = false
        }
        if (isFull || itemSize >= cutSamePickItemsSize) {
            confirmEnable = true
        }
        return Result(
            confirmEnable = confirmEnable,
            videoSelectEnable = itemEnable,
            imageSelectEnable = itemEnable
        )
    }

    private fun getCutSamePickItem(): List<CutSameMediaItem> =
        cutSameBottomDockerViewModel.getCutSamePickItem()

    private fun isFull(): Boolean = cutSameBottomDockerViewModel.checkIsFull()
}
