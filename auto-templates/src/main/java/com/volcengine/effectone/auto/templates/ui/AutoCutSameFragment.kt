package com.volcengine.effectone.auto.templates.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.bytedance.ies.cutsame.util.SizeUtil
import com.gyf.immersionbar.ImmersionBar
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.helper.CutSamePageTitleHelper
import com.volcengine.effectone.auto.templates.helper.CutSameTemplatesSelectHelper
import com.volcengine.effectone.auto.templates.vm.TemplatesSelectViewModel
import com.volcengine.effectone.auto.templates.widget.RoundTransition
import com.volcengine.effectone.auto.templates.widget.TopRoundFrameLayout

/**
 * @author tyx
 * @description:
 * @date :2024/4/26 16:24
 */
class AutoCutSameFragment : Fragment() {

	companion object {
		private const val HAS_SWITCH = "hasSwitch"
		const val TAG = "AutoCutSameFragment"
		fun getInstance(hasSwitch: Boolean): AutoCutSameFragment {
			return AutoCutSameFragment().apply {
				arguments = Bundle().apply {
					putBoolean(HAS_SWITCH, hasSwitch)
				}
			}
		}
	}

	private val mTemplateSelectViewModel by lazy { TemplatesSelectViewModel.get(requireActivity()) }
	private val mViewHelperList by lazy { mutableListOf<IUIHelper>() }
	private var mHasSwitch = false   //是否需要切换功能
	private lateinit var mTemplateSelectLayout: FrameLayout
	private lateinit var mPlayerLayout: TopRoundFrameLayout
	private lateinit var mConsLayout: ConstraintLayout

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		mHasSwitch = arguments?.getBoolean(HAS_SWITCH) ?: mHasSwitch
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_cutsame, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		mViewHelperList.clear()
		mViewHelperList.add(CutSamePageTitleHelper(requireActivity(), viewLifecycleOwner, mHasSwitch))
		if (mHasSwitch) mViewHelperList.add(CutSameTemplatesSelectHelper(requireActivity(), viewLifecycleOwner))
		mViewHelperList.forEach { lifecycle.addObserver(it) }
		super.onViewCreated(view, savedInstanceState)
		mViewHelperList.forEach { it.initView(view as ViewGroup) }
		initView(view)
		startObserver()
		initPlayerFragment()
		mTemplateSelectViewModel.mIsShowTemplatePanel.value = mHasSwitch
	}

	private fun initView(view: View) {
		mTemplateSelectLayout = view.findViewById(R.id.templates_select_layout)
		mPlayerLayout = view.findViewById(R.id.player_layout)
		if (mHasSwitch) mPlayerLayout.setRadius(SizeUtil.dp2px(16f).toFloat())
		mConsLayout = view.findViewById(R.id.cons_layout)
	}

	private fun initPlayerFragment() {
		val fragment = childFragmentManager.findFragmentByTag(AutoCutSamePlayerFragment.TAG) ?: AutoCutSamePlayerFragment()
		childFragmentManager.beginTransaction()
			.replace(mPlayerLayout.id, fragment, AutoCutSamePlayerFragment.TAG)
			.commitNow()
	}

	private fun startObserver() {
		mTemplateSelectViewModel.mIsShowTemplatePanel.observe(viewLifecycleOwner) { isShow ->
			isShow ?: return@observe
			val constraintSet = ConstraintSet()
			constraintSet.clone(mConsLayout)
			val transitionSet = TransitionSet()
			transitionSet.addTransition(ChangeBounds())
			transitionSet.setDuration(200)
			if (isShow) {
				transitionSet.addTransition(RoundTransition(mPlayerLayout, 0, SizeUtil.dp2px(16f)))
				TransitionManager.beginDelayedTransition(mConsLayout, transitionSet)
				val topMargin = SizeUtil.dp2px(48f) + ImmersionBar.getStatusBarHeight(requireActivity())
				constraintSet.setMargin(mTemplateSelectLayout.id, ConstraintSet.TOP, topMargin)
				constraintSet.connect(mPlayerLayout.id, ConstraintSet.TOP, mTemplateSelectLayout.id, ConstraintSet.TOP)
				constraintSet.connect(mPlayerLayout.id, ConstraintSet.LEFT, mTemplateSelectLayout.id, ConstraintSet.RIGHT)
				constraintSet.setAlpha(mTemplateSelectLayout.id, 1f)
				constraintSet.setMargin(mPlayerLayout.id, ConstraintSet.RIGHT, SizeUtil.dp2px(32f))
				constraintSet.applyTo(mConsLayout)
			} else {
				transitionSet.addTransition(RoundTransition(mPlayerLayout, SizeUtil.dp2px(16f), 0))
				TransitionManager.beginDelayedTransition(mConsLayout, transitionSet)
				constraintSet.setMargin(mTemplateSelectLayout.id, ConstraintSet.TOP, 0)
				constraintSet.connect(mPlayerLayout.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
				constraintSet.connect(mPlayerLayout.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT)
				constraintSet.setAlpha(mTemplateSelectLayout.id, 0f)
				constraintSet.setMargin(mPlayerLayout.id, ConstraintSet.RIGHT, 0)
				constraintSet.applyTo(mConsLayout)
			}
		}
	}
}