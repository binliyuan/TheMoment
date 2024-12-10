package com.volcengine.effectone.auto.algorithm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.volcengine.effectone.auto.algorithm.helper.AlgorithmContentViewHelper
import com.volcengine.effectone.auto.algorithm.viewmodel.AlgorithmRecoderEventTrackViewModel
import com.volcengine.effectone.auto.algorithm.viewmodel.AlgorithmViewModel
import com.volcengine.effectone.recorderui.viewmodel.RecoderEventTrackViewModel

class AlgorithmFragment : Fragment() {
    companion object {
        private const val TAG = "AlgorithmFragment"
    }

    private val algorithmViewModel by lazy { AlgorithmViewModel.get(requireActivity()) }
    private val recorderEventTrackViewModel by lazy { AlgorithmRecoderEventTrackViewModel.get(requireActivity()) }

    private val viewHelpers by lazy {
        mutableListOf(
            //Record 内容
            AlgorithmContentViewHelper(requireActivity(), viewLifecycleOwner),
            //Drawer右侧抽屉管理
//            AutoRecordDrawerHelper(requireActivity(), viewLifecycleOwner),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        algorithmViewModel.injectLifeCycle(lifecycle)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(com.volcengine.effectone.auto.recorder.R.layout.auto_recorder_root_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewHelpers.forEach {
            requireActivity().lifecycle.addObserver(it)
        }
        super.onViewCreated(view, savedInstanceState)
        viewHelpers.forEach {
            it.initView(view as ViewGroup)
        }

        initObserver()
    }

    private fun initObserver() {
        recorderEventTrackViewModel.initObserver(viewLifecycleOwner)
    }
}