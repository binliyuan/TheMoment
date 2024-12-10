package com.volcengine.effectone.auto.templates.ui

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.creativex.mediaimport.repository.api.IMaterialItem
import com.bytedance.creativex.mediaimport.repository.api.MaterialCategoryType
import com.ss.android.medialib.common.LogUtil
import com.ss.android.ugc.cut_ui.MediaItem
import com.ss.android.ugc.tools.view.activity.AVActivityOnKeyDownListener
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.adapter.CutSameBottomDockerPickingListAdapter
import com.volcengine.effectone.auto.templates.helper.CutSameBottomDockerHelper
import com.volcengine.effectone.auto.templates.launch.CutSameComposeLauncher
import com.volcengine.effectone.auto.templates.vm.CutSameAlbumViewModel
import com.volcengine.effectone.auto.templates.vm.CutSameBottomDockerViewModel
import com.volcengine.effectone.auto.templates.vm.CutSameBottomDockerViewModel.Companion.get
import com.volcengine.effectone.auto.templates.widget.CenterScrollLinearLayoutManager
import com.volcengine.effectone.auto.templates.widget.CutSameReplaceNoticeDialog
import com.volcengine.effectone.utils.SizeUtil

class CutSameBottomDockerFragment : Fragment(), AVActivityOnKeyDownListener {
	companion object {
		const val TAG = "CutSameBottomDocker"
		const val REQUEST_COMPRESS = 1000
		const val REQUEST_CODE_CLIP = 1001 // clip material exp: clip/record
		const val REQUEST_CODE_TEMPLATE_PREVIEW = 1002 // template preview
		const val ARG_DATA_PRE_PICK_RESULT_MEDIA_ITEMS =
			"arg_data_pre_pick_result_media_items"
	}

	private val cutSameViewModel by lazy { CutSameAlbumViewModel.get(requireActivity()) }

	private val cutSameBottomDockerViewModel by lazy { get(requireActivity()) }
	private val cutSameBottomDockerHelpers by lazy {
		mutableListOf(
			CutSameBottomDockerHelper(requireActivity(), viewLifecycleOwner)
		)
	}

	private val mCutSameComposeLauncher by lazy { CutSameComposeLauncher(requireActivity()) }


	private lateinit var pickingRecyclerView: RecyclerView

	private lateinit var pickingListAdapter: CutSameBottomDockerPickingListAdapter
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		CutSameBottomDockerViewModel.getGalleryPickData(savedInstanceState, requireActivity())
			?.also {
				cutSameBottomDockerViewModel.templateItem = it.first
				cutSameBottomDockerViewModel.processCutSamePickItem.addAll(it.second)
			} ?: run {
			Log.e(TAG, "no data ,finish now ")
			requireActivity().finish()
		}
		val prePickItems =
			arguments?.getParcelableArrayList<MediaItem>(ARG_DATA_PRE_PICK_RESULT_MEDIA_ITEMS)
		LogUtil.d(TAG, "initView prePickItems=${prePickItems?.size}")
		cutSameBottomDockerViewModel.init(prePickItems)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.cutsame_fragment_bottom_docker, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		cutSameBottomDockerHelpers.forEach {
			viewLifecycleOwner.lifecycle.addObserver(it)
		}
		viewLifecycleOwner.lifecycle.addObserver(mCutSameComposeLauncher)
		super.onViewCreated(view, savedInstanceState)
		cutSameBottomDockerHelpers.forEach {
			it.initView(view as ViewGroup)
		}
		initView(view)
		initListener()
		initComponent()
	}

	fun initView(rootView: View) {
		pickingRecyclerView = rootView.findViewById(R.id.pickingRecyclerView)
		//picking
		pickingListAdapter =
			CutSameBottomDockerPickingListAdapter(cutSameBottomDockerViewModel, this)
		pickingRecyclerView.apply {
			layoutManager = CenterScrollLinearLayoutManager(
				requireActivity(),
				LinearLayoutManager.HORIZONTAL,
				false
			)
			setHasFixedSize(true)
			adapter = pickingListAdapter
			addItemDecoration(object : RecyclerView.ItemDecoration() {
				override fun getItemOffsets(
					outRect: Rect,
					view: View,
					parent: RecyclerView,
					state: RecyclerView.State
				) {
					super.getItemOffsets(outRect, view, parent, state)
					outRect.right = SizeUtil.dp2px(12f)
				}
			})
		}
	}

	private fun initListener() {
		pickingListAdapter.deleteBlock = {
			cutSameBottomDockerViewModel.deleteOne(it)
		}
		pickingListAdapter.itemClickBlock = label@{ position, empty ->
			if (empty) {
				cutSameBottomDockerViewModel.setSelected(position)
				return@label
			}
			val mediaItem =
				cutSameBottomDockerViewModel.processPickItem.value?.get(position)?.mediaItem
					?: return@label
			val materialItem =
				cutSameBottomDockerViewModel.processPickItem.value?.get(position)?.materialItem
					?: return@label
			if (mediaItem.type == MediaItem.TYPE_VIDEO || mediaItem.alignMode == MediaItem.ALIGN_MODE_VIDEO) {
				// go to clip activity
				mCutSameComposeLauncher.launchClip(mediaItem)
			} else {
				// go to single material preview page
				val processPickMediaData =
					cutSameBottomDockerViewModel.processPickMediaData.filter { it.value.materialItem != null }.map {
						it.value.materialItem!!
					}
				val realIndex = processPickMediaData.indexOf(materialItem)
				val enterFrom = materialItem.sourceType as? MaterialCategoryType
				cutSameViewModel.showPreview(
					materialItem,
					processPickMediaData,
					realIndex,
					enterFrom
				)
			}
		}
	}

	private fun initComponent() {
		cutSameBottomDockerViewModel.currentPickIndex.observe(
			viewLifecycleOwner
		) { index ->
			index?.apply {
				pickingRecyclerView.smoothScrollToPosition(index)
			}
		}

		cutSameBottomDockerViewModel.multiReplaceSlotIndex.observe(viewLifecycleOwner) {
			showReplaceDialog(it.first, it.second)
		}

		cutSameBottomDockerViewModel.deleteItem.observe(viewLifecycleOwner) { item ->
			if (item == null) {
				return@observe
			}
			cutSameBottomDockerViewModel.processCutSamePickItem.firstOrNull { it.mediaItem.materialId == item.mediaItem.materialId }
				?.let { cutSameItem ->
					cutSameViewModel.mediaSelectViewModel?.mediaSelectorViewModel?.takeIf { cutSameItem.materialItem != null }
						?.cancel(cutSameItem.materialItem!!)
					cutSameItem.materialItem = null
				}

		}
		cutSameViewModel.observerSubscribe()
		cutSameBottomDockerViewModel.launchComposePage.observe(viewLifecycleOwner) {
			mCutSameComposeLauncher.launchCompose(it)
		}
	}

	private fun showReplaceDialog(position: Int, one: IMaterialItem) {
		CutSameReplaceNoticeDialog.Builder(requireActivity())
			.setDialogOperationListener(object : CutSameReplaceNoticeDialog.DialogOperationListener {
				override fun onClickSure() {
					cutSameBottomDockerViewModel.singleReplaceMaterial(position, one)
				}

				override fun onClickCancel() {
					val mediaItems = cutSameBottomDockerViewModel.processPickItem.value ?: return
					cutSameBottomDockerViewModel.multiReplaceMaterial(position, one, mediaItems)
				}
			})
			.setTitleText(this.getString(R.string.eo_cutsame_replace_dialog_title))
			.setSubTitleText(this.getString(R.string.eo_cutsame_replace_dialog_subtitle))
			.setCancelText(this.getString(R.string.eo_cutsame_replace_dialog_confirm))
			.setConfirmText(this.getString(R.string.eo_cutsame_replace_dialog_cancel))
			.create()
			.show()
	}

	override fun onKeyDown(action: Int, keyEvent: KeyEvent): Boolean {
		return cutSameViewModel.onKeyDown(keyEvent)
	}
}