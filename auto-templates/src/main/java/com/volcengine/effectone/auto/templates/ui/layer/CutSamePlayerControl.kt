package com.volcengine.effectone.auto.templates.ui.layer

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.cutsame.solution.player.BasePlayer
import com.cutsame.solution.player.CutSamePlayer
import com.volcengine.effectone.auto.templates.vm.CutSamePlayerViewModel

/**
 * @author tyx
 * @description:
 * @date :2024/4/29 10:05
 */
class CutSamePlayerControl(
	private val activity: FragmentActivity,
	private val owner: LifecycleOwner
) : ICutSamePlayerControl, LifecycleObserver {

	private var mPlayer: CutSamePlayer? = null
	private val mCutSamePlayerViewModel by lazy { CutSamePlayerViewModel.get(activity) }

	fun setPlayer(mPlayer: CutSamePlayer?) {
		this.mPlayer = mPlayer
	}

	override fun onCheckEditText(check: Boolean) {
		mCutSamePlayerViewModel.mShowEditTextPanel.value = check
	}

	override fun onChangeMusic(check: Boolean) {
		mCutSamePlayerViewModel.mShowChangeMusic.value = check
	}

	override fun start() {
		mPlayer?.start()
	}

	override fun pause() {
		mPlayer?.pause()
	}

	override fun getDuration(): Long = mPlayer?.getDuration() ?: 0L

	override fun getPosition(): Long = mPlayer?.getCurrentPosition() ?: 0L

	override fun isPlaying(): Boolean = mPlayer?.getState() == BasePlayer.PlayState.PLAYING

	override fun seekTo(progress: Int, autoPlay: Boolean, callback: ((ret: Int) -> Unit)?) {
		mPlayer?.seekTo(progress, autoPlay, callback)
	}

	override fun seeking(value: Int) {
		mPlayer?.seeking(value)
	}

	@OnLifecycleEvent(value = Lifecycle.Event.ON_DESTROY)
	fun onDestroy() {
		release()
	}

	fun release() {
		mPlayer?.release()
		mPlayer = null
	}
}