package com.volcengine.effectone.auto.templates.helper

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.ies.cutsame.util.SizeUtil
import com.ss.android.ugc.aweme.views.setDebounceOnClickListener
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.adapter.TemplatesSelectAdapter
import com.volcengine.effectone.auto.templates.bean.TemplatesByMedias
import com.volcengine.effectone.auto.templates.ui.AutoCutSameExportActivity
import com.volcengine.effectone.auto.templates.vm.CutSamePlayerViewModel
import com.volcengine.effectone.auto.templates.vm.CutSameViewModel
import com.volcengine.effectone.auto.templates.vm.TemplatesSelectViewModel

/**
 * @author tyx
 * @description:
 * @date :2024/4/26 17:12
 */
class CutSameTemplatesSelectHelper(
	override val activity: FragmentActivity,
	override val owner: LifecycleOwner,
) : IUIHelper {

	private val mTemplatesSelectViewModel by lazy { TemplatesSelectViewModel.get(activity) }
	private val mCutSameViewModel by lazy { CutSameViewModel.get(activity) }
	private val mCutSamePlayerViewModel by lazy { CutSamePlayerViewModel.get(activity) }
	private lateinit var mRv: RecyclerView
	private lateinit var mTvExPort: TextView
	private lateinit var mTvEdit: TextView
	private lateinit var mTemplatesAdapter: TemplatesSelectAdapter
	private var mSwitching = false

	override fun initView(rootView: ViewGroup) {
		val contentLayout = rootView.findViewById<FrameLayout>(R.id.templates_select_layout)
		val inf = LayoutInflater.from(activity)
		val contentView = inf.inflate(R.layout.layout_templates_select, contentLayout, false)
		contentLayout.addView(contentView)
		initView(contentView)
		initRecyclerView()
		startObserver()
	}

	private fun initView(view: View) {
		mRv = view.findViewById(R.id.temp_rv)
		mTvExPort = view.findViewById(R.id.tv_export)
		mTvEdit = view.findViewById(R.id.tv_edit)
		mTvExPort.setDebounceOnClickListener {
			mCutSamePlayerViewModel.pause()
			val key = mCutSameViewModel.mCurrentTemplateItem?.zipPath
			if (key.isNullOrEmpty()) return@setDebounceOnClickListener
			val intent = AutoCutSameExportActivity.createIntent(activity, key)
			activity.startActivity(intent)
		}
		mTvEdit.setDebounceOnClickListener {
			mTemplatesSelectViewModel.mIsShowTemplatePanel.value = false
		}
	}

	private fun initRecyclerView() {
		mTemplatesAdapter = TemplatesSelectAdapter()
		mRv.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
		mRv.adapter = mTemplatesAdapter
		mRv.addItemDecoration(object : RecyclerView.ItemDecoration() {
			override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
				super.getItemOffsets(outRect, view, parent, state)
				outRect.bottom = SizeUtil.dp2px(6f)
			}
		})
		val templatesByMedias = mCutSameViewModel.mTemplatesByMedias
		val selectIndex = mCutSameViewModel.mFirstSelectIndex
		val list = templatesByMedias.mapIndexed { index, item ->
			TemplatesByMedias(index == selectIndex, item.templateItem, item.mediaList)
		}
		mTemplatesAdapter.updateItems(list)
		mTemplatesAdapter.clickBlock = { _, pos, data ->
			if (!data.select && !mSwitching) {
				mSwitching = true
				mTemplatesSelectViewModel.mStartSwitchTemplate.value = data.data
			}
		}
	}

	private fun startObserver() {
		mCutSameViewModel.mPlayerDataReady.observe(activity) { result ->
			result ?: return@observe
			mSwitching = false
			val list = mTemplatesAdapter.mTemplatesList.map {
				it.copy(select = it.data.title == mCutSameViewModel.mCurrentTemplateItem?.title)
			}
			mTemplatesAdapter.updateItems(list)
		}
	}
}