package com.volcengine.effectone.auto.moment.hl.play

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.volcengine.effectone.auto.moment.R
import com.volcengine.effectone.auto.moment.hl.helper.AutoMaterialHighLightPlayInfoHelper
import com.volcengine.effectone.auto.moment.hl.helper.AutoMaterialPlayHelper
import com.volcengine.effectone.auto.moment.hl.vm.AutoMaterialHighLightPlayVM
import com.volcengine.effectone.extensions.setNoDoubleClickListener

class AutoMaterialHighLightPlayFragment : Fragment() {
    private val autoMaterialHighLightPlayVM by lazy { AutoMaterialHighLightPlayVM.get(requireActivity()) }
    private val autoMaterialPlayHelper by lazy { AutoMaterialPlayHelper(requireActivity(),viewLifecycleOwner) }
    private val autoMaterialHighLightPlayInfoHelper by lazy { AutoMaterialHighLightPlayInfoHelper(requireActivity(),viewLifecycleOwner) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            autoMaterialHighLightPlayVM.parseArguments(it)
        } ?: run { requireActivity().finish() }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_auto_material_high_light_play, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycle.addObserver(autoMaterialPlayHelper)
        viewLifecycleOwner.lifecycle.addObserver(autoMaterialHighLightPlayInfoHelper)
        super.onViewCreated(view, savedInstanceState)
        autoMaterialPlayHelper.initView(view as ViewGroup)
        autoMaterialHighLightPlayInfoHelper.initView(view as ViewGroup)
        initView(view)
        initObserver()
    }

    private fun initObserver() {

    }

    private fun initView(view: View) {
        view.findViewById<View>(R.id.auto_material_play_back)?.setNoDoubleClickListener {
            requireActivity().finish()
        }
    }

    companion object {
        const val TAG = "AutoMaterialHighLightPlayFragment"
    }
}