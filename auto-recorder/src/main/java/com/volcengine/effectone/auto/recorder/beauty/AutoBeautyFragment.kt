package com.volcengine.effectone.auto.recorder.beauty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.volcengine.effectone.auto.recorder.AutoCameraViewModel
import com.volcengine.effectone.auto.recorder.R
import com.volcengine.effectone.auto.recorder.viewmodel.AutoRecordUIViewModel
import com.volcengine.effectone.extensions.setNoDoubleClickListener
import com.volcengine.effectone.recorderui.beauty.BeautyRepo
import com.volcengine.effectone.recorderui.beauty.BeautyViewModel
import com.volcengine.effectone.recorderui.beauty.toSingleComposerTabItemList
import com.volcengine.effectone.ui.BaseFragment
import com.volcengine.effectone.widget.EOLoadingState
import com.volcengine.effectone.widget.EOLoadingView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AutoBeautyFragment : BaseFragment() {
    companion object {
        private const val TAG = "AutoBeautyFragment"
        private const val ONE_TAB = 0
    }

    private val beautyViewModel by lazy { BeautyViewModel.get(requireActivity()) }
    private val cameraViewModel by lazy { AutoCameraViewModel.get(requireActivity()) }
    private val recordUIViewModel by lazy { AutoRecordUIViewModel.get(requireActivity()) }

    private lateinit var loadingView: EOLoadingView

    override fun getFragmentTag() = tag ?: TAG

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.auto_recorder_fragment_beauty, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingView = view.findViewById<EOLoadingView?>(R.id.auto_recorder_beauty_loading).apply {
            setNoDoubleClickListener {
                loadData()
            }
        }
        initObserver()
    }

    private fun initObserver() {
        beautyViewModel.showSubFragmentEvent.observe(viewLifecycleOwner) {
            it?.let { composerItem ->
                val manager = childFragmentManager
                val transaction = manager.beginTransaction()
                val sublistFragment = AutoBeautySubListFragment().apply {
                    setItem(composerItem)
                }
                transaction.add(R.id.auto_beauty_fragment_container, sublistFragment)
                transaction.commit()
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
    private fun loadData(){

        requireCoroutineScope().launch(Dispatchers.Main) {
            if (beautyViewModel.isDefaultData) {
                beautyViewModel.beautyData.clear()
                beautyViewModel.isDefaultData = false
            }
            if (beautyViewModel.beautyData.isEmpty()) {
                loadingView.setState(EOLoadingState.LOADING)
                loadingView.visibility = View.VISIBLE
                withContext(Dispatchers.IO) {
                    val resourceItems = beautyViewModel.loadResourceList()
                    resourceItems.forEach {
                        beautyViewModel.loadResourceItem(it)
                    }
                    resourceItems.toSingleComposerTabItemList (beautyViewModel.beautyData)
                    beautyViewModel.buildComposerList()
                }
                val hasSticker = cameraViewModel.getCurrentSticker().isNotEmpty()
                if (hasSticker) {
                    beautyViewModel.updateBeautyConflictWithSticker(false)
                } else {
                    beautyViewModel.updateBeautyConflictWithSticker(true)
                }
                if (beautyViewModel.beautyData.isEmpty()) {
                    loadingView.setState(EOLoadingState.NETWORK_ERROR)
                    return@launch
                }
                loadingView.visibility = View.GONE
            }

            if (!this@AutoBeautyFragment.isAdded) return@launch
            var autoBeautyPanel: Fragment? = childFragmentManager.findFragmentByTag(AutoBeautyListFragment.TAG)
            if (autoBeautyPanel == null) {
                autoBeautyPanel = AutoBeautyListFragment().setComposerTabItem(beautyViewModel.beautyData[ONE_TAB])
                childFragmentManager.beginTransaction().replace(
                    R.id.auto_beauty_fragment_container,
                    autoBeautyPanel,
                    AutoBeautyListFragment.TAG
                ).commitNowAllowingStateLoss()
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        beautyViewModel.triggerBeautySideBarState()
        BeautyRepo.updateRepo(beautyViewModel.beautyData)
    }
}




