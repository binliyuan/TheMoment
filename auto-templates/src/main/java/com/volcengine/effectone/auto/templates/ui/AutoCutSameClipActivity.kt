package com.volcengine.effectone.auto.templates.ui

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.view.SurfaceView
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.bytedance.ies.cutsame.util.SizeUtil
import com.cutsame.solution.CutSameSolution
import com.cutsame.solution.player.BasePlayer
import com.cutsame.solution.player.MediaPlayer
import com.cutsame.solution.player.PlayerStateListener
import com.gyf.immersionbar.ImmersionBar
import com.gyf.immersionbar.ktx.immersionBar
import com.ss.android.ugc.aweme.views.setDebounceOnClickListener
import com.ss.android.ugc.cut_log.LogUtil
import com.ss.android.ugc.cut_ui.MediaItem
import com.ss.ugc.android.editor.track.utils.visible
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.widget.TipsDialog
import com.volcengine.effectone.auto.templates.widget.clip.ClipSizeView
import com.volcengine.effectone.auto.templates.widget.clip.IOnClipSizeListener
import com.volcengine.effectone.auto.templates.widget.clip.videoRange.VideoClipRangeListener
import com.volcengine.effectone.auto.templates.widget.clip.videoRange.VideoClipRangeView
import com.volcengine.effectone.auto.templates.widget.player.IWrapPlayerStateListener

/**
 * @author tyx
 * @description:裁剪
 * @date :2024/5/10 15:33
 */
class AutoCutSameClipActivity : AppCompatActivity(), IWrapPlayerStateListener, VideoClipRangeListener {
	companion object {
		private const val TAG = "AutoCutSameClipActivity"
		private const val MEDIA_ITEM = "mediaItem"
		const val ARG_DATA_CLIP_MEDIA_ITEM = "arg_data_clip_media_item"
		fun createClipIntent(context: Context, mediaItem: MediaItem?): Intent {
			val intent = Intent(context, AutoCutSameClipActivity::class.java)
			intent.putExtra(MEDIA_ITEM, mediaItem)
			return intent
		}
	}

	private lateinit var mOriMediaItem: MediaItem
	private lateinit var mMediaItem: MediaItem
	private var mPlayer: MediaPlayer? = null
	private lateinit var mIvBack: ImageView
	private lateinit var mTvTitle: TextView
	private lateinit var mBtnConfirm: Button
	private lateinit var mBtnCancel: Button
	private lateinit var mBtnTitleConfirm: Button
	private lateinit var mLyBottomContainer: ConstraintLayout
	private lateinit var mSurfaceView: SurfaceView
	private lateinit var mClipRangeView: VideoClipRangeView   //视频时长裁剪view
	private lateinit var mClipSizeView: ClipSizeView
	private val mCanvasSize by lazy { Point() }
	private var isTimeClipChanged = false
	private var isAreaClipChanged = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		immersionBar {
			transparentStatusBar()
			navigationBarColor(com.gyf.immersionbar.R.color.abc_decor_view_status_guard)
			statusBarDarkFont(false)
		}
		setContentView(R.layout.activity_cut_same_clip)
		val mediaItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			intent.getParcelableExtra(MEDIA_ITEM, MediaItem::class.java)
		} else intent.getParcelableExtra(MEDIA_ITEM)
		mediaItem?.let {
			mOriMediaItem = it.copy()
			mMediaItem = it
			initView()
			initData()
		} ?: finish()
	}

	private fun initView() {
		mBtnConfirm = findViewById(R.id.btn_confirm)
		mBtnCancel = findViewById(R.id.btn_cancel)
		mLyBottomContainer = findViewById(R.id.ly_bottom_container)
		mSurfaceView = findViewById(R.id.surface_view)
		mClipRangeView = findViewById(R.id.video_clip_range_view)
		mClipSizeView = findViewById(R.id.clip_size_View)
		mBtnTitleConfirm = findViewById(R.id.btn_title_confirm)
		mBtnTitleConfirm.visible = !mMediaItem.isVideo()
		val titleLayout = findViewById<ConstraintLayout>(R.id.title_layout)
		mIvBack = titleLayout.findViewById(R.id.iv_back)
		mTvTitle = titleLayout.findViewById(R.id.tv_title)
		mTvTitle.text = if (mMediaItem.isVideo()) "裁剪视频" else "裁剪图片"
		val lp = titleLayout.layoutParams as MarginLayoutParams
		titleLayout.layoutParams = lp.apply { topMargin = ImmersionBar.getStatusBarHeight(this@AutoCutSameClipActivity) }
		mIvBack.setDebounceOnClickListener { onBackPressed() }
		mBtnConfirm.setDebounceOnClickListener { confirmClip() }
		mBtnCancel.setDebounceOnClickListener { onBackPressed() }
		mBtnTitleConfirm.setDebounceOnClickListener { confirmClip() }
		initClipView()
	}

	private fun initClipView() {
		mLyBottomContainer.visible = mMediaItem.isVideo()
		mClipRangeView.visible = mMediaItem.isVideo()
		if (mMediaItem.isVideo()) {
			mClipRangeView.setOnVideoClipListener(this)
			mClipRangeView.initMediaItem(mMediaItem)
		}
	}

	private fun initData() {
		val screenWidth = SizeUtil.getScreenWidth(this)
		val screenHeight = SizeUtil.getScreenHeight(this)
		LogUtil.d(TAG, "screenWidth:$screenWidth,screenHeight:$screenHeight")
		mCanvasSize.set(screenWidth, screenHeight)
		initPlayer(mCanvasSize)
	}

	private fun initPlayer(canvasSize: Point) {
		mPlayer = CutSameSolution.createMediaPlayer(mSurfaceView)
		mPlayer?.preparePlayBySingleMedia(mMediaItem, canvasSize, this)
	}

	override fun onChanged(state: Int) {
		super.onChanged(state)
		runOnUiThread {
			when (state) {
				PlayerStateListener.PLAYER_STATE_PREPARED -> {
					mPlayer?.seekTo(mMediaItem.sourceStartTime.toInt(), true)
					initClip()
				}
			}
		}
	}

	private fun initClip() {
		if (mMediaItem.alignMode == MediaItem.ALIGN_MODE_CANVAS) {
			mClipSizeView.visible = false
			return
		}
		mClipSizeView.setOnClipSizeListener(object : IOnClipSizeListener {
			var isTouchMoved = false
			var isCurPlaying = mPlayer?.getState() == BasePlayer.PlayState.PLAYING
			override fun onMove(isTouch: Boolean, scale: Float, translateX: Float, translateY: Float) {
				isTouchMoved = isTouch || isTouchMoved
				if (isTouchMoved) {
					isAreaClipChanged = true
				}
				moveVideo(scale, translateX, translateY)
			}

			override fun onDown() {
				isTouchMoved = false
				isCurPlaying = mPlayer?.getState() == BasePlayer.PlayState.PLAYING
				if (isCurPlaying) mPlayer?.pause()
			}

			override fun onUp() {
				if (isCurPlaying) {
					if (isTouchMoved) mPlayer?.start()
				} else {
					if (!isTouchMoved) mPlayer?.start()
				}
			}
		})
		mClipSizeView.updateData(mCanvasSize, mMediaItem)
	}

	private fun moveVideo(scale: Float, transX: Float, transY: Float) {
		LogUtil.d(TAG, "scale:$scale,transX:$transX,transY:$transY")
		mPlayer?.updateVideoTransform(1.0F, scale, 0F, transX, transY, false, "")
		mPlayer?.refreshCurrentFrame()
	}

	override fun onPlayProgress(process: Long) {
		super.onPlayProgress(process)
		runOnUiThread {
			val startTime = mMediaItem.sourceStartTime
			val endTime = startTime + mMediaItem.duration
			val progress = (process - startTime) / 1f / (endTime - startTime)
			LogUtil.d(TAG, "startTime:$startTime,endTime:$endTime,process:$process,progress:$progress")
			mClipRangeView.onPlayProgress(progress)
			if (process >= endTime) {
				mPlayer?.seekTo(startTime.toInt(), true)
			}
		}
	}

	override fun onScroll(startTime: Int) {
		super.onScroll(startTime)
		runOnUiThread {
			isTimeClipChanged = true
			mMediaItem.sourceStartTime = startTime.toLong()
			mPlayer?.seeking(startTime)
			LogUtil.d(TAG, "onScroll:$startTime")
		}
	}

	override fun onEndScroll(startTime: Int) {
		super.onEndScroll(startTime)
		runOnUiThread {
			mMediaItem.sourceStartTime = startTime.toLong()
			mPlayer?.seekTo(startTime, true)
			LogUtil.d(TAG, "onEndScroll:$startTime")
		}
	}

	private fun hasChange() = isAreaClipChanged || isTimeClipChanged

	private fun genEditedMediaItem(oriMediaItem: MediaItem): MediaItem {
		return oriMediaItem.copy(
			crop = if (oriMediaItem.alignMode == MediaItem.ALIGN_MODE_VIDEO)
				mClipSizeView.getEditedCrop() else oriMediaItem.crop
		)
	}

	@Deprecated("Deprecated in Java")
	override fun onBackPressed() {
		if (!hasChange()) {
			cancelClip()
			return
		}
		showQuitDialog()
	}

	private fun cancelClip() {
		val intent = Intent()
		intent.putExtra(ARG_DATA_CLIP_MEDIA_ITEM, mOriMediaItem)
		setResult(RESULT_OK, intent)
		finish()
	}

	private fun confirmClip() {
		val intent = Intent()
		intent.putExtra(ARG_DATA_CLIP_MEDIA_ITEM, genEditedMediaItem(mMediaItem))
		setResult(RESULT_OK, intent)
		finish()
	}

	private fun showQuitDialog() {
		TipsDialog.Builder()
			.setTitleText("放弃编辑")
			.setMessage("确定放弃所有编辑？")
			.setSureText("放弃")
			.setCancelText("取消")
			.setDialogOperationListener(object : TipsDialog.DialogOperationListener {
				override fun onClickSure() {
					cancelClip()
				}

				override fun onClickCancel() {
				}
			})
			.create(this)
			.show()
	}

	override fun onDestroy() {
		super.onDestroy()
		mPlayer?.pause()
		mPlayer?.release()
		mPlayer = null
	}
}