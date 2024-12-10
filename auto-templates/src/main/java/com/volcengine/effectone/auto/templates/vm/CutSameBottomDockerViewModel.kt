package com.volcengine.effectone.auto.templates.vm

import android.app.Application
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Toast
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import com.bytedance.creativex.mediaimport.repository.api.IMaterialItem
import com.bytedance.creativex.mediaimport.repository.api.IMediaItem
import com.bytedance.creativex.mediaimport.repository.api.isImage
import com.bytedance.creativex.mediaimport.repository.api.isVideo
import com.bytedance.creativex.mediaimport.util.getMediaFileAbsolutePath
import com.cutsame.solution.template.model.TemplateItem
import com.ss.android.medialib.common.LogUtil
import com.ss.android.ugc.cut_ui.ItemCrop
import com.ss.android.ugc.cut_ui.MediaItem
import com.ss.android.ugc.tools.utils.toArrayList
import com.volcengine.effectone.auto.common.cutsame.CutSameContract
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.bean.CutSameMediaItem
import com.volcengine.effectone.singleton.AppSingleton
import java.util.Locale
import java.util.regex.Pattern


class CutSameBottomDockerViewModel(application: Application) : AndroidViewModel(application) {

	val processCutSamePickItem = ArrayList<CutSameMediaItem>() //picking slot data arrays
	val processPickItem = MutableLiveData<ArrayList<CutSameMediaItem>>() // slot/material info list (init)
	private val relationShipMap = mutableMapOf<Int, ArrayList<Triple<Int, String, Boolean>>>() //groupId to <index,materialId,hasValue>

	val currentPickIndex = MutableLiveData<Int>() // current picked slot index
	private val preProcessPickItem = MutableLiveData<ArrayList<MediaItem>>() // no usage

	private val addItem = MutableLiveData<CutSameMediaItem?>() //add item --->pickOne
	val deleteItem = MutableLiveData<CutSameMediaItem?>() // delete item
	val processPickMediaData = mutableMapOf<Int, CutSameMediaItem>()
	val pickFull = MutableLiveData<Boolean>()
	private var curMaterial: CutSameMediaItem? = null // current picked material (no usage)
	val multiReplaceSlotIndex = MutableLiveData<Pair<Int, IMaterialItem>>() //same multi slots info
	lateinit var templateItem: TemplateItem
	val launchComposePage = MutableLiveData<Pair<ArrayList<MediaItem>, TemplateItem>>()

	//region new method
	fun init(preMediaItems: ArrayList<MediaItem>?) {
		processCutSamePickItem.forEachIndexed { index, cutSameMediaItem ->
			cutSameMediaItem.mediaItem.apply {
				source = ""
				sourceStartTime = 0
				mediaSrcPath = ""
				crop = ItemCrop(0f, 0f, 1.0f, 1.0f)
				// parse group
				if (relationShipMap[getGroup()].isNullOrEmpty()) {
					relationShipMap[getGroup()] = ArrayList()
				}
				relationShipMap[getGroup()]!!.add(Triple(index, materialId, false))
			}

		}
		processPickItem.value = processCutSamePickItem
		preProcessPickItem.value = preMediaItems
		setSelected(0)
		pickFull.value = false
	}

	fun setSelected(pos: Int) {
		LogUtil.i(TAG, "setSelected  pos = ${pos}")
		if (pos == currentPickIndex.value) {
			return
		}
		currentPickIndex.value = pos
		processCutSamePickItem.forEachIndexed { index, cutSameMediaItem ->
			cutSameMediaItem.selected = index == pos
		}
		processPickItem.value?.let { list ->
			if (pos < list.size) {
				val mediaItem = list[pos]
				curMaterial = mediaItem
			}

		}
	}

	fun updateProcessPickItem(mediaItem: MediaItem) {
		Log.e(
			TAG,
			"updateProcessPickItem mediaItem = ${mediaItem.materialId} ${mediaItem.source}"
		)
		processPickItem.value?.let { mediaItems ->
			mediaItems.forEachIndexed { index, item ->
				if (item.mediaItem.materialId == mediaItem.materialId) {
					mediaItems[index].mediaItem = mediaItems[index].mediaItem.copy(
						sourceStartTime = mediaItem.sourceStartTime,
						crop = mediaItem.crop
					)
				}
			}
			processPickItem.value = mediaItems
		}
	}


	fun pickOne(mediaItem: IMaterialItem): Boolean {
		Log.i(TAG, "pickOne MediaData = $mediaItem")
		val index = currentPickIndex.value!!
		if (index >= (processPickItem.value?.size ?: 0)) {
			return false
		}
		processPickItem.value?.let { mediaItems ->
			val mediaItemIndex = mediaItems[index].mediaItem
			return if (mediaItem.isImage() || mediaItem.duration >= mediaItemIndex.duration) {//Image ignore check,video check duration
				if (notInGroup(mediaItemIndex)) { // index no relation ship ,group = 0
					//  group 0 replace one bye  one in index
					replaceMaterial(index, mediaItem, mediaItems)
					afterReplaceMaterial(mediaItems)
				} else { // groups
					judgeMultiRelationShips(mediaItems, index, mediaItemIndex, mediaItem)
				}
				true
			} else {
				false
			}
		}
		return false
	}

	private fun judgeMultiRelationShips(
		mediaItems: ArrayList<CutSameMediaItem>,
		index: Int,
		mediaItemIndex: MediaItem,
		mediaItem: IMaterialItem
	): Boolean {
		val multiRelationShipSize = getMultiRelationShipSize(mediaItems, index)
		// has multiRelationShip ,group >0,group count >1
		if (multiRelationShipSize > 1) {
			processMultiRelationShip(index, mediaItems, mediaItemIndex, mediaItem)
		} else if (multiRelationShipSize == 1) {
			// has multiRelationShip ,group >0,group count = 1,exists?
			replaceMaterial(index, mediaItem, mediaItems)
			afterReplaceMaterial(mediaItems)
		} else {
			//multiRelationShipSize == 0
			return false
		}
		return true
	}

	private fun processMultiRelationShip(
		index: Int,
		mediaItems: ArrayList<CutSameMediaItem>,
		mediaItemIndex: MediaItem,
		mediaItem: IMaterialItem
	) {
		val insertImage = mediaItem.isImage()

		if (insertImage) { //Image ignore check,video check duration
			if (needShowMultiReplaceDialog(mediaItems, index)) {
				multiReplaceSlotIndex.value = Pair(index, mediaItem)
				selectNext(mediaItems)
			} else {
				// others are empty, do multi-replace
				multiReplaceMaterial(index, mediaItem, mediaItems)
				afterReplaceMaterial(mediaItems)
			}
		} else {
			//  video  in groups replace one bye one in index
			replaceMaterial(index, mediaItem, mediaItems)
			afterReplaceMaterial(mediaItems)
		}
	}

	private fun getMultiRelationShipPartIndices(
		group: Int,
		mediaItems: ArrayList<CutSameMediaItem>,
		duration: Long
	): List<Int> {
		val partConformRelationShipItems = mutableListOf<Int>()
		return relationShipMap[group]?.filter {
			it.third.not() && mediaItems[it.first].mediaItem.duration <= duration
		}?.map { it.first } ?: partConformRelationShipItems
	}

	/**
	 * Replace the slot in [mediaItems] index [position] with MediaData [one]
	 */
	private fun replaceMaterial(position: Int, one: IMaterialItem, mediaItems: ArrayList<CutSameMediaItem>) {
		if (mediaItems[position].materialItem != null) {
//            deleteOne(position) //TODO 为何注释掉？
		}
		val mediaItem = mediaItems[position].mediaItem
		val index = relationShipMap[mediaItem.getGroup()]?.indexOfFirst {
			it.first == position
		}
		relationShipMap[mediaItem.getGroup()]?.let {
			if (index!! in it.indices) {
				it[index] = it[index].copy(third = true) // update data in object RelationVideoGroup
			}
		}
		val path = getMediaFileAbsolutePath(one as? IMediaItem).takeIf { it.isNotEmpty() } ?: one.path
		mediaItems[position].mediaItem = mediaItems[position].mediaItem.copy(
			source = path,
			mediaSrcPath = path,
			type = if (one.isVideo()) MediaItem.TYPE_VIDEO else MediaItem.TYPE_PHOTO,
			oriDuration = if (one.isVideo()) one.duration else mediaItems[position].mediaItem.duration
		)
		mediaItems[position].materialItem = one
		processPickMediaData[position] = mediaItems[position]  //将槽位下标作为key，替换元素
		addItem.value = mediaItems[position]
	}

	private fun afterReplaceMaterial(mediaItems: ArrayList<CutSameMediaItem>) {
		processPickItem.value = mediaItems

		pickFull.value = isFull(processPickItem.value)
		// notify
		selectNext(mediaItems)
	}

	/**
	 * Replace all the slot in the [mediaItems] [[one]]'s video group with material [one].
	 */
	fun multiReplaceMaterial(position: Int, one: IMaterialItem, mediaItems: ArrayList<CutSameMediaItem>) {
		repeat(mediaItems.size) {
			if (mediaItems[it].mediaItem.getGroup() == mediaItems[position].mediaItem.getGroup()) {
				replaceMaterial(it, one, mediaItems)
			}
		}
		afterReplaceMaterial(mediaItems)
	}

	fun singleReplaceMaterial(position: Int, one: IMaterialItem) {
		val mediaItems = processPickItem.value ?: return
		replaceMaterial(position, one, mediaItems)
		afterReplaceMaterial(mediaItems)
	}

	fun deleteOne(position: Int) {
		Log.e(TAG, "deleteOne position = $position")
		processPickItem.value?.let { mediaItems ->
			val delete = mediaItems[position]

			val index = relationShipMap[delete.mediaItem.getGroup()]?.indexOfFirst {
				it.first == position
			}
			relationShipMap[delete.mediaItem.getGroup()]?.let {
				if (index!! in it.indices) {
					it[index] = it[index].copy(third = false) // update data in object RelationVideoGroup
				}
			}
			mediaItems[position].mediaItem = delete.mediaItem.copy(source = "", mediaSrcPath = "")
			// TODO: 暂时放到外边
//            mediaItems[position].materialItem = null
			processPickItem.value = mediaItems
			deleteItem.value = delete
			processPickMediaData.remove(position)

			pickFull.value = isFull(processPickItem.value)

			selectNext(mediaItems)
		}
	}

	private fun selectNext(
		mediaItems: List<CutSameMediaItem>
	) {
		if (pickFull.value != true) {
			val nextPos = mediaItems.indexOfFirst {
				it.materialItem == null
			}
			if (nextPos in mediaItems.indices) {
				setSelected(nextPos)
			}
		}
	}

	fun isFull(mediaList: ArrayList<CutSameMediaItem>?): Boolean {
		return mediaList?.none { it.materialItem == null } == true
	}

	// 支持槽位>2 就预览合成：如果选择了一个素材，也可以下一步
	fun isFull2(): Boolean {
		val mediaList = processPickItem.value
		// 必须满足第一个不为空
		if (mediaList.isNullOrEmpty() || mediaList.firstOrNull()?.materialItem == null) {
			return false
		}
		var hasEmpty = false
		// 如果选择的素材不连续 就不能下一步
		for (i in 1 until mediaList.size) { // 从第二个 MediaItem 开始遍历
			if (mediaList[i].materialItem == null) { // 如果当前 MediaItem 的 path 为空或不是上一个 MediaItem 的 path 的下一个，则返回 false
				hasEmpty = true
			} else {
				if (hasEmpty) return false
			}

		}
		return true // 如果所有 MediaItem 的 path 都符合要求，则返回 true
	}
	//endregion

	// 判断是否开启了测试按钮
	fun isDebug(): Boolean {
		val sp = PreferenceManager.getDefaultSharedPreferences(getApplication())
		return sp.getBoolean("cut_same_interface_test", false)
	}

	fun notInGroup(data: MediaItem): Boolean {
		return data.getGroup() == 0
	}

	private fun needShowMultiReplaceDialog(mediaItems: ArrayList<CutSameMediaItem>, index: Int): Boolean {
		return relationShipMap[mediaItems[index].mediaItem.getGroup()]?.firstOrNull { it.third } != null
	}

	private fun getMultiRelationShipSize(mediaItems: ArrayList<CutSameMediaItem>, index: Int): Int {
		return relationShipMap[mediaItems[index].mediaItem.getGroup()]?.size ?: 0
	}

	fun checkIsFull(): Boolean = pickFull.value == true
	fun getCutSamePickItem(): List<CutSameMediaItem> = processCutSamePickItem
	fun viewConfirmAction(activity: FragmentActivity) {
		val isDebugPickFull = isDebug() &&
				isFull2()
		if (pickFull.value == true || isDebugPickFull) {
			val items = processPickItem.value!!.map { it.mediaItem }.toArrayList()
			if (TextUtils.isEmpty(templateItem.zipPath) || items.isNullOrEmpty()) {
				Toast.makeText(AppSingleton.instance, R.string.cutsame_compose_need_template_url, Toast.LENGTH_SHORT).show()
				return
			}
			launchComposePage.value = items to templateItem
		}
	}

	fun updatePickTipView(activity: FragmentActivity): CharSequence {
		val totalSie = processCutSamePickItem.size
		var pickingSize = processPickMediaData.size
		if (pickingSize <= 0) {
			val text =
				return String.format(Locale.ENGLISH, activity.getString(R.string.eo_cutsame_pick_tips_default), totalSie)
		}
		pickingSize = if (pickingSize > totalSie) totalSie else pickingSize
		val template = activity.getString(R.string.eo_cutsame_pick_tips)
		val spannableBuilder = SpannableStringBuilder(template)
		val color = ContextCompat.getColor(activity, R.color.color_FF53BB)
		// 设置 "%1d" 的颜色
		val pattern1d = Pattern.compile("%1d")
		val matcher1 = pattern1d.matcher(template)
		while (matcher1.find()) {
			val start = matcher1.start()
			val end = matcher1.end()
			val colorSpan = ForegroundColorSpan(color)
			spannableBuilder.setSpan(
				colorSpan,
				start,
				end,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
			)
			spannableBuilder.replace(start, end, pickingSize.toString())
		}

		// 设置 "%2d" 的颜色
		val pattern2d = Pattern.compile("%2d")
		val matcher2 = pattern2d.matcher(spannableBuilder)
		while (matcher2.find()) {
			val start = matcher2.start()
			val end = matcher2.end()
			val colorSpan = ForegroundColorSpan(color)
			spannableBuilder.setSpan(
				colorSpan,
				start,
				end,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
			)
			spannableBuilder.replace(start, end, pickingSize.toString())
		}

		// 设置 "%2d" 的颜色
		val pattern3d = Pattern.compile("%3d")
		val matcher3 = pattern3d.matcher(spannableBuilder)
		while (matcher3.find()) {
			val start = matcher3.start()
			val end = matcher3.end()
			spannableBuilder.replace(start, end, totalSie.toString())
		}
		return spannableBuilder
	}

	companion object {
		const val TAG = "BottomDockerViewModel"
		private val labelColorArray = intArrayOf(
			Color.parseColor("#514EFB"), Color.parseColor("#E9BA15"),
			Color.parseColor("#FF453A"), Color.parseColor("#0084FF"),
			Color.parseColor("#0ACE82"), Color.parseColor("#FE7F2E"),
			Color.parseColor("#3A37B2"), Color.parseColor("#A5840F"),
			Color.parseColor("#B53129"), Color.parseColor("#005EB5"),
			Color.parseColor("#07925C"), Color.parseColor("#B45A21"),
			Color.parseColor("#8A88FC"), Color.parseColor("#F0D162"),
			Color.parseColor("#FF827B"), Color.parseColor("#54ADFF"),
			Color.parseColor("#5BDEAB"), Color.parseColor("#FEA973")
		)

		fun get(activity: FragmentActivity): CutSameBottomDockerViewModel {
			return ViewModelProvider(activity, AndroidViewModelFactory.getInstance(activity.application)).get(
				CutSameBottomDockerViewModel::class.java
			)
		}

		fun getGalleryPickData(savedInstanceState: Bundle?, activity: FragmentActivity): Pair<TemplateItem, List<CutSameMediaItem>>? {
			if (savedInstanceState != null) return null
			activity.intent?.apply {
				val templateItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
					getParcelableExtra(CutSameContract.ARG_TEMPLATE_ITEM, TemplateItem::class.java)
				} else {
					getParcelableExtra<TemplateItem>(CutSameContract.ARG_TEMPLATE_ITEM)
				} ?: return null

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
					getParcelableArrayListExtra(CutSameContract.ARG_DATA_PICK_MEDIA_ITEMS, MediaItem::class.java)
				} else {
					getParcelableArrayListExtra<MediaItem>(CutSameContract.ARG_DATA_PICK_MEDIA_ITEMS)
				}?.also {
					return templateItem to it.mapIndexed { index, mediaItem ->
						CutSameMediaItem(mediaItem).apply {
							selected = index == 0
						}
					}
				}
			}

			return null
		}

		fun getIdColor(@IntRange(from = 1) groupId: Int): Int {
			if (groupId == 0) {
				return getThemeColor()
			}
			return labelColorArray[(groupId - 1) % labelColorArray.size]
		}

		fun getThemeColor(): Int {
			return labelColorArray[0]
		}
	}
}

