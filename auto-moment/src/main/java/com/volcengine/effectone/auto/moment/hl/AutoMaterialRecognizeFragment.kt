package com.volcengine.effectone.auto.moment.hl

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.bytedance.creativex.visibleOrGone
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.gyf.immersionbar.ImmersionBar
import com.volcengine.ck.logkit.LogKit
import com.volcengine.effectone.auto.common.extention.updateViewMargin
import com.volcengine.effectone.auto.moment.R
import com.volcengine.effectone.auto.moment.hl.adapter.AutoMaterialRecognizeAdapter
import com.volcengine.effectone.auto.moment.hl.launcher.AutoMaterialRecognizeAlbumLauncher
import com.volcengine.effectone.auto.moment.hl.vm.AutoMaterialRecognizeViewModel
import com.volcengine.effectone.extensions.setNoDoubleClickListener
import com.volcengine.effectone.utils.SizeUtil


enum class AutoMaterialRecognizeTabType(val tabName:String){
    MATERIAL_ALL("全部"),
    MATERIAL_IMG("图片素材提取"),
    MATERIAL_VIDEO("视频素材提取");

    fun AutoMaterialRecognizeTabType.isAllTab() = this == MATERIAL_ALL
    fun AutoMaterialRecognizeTabType.isImgTab() =  this == MATERIAL_IMG
    fun AutoMaterialRecognizeTabType.isVideoTab() = this == MATERIAL_VIDEO
}
class AutoMaterialRecognizeFragment : Fragment() {
    private val autoMaterialRecognizeViewModel by lazy { AutoMaterialRecognizeViewModel.get(requireActivity()) }

    private val autoMaterialRecognizeAlbumLauncher by lazy { AutoMaterialRecognizeAlbumLauncher(requireActivity()) }

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayoutMediator: TabLayoutMediator
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            autoMaterialRecognizeViewModel.parseArguments(it)
        } ?: run {
            LogKit.d(TAG, "onCreate() Fragment   $this   does not have any arguments")
            requireActivity().finish()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_auto_material_recognize, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycle.addObserver(autoMaterialRecognizeAlbumLauncher)
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        initObserver()
        initData()
    }

    private fun initData() {
        autoMaterialRecognizeViewModel.startScan()
    }

    private fun initObserver() {
       //ignore
    }

    private fun initView(view: View) {
        view.findViewById<View>(R.id.auto_material_recognize_topContainer)?.apply {
           post{
               val statusBarHeight = ImmersionBar.getStatusBarHeight(this.context)
               val diff = SizeUtil.dp2px(TOP_MARGIN) - statusBarHeight
               this.updateViewMargin(topMargin = diff)
           }
        }
        view.findViewById<View>(R.id.auto_material_recognize_back)?.setNoDoubleClickListener {
            requireActivity().finish()
        }
        view.findViewById<View>(R.id.auto_material_recognize_add_container)?.setNoDoubleClickListener {
            autoMaterialRecognizeAlbumLauncher.launchAlbum()
        }
        tabLayout = view.findViewById(R.id.auto_material_recognize_tabLayout)
        viewPager = view.findViewById(R.id.auto_material_recognize_viewPager)
        setupTabLayout()
    }

    private fun setupTabLayout() {
        val tabTypes = AutoMaterialRecognizeTabType.values().toList()
        viewPager.adapter = AutoMaterialRecognizeAdapter(childFragmentManager,viewLifecycleOwner.lifecycle,tabTypes)
        viewPager.isUserInputEnabled = false
        viewPager.offscreenPageLimit = 2
        tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager, true, false) { tab, pos ->
            val inflater = LayoutInflater.from(tabLayout.context)
            val customView = inflater.inflate(R.layout.auto_layout_tab, tabLayout, false)
            val tabTitle = customView.findViewById<TextView>(R.id.tab_title)
            tabTitle.text = tabTypes[pos].tabName
            val indicator = customView.findViewById<View>(R.id.tab_indicator)
            indicator.visibility = View.INVISIBLE
            tab.customView = customView
        }
        tabLayout.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab) {
                updateTabStyle(tab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                updateTabStyle(tab)
            }

            override fun onTabReselected(tab: TabLayout.Tab) =Unit

            private fun updateTabStyle(tab:TabLayout.Tab){
                val selected = tab.isSelected
                tab.customView?.findViewById<View>(R.id.tab_indicator)?.visibleOrGone = selected
                tab.customView?.findViewById<TextView>(R.id.tab_title)?.run {
                    val color = ContextCompat.getColor(
                        tabLayout.context,
                        if (selected) R.color.TextPrimary else com.volcengine.effectone.auto.common.R.color.color_60EEEEEE
                    )
                    setTextColor(color)
                    setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)
                }
            }

        })
        tabLayoutMediator.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tabLayoutMediator.detach()
    }


    companion object {
        const val TAG = "AutoMaterialRecognizeFragment"
        const val TOP_MARGIN = 38f
    }
}