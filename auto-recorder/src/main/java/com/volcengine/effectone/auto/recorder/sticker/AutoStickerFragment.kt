package com.volcengine.effectone.auto.recorder.sticker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.volcengine.effectone.auto.recorder.R
import com.volcengine.effectone.auto.recorder.viewmodel.AutoRecordUIViewModel
import com.volcengine.effectone.data.IEOResourceItem
import com.volcengine.effectone.extensions.setNoDoubleClickListener
import com.volcengine.effectone.sticker.EOBaseStickerViewModel
import com.volcengine.effectone.sticker.RecorderStickerTabFragment
import com.volcengine.effectone.sticker.base.EOBaseStickerTabConfig
import com.volcengine.effectone.ui.BaseFragment
import com.volcengine.effectone.widget.EOLoadingState
import com.volcengine.effectone.widget.EOLoadingView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AutoStickerFragment : BaseFragment() {
    companion object {
        const val TAG = "AutoStickerFragment"
        private const val REQUIRED_TAB_INDEX = 0
    }

    private val stickerViewModel by lazy { EOBaseStickerViewModel.get(requireActivity()) }
    private val recordUIViewModel by lazy { AutoRecordUIViewModel.get(requireActivity()) }
    override fun getFragmentTag() = tag ?: TAG
    private lateinit var loadingView: EOLoadingView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.auto_recorder_fragment_sticker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingView = view.findViewById<EOLoadingView?>(R.id.auto_recorder_sticker_loading).apply {
            setNoDoubleClickListener{
                loadData()
            }
        }
        recordUIViewModel.drawerViewState.observe(viewLifecycleOwner){
            if (it && isResumed) {
                loadData()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (recordUIViewModel.isDrawerOpened()) {
            loadData()
        }
    }
    private fun loadData() {
        requireCoroutineScope().launch(Dispatchers.Main) {
            var stickerTabList = stickerViewModel.stickerList.value
            if (stickerTabList.isNullOrEmpty()) {
                loadingView.setState(EOLoadingState.LOADING)
                loadingView.visibility = View.VISIBLE
                stickerTabList = withContext(Dispatchers.IO) {
                    val loadResourceList = stickerViewModel.loadResourceList()
                    loadResourceList.takeIf { it.isNotEmpty() }?.let { tabLists->
                        //取第一个tab
                        tabLists.subList(REQUIRED_TAB_INDEX, REQUIRED_TAB_INDEX + 1).apply {
                            this[REQUIRED_TAB_INDEX].subItems = //取所有 tabItem 的 subItems
                                tabLists.flatMapTo(mutableListOf<IEOResourceItem>()){  tabItem->
                                    tabItem.subItems?: emptyList()
                                }
                        }
                    }?: emptyList()
                }
                stickerViewModel.stickerList.value = stickerTabList

                if (stickerTabList.isEmpty()) {
                    loadingView.setState(EOLoadingState.NETWORK_ERROR)
                    return@launch
                }
                loadingView.visibility = View.GONE
            }

            if (!this@AutoStickerFragment.isAdded) return@launch
            var fragment = childFragmentManager.findFragmentByTag(RecorderStickerTabFragment.TAG)
            if (fragment == null) {
                fragment = AutoStickerListFragment(stickerTabList, REQUIRED_TAB_INDEX).apply {
                    arguments = arguments ?: Bundle()
                    requireArguments().putParcelable(
                        RecorderStickerTabFragment.ARGUMENT_KEY_STICKER_TAB_CONFIG,
                        EOBaseStickerTabConfig(spanCount = 3)
                    )
                }
                childFragmentManager.beginTransaction().replace(
                    R.id.auto_recorder_sticker_root, fragment,
                    RecorderStickerTabFragment.TAG
                ).commitAllowingStateLoss()
            }
        }
    }
}