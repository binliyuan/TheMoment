package com.volcengine.effectone.auto.templates.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cutsame.solution.template.model.TemplateCategory
import com.volcengine.effectone.auto.templates.ui.AutoTemplatesPageFragment

/**
 * @author tyx
 * @description:
 * @date :2024/4/25 10:17
 */
class TemplateViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

	private val mTemplatesCategory = mutableListOf<TemplateCategory>()

	fun updateItems(items: List<TemplateCategory>) {
		val size = mTemplatesCategory.size
		mTemplatesCategory.clear()
		notifyItemRangeRemoved(0, size)
		mTemplatesCategory.addAll(items)
		notifyItemRangeInserted(0, mTemplatesCategory.size)
	}

	override fun getItemCount(): Int {
		return mTemplatesCategory.size
	}

	override fun createFragment(p0: Int): Fragment {
		return AutoTemplatesPageFragment.newInstance(mTemplatesCategory[p0])
	}
}