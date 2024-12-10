package com.volcengine.effectone.auto.templates.helper

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.ss.android.ugc.cut_log.LogUtil
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.music.MusicItem
import com.volcengine.effectone.auto.templates.music.MusicListAdapter
import com.volcengine.effectone.auto.templates.music.MusicResp
import com.volcengine.effectone.auto.templates.music.MusicUtils
import com.volcengine.effectone.auto.templates.music.filePath
import com.volcengine.effectone.auto.templates.vm.CutSamePlayerViewModel
import com.volcengine.effectone.auto.templates.vm.CutSameViewModel
import com.volcengine.effectone.auto.templates.widget.RvScrollbarItemDecoration
import com.volcengine.effectone.utils.FileUtil
import com.volcengine.effectone.utils.SizeUtil
import com.volcengine.effectone.widget.popover.DuxPopover
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

/**
 *Author: gaojin
 *Time: 2024/5/7 15:36
 */

@SuppressLint("InflateParams")
class ChangeMusicHelper(
	override val activity: FragmentActivity,
	override val owner: LifecycleOwner
) : IUIHelper {

	companion object {
		private const val TAG = "ChangeMusicHelper"
	}

	private val mCutSameViewModel by lazy { CutSameViewModel.get(activity) }
	private val mCutSamePlayerViewModel by lazy { CutSamePlayerViewModel.get(activity) }
	private lateinit var mRootView: View

	private val mPopOverRootView by lazy {
		val rootView = LayoutInflater.from(activity).inflate(R.layout.layout_change_music_pop_container, null, false)
		initPopRootView(rootView)
		rootView
	}

	private val mPopover by lazy {
		DuxPopover.Builder(activity)
			.setUseDefaultView(false)
			.setNeedArrow(false)
			.setCornerRadius(SizeUtil.dp2px(6F).toFloat())
			.setBgColor(ContextCompat.getColor(activity, R.color.color_232530))
			.setBorderWidth(1)
			.setBorderColor(ContextCompat.getColor(activity, R.color.color_14_FFF))
			.setShowElevationShadow(true)
			.setAutoDismissDelayMillis(Long.MIN_VALUE)
			.setView(mPopOverRootView)
			.setGravity(Gravity.BOTTOM)
			.build()
	}

	private var musicListAdapter: MusicListAdapter? = null

	override fun initView(rootView: ViewGroup) {
		mRootView = rootView
		mPopover.setOnDismissListener {
			musicListAdapter?.previewItem(-1)
			mCutSamePlayerViewModel.mShowChangeMusic.value = false
		}
		startObserver()
	}

	private fun initPopRootView(rootView: View) {
		mCutSameViewModel.mScope.launch(Dispatchers.Main) {
			val loadingView = rootView.findViewById<View>(R.id.music_loading_view)
			val isResReady = MusicUtils.musicResIsReady()
			if (!isResReady) {
				withContext(Dispatchers.IO) {
					MusicUtils.copyMusicRes()
				}
			}
			loadingView.visibility = View.GONE
			val recyclerView = rootView.findViewById<RecyclerView>(R.id.pop_music_list)
			musicListAdapter = MusicListAdapter()
			recyclerView.apply {
				adapter = musicListAdapter
				setHasFixedSize(true)
				layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
				addItemDecoration(RvScrollbarItemDecoration())
			}
			musicListAdapter?.updateItems(getMusicList())

			musicListAdapter?.clickAction = { view, item ->
				//执行切换音乐逻辑
				val audioManager = mCutSamePlayerViewModel.getAudioManager()
				audioManager?.let { cutSameAudio ->
					mCutSamePlayerViewModel.pause()
					if (item.id == MusicItem.DEFAULT_ID) {
						cutSameAudio.resetAudio()
					} else {
						cutSameAudio.setCustomAudio(item.filePath(), item.name, 0)
					}
					mCutSamePlayerViewModel.seekTo(0, true)
					dismiss()
				}
			}
		}
	}

	private fun getMusicList(): MutableList<MusicItem> {
		val duration = mCutSamePlayerViewModel.getAudioManager()?.getTemplateAudioDuration()
		val firstItem = MusicItem(
			id = MusicItem.DEFAULT_ID,
			name = "模板自配歌曲",
			icon = "default",
			//TODO这里设置模板音乐时长
			duration = duration ?: 0
		).apply {
			isSelected = true
		}
		val bean = Gson().fromJson(FileUtil.readJsonFromAssets(activity, "music/music.json"), MusicResp::class.java)
		return mutableListOf<MusicItem>().apply {
			add(firstItem)
			bean.resource?.list?.let {
				addAll(it)
			}
		}
	}

	private fun startObserver() {
		mCutSamePlayerViewModel.mShowChangeMusic.observe(activity) { result ->
			LogUtil.d(TAG, "showChangeMusic:$result")
			if (result) show() else dismiss()
		}
		mCutSamePlayerViewModel.mPlayerState.observe(activity) { result ->
			if (result == CutSamePlayerViewModel.PREPARED) {
				musicListAdapter?.let { adapter ->
					adapter.selectItem(-1)
					//切换模板
					val audioManager = mCutSamePlayerViewModel.getAudioManager()
					audioManager?.let { manager ->
						val duration = manager.getTemplateAudioDuration()
						val list = ArrayList(adapter.mMusicItems)
						if (list.isNotEmpty()) {
							list[0] = list.first().copy(duration = duration)
						}
						list.forEach {
							it.isPreview = false
							it.isSelected = it.id == MusicItem.DEFAULT_ID
						}
						adapter.updateItems(list)
					}
				}
			}
		}
	}

	@SuppressLint("RtlHardcoded")
	fun show() {
		if (mPopover.isShowing) return
		//这里找到子view 播放器CutSamePlayerEditLayer的按钮
		val view = mRootView.findViewById<View>(R.id.layout_change_music)
		val xOffset = SizeUtil.dp2px(10f)
		val popHeight = mPopover.getMeasuredHeight()
		//计算pop显示在距底部23dp的位置
		val yOffset = -(abs(popHeight - (SizeUtil.getScreenHeight(activity) - view.top)) + SizeUtil.dp2px(23f))
		mPopover.show(view, Gravity.RIGHT, false, 0f, xOffset, yOffset)
	}

	fun dismiss() {
		if (mPopover.isShowing) mPopover.dismiss()
	}
}