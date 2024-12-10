package com.volcengine.effectone.auto.recorder.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.volcengine.effectone.auto.recorder.fragment.AutoRecordTabType
import com.volcengine.effectone.auto.recorder.beauty.AutoBeautyFragment
import com.volcengine.effectone.auto.recorder.sticker.AutoStickerFragment

/**
 *Author: gaojin
 *Time: 2023/11/28 15:09
 */

class AutoDrawerTabsAdapter(
    manager: FragmentManager,
    lifecycle: Lifecycle,
    private val items: List<AutoRecordTabType>
) : FragmentStateAdapter(manager, lifecycle) {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun createFragment(position: Int): Fragment {
       return when (items[position]) {
           AutoRecordTabType.RECORDER_STICKER -> AutoStickerFragment()
           AutoRecordTabType.RECORDER_BEAUTY -> AutoBeautyFragment()
        }
    }
}