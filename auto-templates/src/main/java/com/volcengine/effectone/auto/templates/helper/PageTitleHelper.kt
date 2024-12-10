package com.volcengine.effectone.auto.templates.helper

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.gyf.immersionbar.ImmersionBar
import com.ss.android.ugc.aweme.views.setDebounceOnClickListener
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.templates.R

/**
 * @author tyx
 * @description:
 * @date :2024/4/24 15:32
 */
open class PageTitleHelper(
	override val activity: FragmentActivity,
	override val owner: LifecycleOwner,
	private val title: String = "",
	private val fitsSystemWindows: Boolean = false
) : IUIHelper {

	private var mTvTitle: TextView? = null

	override fun initView(rootView: ViewGroup) {
		mTvTitle = rootView.findViewById(R.id.tv_title)
		if (fitsSystemWindows) {
			val titleLayout = rootView.findViewById<ConstraintLayout>(R.id.title_layout)
			val lp = titleLayout.layoutParams as ViewGroup.MarginLayoutParams
			titleLayout.layoutParams = lp.apply { topMargin = ImmersionBar.getStatusBarHeight(activity) }
		}
		updateTitle(title)
		val ivBack = rootView.findViewById<ImageView>(R.id.iv_back)
		ivBack.setDebounceOnClickListener { onBack() }
	}

	open fun updateTitle(title: String) {
		mTvTitle?.text = title
		mTvTitle?.visibility = if (title.isEmpty()) View.GONE else View.VISIBLE
	}

	open fun onBack() {
		activity.finish()
	}
}