package com.volcengine.effectone.auto.templates.widget.clip

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import com.bytedance.ies.cutsame.util.MediaUtil
import com.ss.android.medialib.common.LogUtil
import com.ss.android.ugc.cut_ui.ItemCrop
import com.ss.android.ugc.cut_ui.MediaItem
import com.ss.android.ugc.tools.utils.gesture.MoveGestureDetector
import com.ss.ugc.android.editor.track.widget.ScaleGestureDetector
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.utils.SizeUtil
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * @author tyx
 * @description:
 * @date :2024/5/16 17:35
 */
class ClipSizeView : View, ScaleGestureDetector.OnScaleGestureListener, MoveGestureDetector.OnMoveGestureListener {

	companion object {
		private const val TAG = "ClipSizeView"
		private const val SCALE_MAX_RATIO = 30
	}

	private lateinit var mMaskPaint: Paint
	private var mMaskColor = ContextCompat.getColor(context, R.color.color_99_000)
	private val mMaskRectF by lazy { RectF() }

	private lateinit var mClipPaint: Paint
	private var mClipStrokeStartColor = ContextCompat.getColor(context, R.color.tab_indicator_start_color)
	private var mClipStrokeEndColor = ContextCompat.getColor(context, R.color.tab_indicator_end_color)
	private var mClipStrokeWidth = SizeUtil.dp2px(1.5f)
	private val mClipRectF by lazy { RectF() }                //裁剪框，大小往内缩小裁剪框线条宽度的一半
	private lateinit var mLinearGradient: LinearGradient
	private val mClearMode by lazy { PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
	private var mClipWidth = SizeUtil.dp2px(513f).toFloat()    //裁剪框
	private var mClipHeight = SizeUtil.dp2px(289f).toFloat()
	private val mCanvasSize by lazy { Point() }    //视频画布大小
	private var mMediaItem: MediaItem? = null

	private var mListener: IOnClipSizeListener? = null

	private var mScaleGestureDetector: ScaleGestureDetector   //缩放手势
	private var mMoveGestureDetector: MoveGestureDetector     //move手势

	private var mCurrentScale = 1f                            //当前缩放比例
	private var mVideoFrameFactor = 0f                        //裁剪框相对于视频真实显示大小的缩放比例
	private val mBoxRectF by lazy { RectF() }                 //裁剪框
	private val mVideoRectF by lazy { RectF() }               //视频
	private var isRebounding = false

	private var mCenterX = 0f       //内容的中心坐标
	private var mCenterY = 0f
	private var mTranslatesX = 0f   //X偏移的比例
	private var mTranslatesY = 0f
	private var mDefTranslatesY = 0f
	private var mWidth = 0f         //放大后的真实宽高
	private var mHeight = 0f

	constructor(context: Context) : this(context, null)
	constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
		initPaint()
		mScaleGestureDetector = ScaleGestureDetector(this)
		mMoveGestureDetector = MoveGestureDetector(context, this)
	}

	private fun initPaint() {
		mMaskPaint = Paint()
		mMaskPaint.run {
			isDither = true
			isAntiAlias = true
			color = mMaskColor
			style = Paint.Style.FILL
		}
		mClipPaint = Paint()
		mClipPaint.run {
			isDither = true
			isAntiAlias = true
			style = Paint.Style.STROKE
			strokeWidth = mClipStrokeWidth.toFloat()
		}
	}

	@SuppressLint("DrawAllocation")
	override fun onDraw(canvas: Canvas?) {
		super.onDraw(canvas)
		canvas ?: return
		mMaskRectF.set(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())
		val layer = canvas.saveLayer(mMaskRectF, mMaskPaint)
		canvas.drawRect(mMaskRectF, mMaskPaint)
		mMaskPaint.setXfermode(mClearMode)
		mClipRectF.set(mBoxRectF)
		canvas.drawRect(mClipRectF, mMaskPaint)
		mClipRectF.inset(mClipStrokeWidth / 2f, mClipStrokeWidth / 2f)
		mLinearGradient = LinearGradient(0f, 0f, mClipWidth, 0f, intArrayOf(mClipStrokeStartColor, mClipStrokeEndColor), null, Shader.TileMode.CLAMP)
		mClipPaint.setShader(mLinearGradient)
		canvas.drawRect(mClipRectF, mClipPaint)
		mMaskPaint.setXfermode(null)
		canvas.restoreToCount(layer)
	}

	fun setOnClipSizeListener(listener: IOnClipSizeListener) {
		this.mListener = listener
	}

	@SuppressLint("ClickableViewAccessibility")
	override fun onTouchEvent(event: MotionEvent): Boolean {
		mScaleGestureDetector.onTouchEvent(this, event)
		mMoveGestureDetector.onTouchEvent(event)
		when (event.action) {
			MotionEvent.ACTION_DOWN -> mListener?.onDown()
			MotionEvent.ACTION_UP -> onUp()
		}
		return true
	}

	private fun onUp() {
		if (mMediaItem?.alignMode == MediaItem.ALIGN_MODE_CANVAS) {
			rebound()
			return
		}
		if ((mCenterX - mWidth / 2 <= mBoxRectF.left && mCenterX + mWidth / 2 >= mBoxRectF.right)
			&& (mCenterY - mHeight / 2 <= mBoxRectF.top && mCenterY + mHeight / 2 >= mBoxRectF.bottom)
		) {
			mListener?.onUp()
			return
		}
		rebound()
	}

	private fun rebound() {
		if (isRebounding) return
		isRebounding = true
		val beginScale = mCurrentScale
		val beginTransX = mTranslatesX
		val beginTransY = mTranslatesY
		//裁剪框不是居中的，所有有默认的Y偏移
		val defTransY = (mBoxRectF.centerY() - mVideoRectF.centerY()) / mCanvasSize.y
		val animator = ValueAnimator.ofFloat(0F, 1F)
		animator.interpolator = OvershootInterpolator(1.0F)
		animator.duration = 250
		animator.addUpdateListener {
			val fraction = it.animatedFraction
			mCurrentScale = beginScale + (1 - beginScale) * fraction
			mTranslatesX = beginTransX * (1 - fraction)
			mTranslatesY = beginTransY * (1 - fraction) + defTransY
			onMove(mCurrentScale * mVideoFrameFactor, false)
		}
		animator.addListener(object : AnimatorListenerAdapter() {
			override fun onAnimationEnd(animation: Animator) {
				isRebounding = false
				mListener?.onUp()
			}
		})
		animator.start()
	}

	fun updateData(canvasSize: Point, mediaItem: MediaItem) {
		mCanvasSize.set(canvasSize.x, canvasSize.y)
		mMediaItem = mediaItem
		//裁剪框适配:保持mediaItem的裁剪宽高比进行缩放，得出在裁剪框内显示的大小，可能一边留白
		val clipWidth = mediaItem.clipWidth
		val clipHeight = mediaItem.clipHeight
		val clipScaleFactor = minOf(mClipWidth / clipWidth, mClipHeight / clipHeight)
		mClipWidth = clipWidth * clipScaleFactor
		mClipHeight = clipHeight * clipScaleFactor
		LogUtil.d(TAG, "clipW:$mClipWidth,clipH:$mClipHeight")
		//初始化裁剪框坐标
		var left = (canvasSize.x - mClipWidth) / 2
		var top = SizeUtil.dp2px(90f).toFloat()
		mBoxRectF.set(left, top, left + mClipWidth, top + mClipHeight)

		//刷新裁剪框
		invalidate()

		//canvas大小适配，保持视频的宽高比进行缩放，以保证能够在canvas中显示完整，得出在canvas内显示的大小
		val videoInfo = MediaUtil.getRealVideoMetaDataInfo(context, mediaItem.source)
		var videoWidth = videoInfo.width.toFloat()
		var videoHeight = videoInfo.height.toFloat()
		val canvasScaleFactor = minOf(mCanvasSize.x / 1f / videoWidth, mCanvasSize.y / 1f / videoHeight)
		videoWidth *= canvasScaleFactor
		videoHeight *= canvasScaleFactor

		//裁剪框与真实视频播放大小的缩放比例，视频显示在裁剪框内，保证一边铺满，此时视频内容显示在裁剪框内
		mVideoFrameFactor = maxOf(mClipWidth / videoWidth, mClipHeight / videoHeight)
		videoWidth *= mVideoFrameFactor
		videoHeight *= mVideoFrameFactor
		LogUtil.d(TAG, "videoW:$videoWidth,videoH:$videoHeight")
		LogUtil.d(TAG, "videoFrameFactor:$mVideoFrameFactor")
		//初始化视频内容坐标
		left = (canvasSize.x - videoWidth) / 2f
		top = (canvasSize.y - videoHeight) / 2f
		mVideoRectF.set(left, top, left + videoWidth, top + videoHeight)

		//视频内容还需要进一步放大，以将视频内容显示在裁剪的区域内(有裁剪的话)
		val crop = mediaItem.crop
		mCurrentScale = if (mClipWidth / mClipHeight >= videoWidth / videoHeight) {
			1f / (crop.lowerRightX - crop.upperLeftX)
		} else {
			1f / (crop.lowerRightY - crop.upperLeftY)
		}
		LogUtil.d(TAG, "currentScale:$mCurrentScale")

		//计算偏移后内容的的中心坐标
		val width = mBoxRectF.width() / (crop.lowerRightX - crop.upperLeftX)    //可以理解为裁剪内容放大后的宽
		val haltW = width / 2f
		val offsetFromCenterX = width * crop.upperLeftX  //X的偏移量
		mCenterX = mBoxRectF.left + haltW - offsetFromCenterX
		val height = mBoxRectF.height() / (crop.lowerRightY - crop.upperLeftY)
		val halfH = height / 2f
		val offsetFromCenterY = height * crop.upperLeftY //y的偏移量
		mCenterY = mBoxRectF.top + halfH - offsetFromCenterY

		//计算内容的X和Y的偏移比例，内容进行偏移，使裁剪内容与裁剪框匹配上
		mTranslatesX = (mCenterX - mVideoRectF.centerX()) / canvasSize.x
		mTranslatesY = (mCenterY - mVideoRectF.centerY()) / canvasSize.y
		mDefTranslatesY = mTranslatesY

		//将正好显示在裁剪框内的视频内容大小进一步放大,放大到裁剪框内显示裁剪范围的内容
		onMove(mVideoFrameFactor * mCurrentScale, false)
	}

	override fun onScale(view: View, detector: ScaleGestureDetector): Boolean {
		//最大缩放限制
		if (mVideoFrameFactor * mCurrentScale * detector.scaleFactor > SCALE_MAX_RATIO) return true
		mCurrentScale *= detector.scaleFactor
		onMove(mVideoFrameFactor * mCurrentScale)
		return true
	}

	override fun onScaleBegin(view: View, detector: ScaleGestureDetector): Boolean {
		return true
	}

	override fun onScaleEnd(view: View, detector: ScaleGestureDetector) {

	}

	override fun onMove(detector: MoveGestureDetector): Boolean {
		var isTranslate = false
		val tempCenterX = mCenterX + detector.focusDelta.x
		val tempCenterY = mCenterY + detector.focusDelta.y
		val tempWidth = mWidth
		val tempHeight = mHeight
		if (tempCenterX - tempWidth / 2 <= mBoxRectF.left && tempCenterX + tempWidth / 2 >= mBoxRectF.right) {
			mTranslatesX += detector.focusDelta.x / mCanvasSize.x
			isTranslate = true
		}
		if (tempCenterY - tempHeight / 2 <= mBoxRectF.top && tempCenterY + tempHeight / 2 >= mBoxRectF.bottom) {
			mTranslatesY += detector.focusDelta.y / mCanvasSize.y
			isTranslate = true
		}
		if (isTranslate) {
			val moveX = abs(detector.eventX - mDownX)
			val moveY = abs(detector.eventY - mDownY)
			onMove(mVideoFrameFactor * mCurrentScale, moveX > 20 || moveY > 20)
		}
		return true
	}

	private var mDownX = 0f
	private var mDownY = 0f

	override fun onMoveBegin(detector: MoveGestureDetector, downX: Float, downY: Float): Boolean {
		mDownX = downX
		mDownY = downY
		return true
	}

	override fun onMoveEnd(detector: MoveGestureDetector) {

	}

	private fun onMove(scale: Float, isTouch: Boolean = true) {
		mCenterX = mVideoRectF.centerX() + mTranslatesX * mCanvasSize.x
		mCenterY = mVideoRectF.centerY() + mTranslatesY * mCanvasSize.y
		mWidth = mVideoRectF.width() * mCurrentScale
		mHeight = mVideoRectF.height() * mCurrentScale
		mListener?.onMove(isTouch, scale, mTranslatesX, mTranslatesY)
	}

	fun getEditedCrop(): ItemCrop {
		if (mWidth == 0F || mHeight == 0F) return ItemCrop(0F, 0F, 1F, 1F)
		val upperLeftX = removeDeviation((mBoxRectF.left - (mCenterX - mWidth / 2)) / mWidth)
		val upperLeftY = removeDeviation((mBoxRectF.top - (mCenterY - mHeight / 2)) / mHeight)
		val lowRightX = removeDeviation(1 + (mBoxRectF.right - (mCenterX + mWidth / 2)) / mWidth)
		val lowRightY = removeDeviation(1 + (mBoxRectF.bottom - (mCenterY + mHeight / 2)) / mHeight)
		return ItemCrop(upperLeftX, upperLeftY, lowRightX, lowRightY)
	}

	private fun removeDeviation(float: Float): Float {
		return if (float.isNaN()) {
			0F
		} else {
			(float * 100000).roundToInt() / 100000.0F
		}
	}
}