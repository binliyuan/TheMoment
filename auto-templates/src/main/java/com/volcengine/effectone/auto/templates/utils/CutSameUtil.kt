package com.volcengine.effectone.auto.templates.utils

import com.ss.android.ugc.cut_ui.MediaItem

/**
 * @author tyx
 * @description:
 * @date :2024/5/8 17:51
 */
object CutSameUtil {

	fun findIndexFromPlayerProgress(progress: Long, list: List<MediaItem>): Int {
		//二分查找
		var left = 0
		var right = list.size - 1
		var targetIndex = -1
		while (left <= right) {
			val mid = left + (right - left) / 2
			val startTime = list[mid].targetStartTime
			val end = startTime + list[mid].duration
			if (progress in startTime until end) {
				targetIndex = mid
				break
			} else if (startTime < progress) {
				left = mid + 1
			} else {
				right = mid - 1
			}
		}
		//区间重叠，没找到则最后一个
		if (targetIndex == -1) {
			if (right >= 0) {
				val startTime = list[right].targetStartTime
				val end = startTime + list[right].duration
				if (progress > end) {
					targetIndex = right
				}
			}
		}
		return minOf(maxOf(0, targetIndex), list.size - 1)
	}

	/**
	 * 仅以startTime.因发现存在下一个start比当前end要小
	 */
	fun findIndexFromPlayerProgressV2(progress: Long, list: List<MediaItem>): Int {
		return list.withIndex()
			.findLast {
				progress >= it.value.targetStartTime
			}
			?.index
			?.coerceIn(0, list.size - 1) ?: 0
	}

	fun findIndexFromPlayerProgressV3(progress: Long, list: List<MediaItem>): Int {
		return list.withIndex()
			.firstOrNull {
				val item = it.value
				progress >= item.targetStartTime && (progress < item.targetStartTime + item.duration || it.index == list.lastIndex)
			}
			?.index
			?.coerceIn(0, list.size - 1) ?: 0
	}

	/**
	 * 找到下一条数据
	 */
	fun findNextItemFormMediaItems(mediaItem: MediaItem, list: List<MediaItem>): MediaItem? {
		val index = list.indexOfFirst { it.materialId == mediaItem.materialId }
		if (index != -1 && index + 1 < list.size) {
			return list[index + 1]
		}
		return null
	}
}