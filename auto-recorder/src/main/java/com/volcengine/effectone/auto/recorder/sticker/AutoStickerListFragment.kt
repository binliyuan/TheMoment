package com.volcengine.effectone.auto.recorder.sticker

import com.volcengine.effectone.data.IEOResourceItem
import com.volcengine.effectone.data.emptyResourceItem
import com.volcengine.effectone.sticker.EOBaseStickerViewModel
import com.volcengine.effectone.sticker.RecorderStickerTabFragment
import com.volcengine.effectone.sticker.data.EOBaseStickerItemWrapper
import com.volcengine.effectone.widget.EOToaster

class AutoStickerListFragment( stickerTabList: List<IEOResourceItem>, private  val tabIndex: Int) :
    RecorderStickerTabFragment(stickerTabList, tabIndex) {

    override val stickerList = stickerTabList[tabIndex].subItems?.addNoneExpectancyItem()?: emptyList()
    override val stickerRecyclerAdapter by lazy { AutoStickerRecyclerAdapter(stickerList) }
    private val stickerViewModel: EOBaseStickerViewModel by lazy {
        EOBaseStickerViewModel.get(
            requireActivity()
        )
    }
    override fun setupStickerRecyclerAdapter() {
        super.setupStickerRecyclerAdapter()
        stickerRecyclerAdapter.apply {
            stickerOtherSelectedAction = { wrapperItem, position ->
                stickerViewModel.selectedPos.value = tabIndex to position
                when (wrapperItem.type) {
                    EOBaseStickerItemWrapper.TYPE_NONE -> {
                        stickerViewModel.selectedItem.value = EOBaseStickerItemWrapper(emptyResourceItem())
                        stickerViewModel.selectedPos.value = Pair(-1, 0)
                    }
                    EOBaseStickerItemWrapper.TYPE_EXPECTANCY -> {
                        EOToaster.show(requireActivity(),"敬请期待")
                    }
                    else -> {}
                }
            }
        }
    }
}
private fun List<IEOResourceItem>.addNoneExpectancyItem():List<EOBaseStickerItemWrapper> {
    return mutableListOf<EOBaseStickerItemWrapper>().apply {
        addAll(this@addNoneExpectancyItem.map { item -> EOBaseStickerItemWrapper(item) })
        add(0,EOBaseStickerItemWrapper(emptyResourceItem()).apply {
            type = EOBaseStickerItemWrapper.TYPE_NONE
            state = EOBaseStickerItemWrapper.STATE_SUCCESS
        })
        add(size,EOBaseStickerItemWrapper(emptyResourceItem()).apply {
            type = EOBaseStickerItemWrapper.TYPE_EXPECTANCY
            state = EOBaseStickerItemWrapper.STATE_SUCCESS
        })
    }
}