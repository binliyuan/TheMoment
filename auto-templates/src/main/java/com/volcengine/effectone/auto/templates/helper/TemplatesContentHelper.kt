package com.volcengine.effectone.auto.templates.helper

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.ss.ugc.android.editor.track.utils.visible
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.adapter.TemplateViewPagerAdapter
import com.volcengine.effectone.auto.templates.bean.BaseResultData
import com.volcengine.effectone.auto.templates.vm.TemplatesHomeViewModel
import com.volcengine.effectone.widget.EOLoadingView

/**
 * @author tyx
 * @description:
 * @date :2024/4/24 20:42
 */
class TemplatesContentHelper(
	override val activity: FragmentActivity,
	override val owner: LifecycleOwner,
	private val fragment: Fragment
) : IUIHelper {

	private val mViewModel by lazy { TemplatesHomeViewModel.create(activity) }

	private lateinit var mTabLayout: TabLayout
	private lateinit var mViewPager: ViewPager2
	private lateinit var mLoadingView: EOLoadingView
	private lateinit var mPageAdapter: TemplateViewPagerAdapter

	override fun initView(rootView: ViewGroup) {
		mTabLayout = rootView.findViewById(R.id.tab_layout)
		mViewPager = rootView.findViewById(R.id.view_pager)
		mLoadingView = rootView.findViewById(R.id.template_loading)
		mPageAdapter = TemplateViewPagerAdapter(fragment)
		mViewPager.adapter = mPageAdapter
		mViewPager.offscreenPageLimit = 1
		mTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
			override fun onTabSelected(tab: TabLayout.Tab) {
				tabSelectChange(tab, true)
			}

			override fun onTabUnselected(tab: TabLayout.Tab) {
				tabSelectChange(tab, false)
			}

			override fun onTabReselected(p0: TabLayout.Tab) {}
		})
		startObserver()
		mViewModel.loadTemplateCategoryList()
	}

	private fun tabSelectChange(tab: TabLayout.Tab, select: Boolean) {
		val textView = tab.customView?.findViewById<TextView>(R.id.tvTabTitle)
		val color = ContextCompat.getColor(activity, if (select) R.color.tab_select_text_color else R.color.tab_text_color)
		textView?.setTextColor(color)
		val view = tab.customView?.findViewById<View>(R.id.view_indicator)
		view?.visibility = if (select) View.VISIBLE else View.INVISIBLE
		textView?.setTypeface(null, if (select) Typeface.BOLD else Typeface.NORMAL)
	}

	private fun startObserver() {
		mViewModel.templatesCategoryList.observe(activity) { result ->
			result ?: return@observe
			when (result.state) {
				BaseResultData.START -> mLoadingView.visible = true
				BaseResultData.END -> {
					mLoadingView.visible = false
					result.resultData?.onSuccess { list ->
						mPageAdapter.updateItems(list)
						TabLayoutMediator(mTabLayout, mViewPager) { tab, position ->
							val inflater = LayoutInflater.from(activity)
							val customView = inflater.inflate(R.layout.tab_item, mTabLayout, false)
							val tvTitle = customView.findViewById<TextView>(R.id.tvTabTitle)
							tvTitle.text = list[position].name
							val view = customView.findViewById<View>(R.id.view_indicator)
							view.visibility = View.INVISIBLE
							tab.customView = customView
						}.attach()
					}
				}
			}
		}
	}
}