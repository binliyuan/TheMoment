package com.volcengine.effectone.auto.moment.hl.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.volcengine.effectone.auto.moment.hl.AutoMaterialRecognizeTabFragment
import com.volcengine.effectone.auto.moment.hl.AutoMaterialRecognizeTabType

class AutoMaterialRecognizeAdapter(
    manager: FragmentManager,
    lifecycle: Lifecycle,
    private val tabTypes: List<AutoMaterialRecognizeTabType>
) : FragmentStateAdapter(manager,lifecycle) {
    private val tabFragments = mutableMapOf<AutoMaterialRecognizeTabType, Fragment>()
    override fun getItemCount() = tabTypes.size

    override fun createFragment(position: Int): Fragment {
        val tabType = tabTypes[position]
        var fragment = tabFragments[tabType]
        if (fragment == null) {
            fragment =  AutoMaterialRecognizeTabFragment().apply {
                arguments = (arguments?:Bundle()).also {
                    it.putString(AutoMaterialRecognizeTabFragment.ARGUMENTS_KEY_TAB_TYPE,tabType.name)
                }
            }.also {
                 tabFragments[tabType] = it
             }
        }
       return  fragment
    }

}
