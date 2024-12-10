package com.volcengine.effectone.auto.recorder.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.volcengine.effectone.auto.recorder.AutoCameraViewModel
import com.volcengine.effectone.auto.recorder.R
import com.volcengine.effectone.auto.recorder.adapter.AutoDrawerTabsAdapter
import com.volcengine.effectone.auto.recorder.helper.AutoRecordDrawerSeekbarHelper
import com.volcengine.effectone.recorderui.beauty.BeautyRepo
import com.volcengine.effectone.recorderui.beauty.BeautyViewModel
import com.volcengine.effectone.ui.BaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 *Author: gaojin
 *Time: 2023/11/24 10:41 AM
 */
enum class AutoRecordTabType(val tabName:String){
    RECORDER_BEAUTY("美颜"),
    RECORDER_STICKER("贴纸");

    val isBeauty get() = this == RECORDER_BEAUTY
    val isSticker get() = this == RECORDER_STICKER
}
class AutoDrawerFragment : BaseFragment() {
    companion object {
        const val TAG = "AutoDrawerFragment"
    }

    private val beautyViewModel by lazy { BeautyViewModel.get(requireActivity()) }
    private val autoDrawerPanelHelper by lazy { AutoRecordDrawerSeekbarHelper(requireActivity(),viewLifecycleOwner) }
    private val cameraViewModel by lazy { AutoCameraViewModel.get(requireActivity()) }
    private val tabs by lazy { AutoRecordTabType.values().toList() }
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager2: ViewPager2
    private lateinit var tabLayoutMediator: TabLayoutMediator
    private var hasApplySticker = false
    private var needUpdateBeautyTab = false

    override fun getFragmentTag() = tag ?: TAG

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.auto_recorder_fragment_drawer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().lifecycle.addObserver(autoDrawerPanelHelper)
        super.onViewCreated(view, savedInstanceState)
        autoDrawerPanelHelper.initView(view as ViewGroup)
        tabLayout = view.findViewById(R.id.auto_recorder_drawer_tablayout)
        viewPager2 = view.findViewById(R.id.auto_recorder_drawer_viewpager2)
        viewPager2.isUserInputEnabled = false
        viewPager2.offscreenPageLimit = 1
        viewPager2.isNestedScrollingEnabled = true
        hasApplySticker = cameraViewModel.getCurrentSticker().isNotEmpty()
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                updateTabSelectedState(tab, true)
                updateBeautyTab(tab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                updateTabSelectedState(tab, false)
                triggerStickerChanged(tab)
            }
            override fun onTabReselected(p0: TabLayout.Tab) = Unit

            private fun triggerStickerChanged(tab: TabLayout.Tab) {
                when (tabs[tab.position]) {
                    AutoRecordTabType.RECORDER_BEAUTY -> {
                        //记录离开美颜tab的贴纸信息
                        hasApplySticker = cameraViewModel.getCurrentSticker().isNotEmpty()
                    }

                    AutoRecordTabType.RECORDER_STICKER -> {
                        //离开贴纸tab
                        needUpdateBeautyTab = hasApplySticker != cameraViewModel.getCurrentSticker().isNotEmpty()
                    }
                }
            }
            private fun updateBeautyTab(tab: TabLayout.Tab) {
                if (tabs[tab.position].isBeauty && needUpdateBeautyTab) {
                    beautyViewModel.resetBeautyEvent.value = Unit
                    needUpdateBeautyTab = false
                }
            }

            private fun updateTabSelectedState(tab: TabLayout.Tab, select: Boolean) {
                val textView = tab.customView?.findViewById<TextView>(R.id.auto_tablayout_tab_title)
                val color = ContextCompat.getColor(requireContext(), if (select) R.color.auto_tab_tile_selected else R.color.auto_tab_tile_unselected)
                textView?.setTextColor(color)
                val view = tab.customView?.findViewById<View>(R.id.auto_tablayout_tab_indicator)
                view?.visibility = if (select) View.VISIBLE else View.INVISIBLE
            }

        })
        requireCoroutineScope().launch(Dispatchers.Main) {
            if (!this@AutoDrawerFragment.isAdded) return@launch
            viewPager2.adapter = AutoDrawerTabsAdapter(childFragmentManager, viewLifecycleOwner.lifecycle, tabs)
            tabLayoutMediator =  TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
                val tabView = LayoutInflater.from(activity).inflate(R.layout.auto_recorder_layout_tablayout_tab,tab.view,false)
                tabView.findViewById<TextView>(R.id.auto_tablayout_tab_title)?.let {
                    it.text = tabs[position].tabName
                }
                tabView.findViewById<View>(R.id.auto_tablayout_tab_indicator)?.let {
                    it.visibility = View.INVISIBLE
                }
                tab.customView = tabView

            }.also { it.attach() }

            viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    beautyViewModel.beautyData.getOrNull(position)?.let { composerTabItem ->
                        if (!composerTabItem.closeItem.isSelected) {
                            beautyViewModel.currentVisibleComposerItem.value = composerTabItem.items.firstOrNull { it.isSelected }
                        } else {
                            beautyViewModel.currentVisibleComposerItem.value = null
                        }
                    }?: kotlin.run { beautyViewModel.currentVisibleComposerItem.value = null }
                }
            })
        }
        initObserver()
    }

    private fun initObserver() {
        //ignore
    }

    override fun onDestroy() {
        super.onDestroy()
        tabLayoutMediator.detach()
        beautyViewModel.triggerBeautySideBarState()
        BeautyRepo.updateRepo(beautyViewModel.beautyData)
    }
}