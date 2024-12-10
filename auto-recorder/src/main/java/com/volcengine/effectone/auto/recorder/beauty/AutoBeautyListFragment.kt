package com.volcengine.effectone.auto.recorder.beauty

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.volcengine.effectone.auto.recorder.R
import com.volcengine.effectone.auto.recorder.beauty.adapter.AutoBeautyItemAdapter
import com.volcengine.effectone.auto.recorder.beauty.item.AutoBeautyItemDecoration
import com.volcengine.effectone.recorderui.beauty.BeautyViewModel
import com.volcengine.effectone.recorderui.beauty.OnItemClickListener
import com.volcengine.effectone.recorderui.beauty.data.ComposerItem
import com.volcengine.effectone.recorderui.beauty.data.ComposerTabItem
import com.volcengine.effectone.ui.BaseFragment
import com.volcengine.effectone.utils.SizeUtil

class AutoBeautyListFragment :BaseFragment(){
    companion object{
        const val TAG = "AutoBeautyListFragment"
    }
    override fun getFragmentTag() = tag ?: TAG


    private val beautyViewModel by lazy { BeautyViewModel.get(requireActivity()) }
    private lateinit var beautyItemAdapter: AutoBeautyItemAdapter

    private lateinit var composerTabItem: ComposerTabItem

    private val recyclerviewLayoutManager: LinearLayoutManager by lazy {
        GridLayoutManager(requireContext(),3,RecyclerView.VERTICAL,false)
    }

    private val beautyRecyclerView by lazy { rootView.findViewById<RecyclerView>(R.id.auto_recorder_beauty_list) }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.auto_recorder_layout_beauty_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        beautyRecyclerView?.apply {
            beautyItemAdapter = AutoBeautyItemAdapter(
                composerTabItem.items,
                composerTabItem.closeItem,
                onItemClickListener()
            )
            adapter = beautyItemAdapter
            layoutManager = recyclerviewLayoutManager
            addItemDecoration(AutoBeautyItemDecoration(SizeUtil.dp2px(1f),true))
        }
        initObserver()
    }

    private fun RecyclerView.onItemClickListener() =
        object : OnItemClickListener {
            override fun onItemClick(item: ComposerItem, position: Int) {
                if (!item.open) {
                    item.open = true
                    item.composeNode.value = item.composeNode.getDefaultValue()
                }
                beautyViewModel.currentVisibleComposerItem.value = item
                if (item.hasChild()) {
                    beautyViewModel.showSubFragmentEvent.value = item
                } else {
                    layoutManager?.smoothScrollToPosition(this@onItemClickListener, null, position)
                }
                beautyViewModel.beautyChangedEvent.value = Unit
            }

            override fun onCloseClick() {
                beautyViewModel.beautyChangedEvent.value = Unit
                if (composerTabItem.closeItem.isSelected) {
                    //一级none时候，其他选中未恢复
                    composerTabItem.items.forEachIndexed { _, composerItem ->
                        composerItem.isSelected = false
                    }
                    beautyViewModel.currentVisibleComposerItem.value = null
                } else {
                    beautyViewModel.currentVisibleComposerItem.value =
                        composerTabItem.items.firstOrNull { it.isSelected }
                }
            }
        }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObserver() {
        beautyViewModel.updateComposerIntensityEvent.observe(viewLifecycleOwner) {
            it?.let { item ->
                val index = composerTabItem.items.indexOfFirst { it.getPathNode() == item.getPathNode() }
                if (index != -1) {
                    beautyItemAdapter.notifyItemChanged(index + 1)
                }
            }
        }
        beautyViewModel.backSubListFragmentEvent.observe(viewLifecycleOwner) {
            beautyItemAdapter.notifyDataSetChanged()
        }

        beautyViewModel.resetBeautyEvent.observe(viewLifecycleOwner) {
            beautyItemAdapter.clearSelectState()
            beautyItemAdapter.notifyDataSetChanged()
        }
    }

    fun setComposerTabItem(composerTabItem: ComposerTabItem): AutoBeautyListFragment {
        this.composerTabItem = composerTabItem
        return this
    }

}