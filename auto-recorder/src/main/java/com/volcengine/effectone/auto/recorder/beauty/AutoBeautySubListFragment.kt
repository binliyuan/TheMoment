package com.volcengine.effectone.auto.recorder.beauty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.volcengine.ck.logkit.LogKit
import com.volcengine.effectone.auto.recorder.R
import com.volcengine.effectone.auto.recorder.beauty.adapter.AutoBeautyItemAdapter
import com.volcengine.effectone.auto.recorder.beauty.item.AutoBeautyItemDecoration
import com.volcengine.effectone.extensions.setNoDoubleClickListener
import com.volcengine.effectone.recorderui.beauty.BeautyItemAdapter
import com.volcengine.effectone.recorderui.beauty.BeautyViewModel
import com.volcengine.effectone.recorderui.beauty.OnItemClickListener
import com.volcengine.effectone.recorderui.beauty.data.ComposerItem
import com.volcengine.effectone.ui.BaseFragment
import com.volcengine.effectone.utils.SizeUtil

class AutoBeautySubListFragment :BaseFragment() {
    companion object{
        private const val TAG = "AutoBeautySubListFragme"
    }

    private val beautyViewModel by lazy { BeautyViewModel.get(requireActivity()) }

    open  lateinit var totalItem: ComposerItem

    private lateinit var beautyAdapter: BeautyItemAdapter

    private val recyclerviewLayoutManager: LinearLayoutManager by lazy {
        GridLayoutManager(requireContext(),3,RecyclerView.VERTICAL,false)
    }

    override fun getFragmentTag() = tag?: TAG
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.auto_recorder_layout_beauty_sublist, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.eo_recorder_beauty_sublist_back).setNoDoubleClickListener {
            beautyViewModel.currentVisibleComposerItem.value = null
            beautyViewModel.showSubFragmentEvent.value = null
            beautyViewModel.backSubListFragmentEvent.value = Unit
            parentFragmentManager.beginTransaction().remove(this).commit()
        }
        view.findViewById<TextView>(R.id.eo_recorder_beauty_sublist_title).run {
            text = totalItem.title
        }
        view.findViewById<RecyclerView>(R.id.eo_recorder_beauty_sublist).run {
            beautyAdapter = beautyItemAdapter()
            adapter = beautyAdapter
            layoutManager = recyclerviewLayoutManager
            addItemDecoration(AutoBeautyItemDecoration(SizeUtil.dp2px(2f),true))
        }

        beautyViewModel.currentVisibleComposerItem.value = totalItem.items.firstOrNull { it.isSelected }

        beautyViewModel.resetBeautyEvent.observe(viewLifecycleOwner) {
            beautyAdapter.clearSelectState()
            beautyAdapter.notifyDataSetChanged()
        }

        beautyViewModel.updateComposerIntensityEvent.observe(viewLifecycleOwner) {
            it?.let { item ->
                val index = totalItem.items.indexOfFirst { it.getPathNode() == item.getPathNode() }
                if (index != -1) {
                    beautyAdapter.notifyItemChanged(index + 1)
                }
            }
        }
    }

    private fun beautyItemAdapter() = AutoBeautyItemAdapter(
        totalItem.items,
        totalItem.closeItem,
        onItemClickListener(),
        showPoint = false
    )

    private fun onItemClickListener() = object : OnItemClickListener {
        override fun onItemClick(item: ComposerItem, position: Int) {
            totalItem.items.forEach {
                if (it.getPathNode() == item.getPathNode()) {
                    it.open = true
                    if (it.composeNode.value == -1F) {
                        it.composeNode.value = it.composeNode.getDefaultValue()
                    }
                } else {
                    it.open = false
                }
            }
            beautyViewModel.currentVisibleComposerItem.value = item
            if (item.hasChild()) {
                LogKit.d(TAG, "only support two level")
            } else {
                beautyViewModel.beautyChangedEvent.value = Unit
            }
        }

        override fun onCloseClick() {
            beautyViewModel.beautyChangedEvent.value = Unit
            if (totalItem.closeItem.isSelected) {
                //二级none时候，其他选中未恢复
                totalItem.items.forEachIndexed { _, composerItem ->
                    composerItem.isSelected = false
                }
                beautyViewModel.currentVisibleComposerItem.value = null
            } else {
                beautyViewModel.currentVisibleComposerItem.value =
                    totalItem.items.firstOrNull { it.isSelected }
            }
        }
    }

    fun setItem(item: ComposerItem) {
        this.totalItem = item
    }
}