package com.volcengine.effectone.auto.templates.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.helper.PageTitleHelper
import com.volcengine.effectone.auto.templates.helper.TemplatesContentHelper

/**
 * @author tyx
 * @description:
 * @date :2024/4/24 14:01
 */
class AutoTemplatesHomeFragment : Fragment() {

	private val mViewHelper by lazy {
		listOf(
			PageTitleHelper(requireActivity(), viewLifecycleOwner, getString(R.string.eo_frontpage_magictemplates)),
			TemplatesContentHelper(requireActivity(), viewLifecycleOwner, this),
		)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_home_templates, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		mViewHelper.forEach { lifecycle.addObserver(it) }
		super.onViewCreated(view, savedInstanceState)
		mViewHelper.forEach { it.initView(view as ViewGroup) }
	}
}