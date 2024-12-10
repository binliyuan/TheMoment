package com.volcengine.effectone.auto.templates.helper

import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.volcengine.effectone.auto.templates.vm.CutSameViewModel
import com.volcengine.effectone.auto.templates.vm.TemplatesSelectViewModel

/**
 * @author tyx
 * @description:
 * @date :2024/4/29 17:18
 */
class CutSamePageTitleHelper(
	override val activity: FragmentActivity,
	override val owner: LifecycleOwner,
	private val mHasSwitch: Boolean
) : PageTitleHelper(activity, owner, fitsSystemWindows = true) {

	private val mTemplateSelectViewModel by lazy { TemplatesSelectViewModel.get(activity) }
	private val mCutSameViewModel by lazy { CutSameViewModel.get(activity) }

	private val mBackPressedCallback by lazy {
		object : OnBackPressedCallback(false) {
			override fun handleOnBackPressed() {
				mTemplateSelectViewModel.mIsShowTemplatePanel.value = true
			}
		}
	}

	init {
		activity.onBackPressedDispatcher.addCallback(owner, mBackPressedCallback)
	}

	override fun initView(rootView: ViewGroup) {
		super.initView(rootView)
		startObserver()
	}

	private fun startObserver() {
		mCutSameViewModel.mPrepareLiveData.observe(activity) { result ->
			result ?: return@observe
			updateTitle(result.resultData?.getOrNull()?.templateItem?.title ?: "")
		}
		mTemplateSelectViewModel.mIsShowTemplatePanel.observe(activity) { isShow ->
			isShow ?: return@observe
			mBackPressedCallback.isEnabled = mHasSwitch && !isShow
			val title = if (isShow) mCutSameViewModel.mCurrentTemplateItem?.title ?: ""
			else "编辑视频"
			updateTitle(title)
		}
	}

	override fun onBack() {
		activity.onBackPressed()
	}

	@OnLifecycleEvent(value = Lifecycle.Event.ON_DESTROY)
	fun onDestroy() {
		mBackPressedCallback.remove()
	}
}