package com.volcengine.effectone.auto.templates.cutsame

import android.util.Log
import com.cutsame.solution.player.CutSamePlayer
import com.cutsame.solution.source.CutSameSource

object CutSameContext {
	private const val TAG = "CutSameContext"
	private val cutSameSourceMap = HashMap<String, CutSameSource>()
	private val cutSamePlayerMap = HashMap<String, CutSamePlayer>()

	fun addCutSameSource(cacheKey: String, cutSameSource: CutSameSource?) {
		Log.d(TAG, "addCutSameSource cacheKey: $cacheKey, cutSameSource: $cutSameSource")
		cutSameSource ?: return
		cutSameSourceMap[cacheKey] = cutSameSource
	}

	fun removeCutSameSource(cacheKey: String) {
		Log.d(TAG, "removeCutSameSource templateUrl: $cacheKey")
		cutSameSourceMap.remove(cacheKey)
	}

	fun getCutSameSource(cacheKey: String): CutSameSource? {
		Log.d(TAG, "getCutSameSource cacheKey: $cacheKey")
		return cutSameSourceMap[cacheKey]
	}

	fun addCutSamePlayer(cacheKey: String, cutSamePlayer: CutSamePlayer?) {
		Log.d(TAG, "addCutSameSource cacheKey: $cacheKey, cutSamePlayer: $cutSamePlayer")
		cutSamePlayer ?: return
		cutSamePlayerMap[cacheKey] = cutSamePlayer
	}

	fun removeCutSamePlayer(cacheKey: String) {
		Log.d(TAG, "removeCutSamePlayer cacheKey: $cacheKey")
		cutSamePlayerMap.remove(cacheKey)
	}

	fun getCutSamePlayer(cacheKey: String): CutSamePlayer? {
		Log.d(TAG, "getCutSamePlayer cacheKey: $cacheKey")
		return cutSamePlayerMap[cacheKey]
	}

	fun release() {
		cutSamePlayerMap.clear()
		cutSameSourceMap.clear()
	}
}