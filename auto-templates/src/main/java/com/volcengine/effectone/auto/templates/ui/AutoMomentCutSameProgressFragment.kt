package com.volcengine.effectone.auto.templates.ui

import android.os.Bundle
import com.cutsame.solution.template.model.TemplateItem
import com.volcengine.effectone.auto.common.cutsame.CutSameContract
import com.volcengine.effectone.auto.templates.bean.BaseResultData
import com.volcengine.effectone.auto.templates.vm.CutSameViewModel
import kotlin.math.roundToInt

/**
 * @author tyx
 * @description:
 * @date :2024/6/5 18:25
 */
class AutoMomentCutSameProgressFragment : AutoComposeFragment() {

	companion object {
		const val TAG = "MomentCutSameProgressFragment"
		fun getInstance(templateItem: TemplateItem?): AutoMomentCutSameProgressFragment {
			val bundle = Bundle()
			return AutoMomentCutSameProgressFragment().apply {
				arguments = bundle.apply {
					putParcelable(CutSameContract.ARG_TEMPLATE_ITEM, templateItem)
				}
			}
		}
	}

	private val mCutSameViewModel by lazy { CutSameViewModel.get(requireActivity()) }

	override fun startObserver() {
		mCutSameViewModel.mPrepareLiveData.observe(viewLifecycleOwner) { result ->
			if (result.state == BaseResultData.START || result.state == BaseResultData.PROGRESS) {
				mProgressbar.progress = (result.progress / 100f * 50).roundToInt()
			}
		}
		mCutSameViewModel.mComposeLiveData.observe(viewLifecycleOwner) { result ->
			if (result.state == BaseResultData.START || result.state == BaseResultData.PROGRESS) {
				mProgressbar.progress = 50 + (result.progress / 100f * 50).roundToInt()
			}
		}
	}
}