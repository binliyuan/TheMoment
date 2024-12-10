package com.volcengine.effectone.auto.templates.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.ies.cutsame.util.SizeUtil
import com.cutsame.solution.template.model.TemplateCategory
import com.volcengine.ck.logkit.LogKit
import com.volcengine.effectone.auto.common.widget.GridItemDecoration
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.adapter.TemplatesItemAdapter
import com.volcengine.effectone.auto.templates.bean.BaseResultData
import com.volcengine.effectone.auto.templates.utils.showToast
import com.volcengine.effectone.auto.templates.vm.TemplatesHomeViewModel
import com.volcengine.effectone.auto.templates.vm.TemplatesPageViewModel
import com.volcengine.effectone.auto.templates.widget.LoadingDialog
import kotlin.math.roundToInt

/**
 * @author tyx
 * @description:
 * @date :2024/4/25 10:00
 */
class AutoTemplatesPageFragment : Fragment() {
	companion object {
		private const val ARGUMENT_CATEGORY = "ARGUMENT_CATEGORY"
		private const val TAG = "TemplatesPageFragment"
		fun newInstance(category: TemplateCategory): AutoTemplatesPageFragment {
			val templatesPageFragment = AutoTemplatesPageFragment()
			return templatesPageFragment.apply {
				arguments = Bundle().apply { putParcelable(ARGUMENT_CATEGORY, category) }
			}
		}
	}

	private val mLoadingDialog by lazy { LoadingDialog(requireActivity()) }
	private var mTemplateCategory: TemplateCategory? = null
	private lateinit var mRecyclerView: RecyclerView
	private lateinit var mTemplatesAdapter: TemplatesItemAdapter
	private val mHomeViewModel by lazy { TemplatesHomeViewModel.create(requireActivity()) }
	private val mViewModel by lazy { ViewModelProvider(this).get(TemplatesPageViewModel::class.java) }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		mTemplateCategory = arguments?.getParcelable(ARGUMENT_CATEGORY)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_templates_page, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initRecyclerView(view)
		startObserver()
		mViewModel.filterTemplatesList(mTemplateCategory, mHomeViewModel.mTemplatesList)
	}

	private fun initRecyclerView(view: View) {
		val spanCount = 4
		mRecyclerView = view.findViewById(R.id.recycler_view)
		mTemplatesAdapter = TemplatesItemAdapter()
		mRecyclerView.adapter = mTemplatesAdapter
		mRecyclerView.layoutManager = GridLayoutManager(requireActivity(), spanCount)
		mRecyclerView.addItemDecoration(GridItemDecoration(spanCount, SizeUtil.dp2px(20f), SizeUtil.dp2px(20f)))
		mTemplatesAdapter.clickBlock = { _, position, data ->
			LogKit.d(TAG, "click position = $position,id = ${data.id}")
			mViewModel.loadTemplateResource(data)
		}
	}

	private fun startObserver() {
		mViewModel.mTemplateListResult.observe(viewLifecycleOwner) { result ->
			result ?: return@observe
			when (result.state) {
				BaseResultData.END -> {
					result.resultData?.onSuccess {
						mTemplatesAdapter.updateItems(it)
					}
				}
			}
		}
		mViewModel.mTemplateLoadResult.observe(viewLifecycleOwner) { result ->
			result ?: return@observe
			when (result.state) {
				BaseResultData.START -> showLoading()
				BaseResultData.PROGRESS -> mLoadingDialog.setMessage(String.format("加载中%d%%...", result.progress.roundToInt()))
				BaseResultData.END -> {
					highLoading()
					result.resultData?.onSuccess {
						AutoTemplatePreviewActivity.launch(requireActivity(), it)
					}?.onFailure {
						it.message?.showToast(requireActivity())
					}
				}
			}
		}
	}

	private fun showLoading() {
		if (mLoadingDialog.isShowing) return
		mLoadingDialog.setMessage("加载中...")
		mLoadingDialog.show()
	}

	private fun highLoading() {
		if (mLoadingDialog.isShowing) mLoadingDialog.dismiss()
	}
}