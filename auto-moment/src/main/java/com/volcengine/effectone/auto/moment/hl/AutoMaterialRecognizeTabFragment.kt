package com.volcengine.effectone.auto.moment.hl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.volcengine.effectone.auto.common.widget.GridItemDecoration
import com.volcengine.effectone.auto.moment.R
import com.volcengine.effectone.auto.moment.hl.adapter.AutoMaterialRecognizeTabAdapter
import com.volcengine.effectone.auto.moment.hl.vm.AutoMaterialRecognizeViewModel
import com.volcengine.effectone.utils.SizeUtil

class AutoMaterialRecognizeTabFragment : Fragment() {

    private val autoMaterialRecognizeViewModel by lazy { AutoMaterialRecognizeViewModel.get(requireActivity()) }

    private val autoMaterialRecognizeTabAdapter by lazy { AutoMaterialRecognizeTabAdapter() }
    private lateinit var curTabType: AutoMaterialRecognizeTabType
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        curTabType =  AutoMaterialRecognizeTabType.valueOf(requireArguments().getString(ARGUMENTS_KEY_TAB_TYPE)!!)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_auto_material_recognize_tab,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        initObserver()
        initData()
    }

    private fun initData() {
        autoMaterialRecognizeTabAdapter.updateItems(autoMaterialRecognizeViewModel.getCurTabItems(curTabType))
    }

    private fun initView(view: View) {
        view.findViewById<RecyclerView>(R.id.auto_material_recognize_recyclerView)?.apply {
            val spanCount = 3
            adapter = autoMaterialRecognizeTabAdapter
            layoutManager =
                GridLayoutManager(requireContext(), spanCount, GridLayoutManager.VERTICAL, false)
            addItemDecoration(
                GridItemDecoration(
                    spanCount,
                    SizeUtil.dp2px(17f),
                    SizeUtil.dp2px(17f)
                )
            )
        }
    }

    private fun initObserver() {
        autoMaterialRecognizeTabAdapter.goneDetailAction = {
            autoMaterialRecognizeViewModel.goDetail(requireActivity(),it)
        }
        autoMaterialRecognizeTabAdapter.goHlExtractAction = {
            autoMaterialRecognizeViewModel.showHighLightFragment(requireActivity(),it)
        }

        autoMaterialRecognizeViewModel.updateAdapterUI.observe(viewLifecycleOwner){
            it?.let {
                autoMaterialRecognizeTabAdapter.updateItems(autoMaterialRecognizeViewModel.getCurTabItems(curTabType))
            }
        }
        autoMaterialRecognizeViewModel.updateItem.observe(viewLifecycleOwner){
            it?.let {
                autoMaterialRecognizeTabAdapter.updateItem(it)
            }
        }

    }


    companion object {
        const val ARGUMENTS_KEY_TAB_TYPE = "arguments_key_tab_type"
    }
}
