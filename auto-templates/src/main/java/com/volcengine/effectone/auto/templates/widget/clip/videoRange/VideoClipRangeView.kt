package com.volcengine.effectone.auto.templates.widget.clip.videoRange

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.Scroller
import androidx.core.content.ContextCompat
import com.bytedance.ies.cutsame.util.MediaUtil
import com.ss.android.ugc.cut_log.LogUtil
import com.ss.android.ugc.cut_ui.MediaItem
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.utils.SizeUtil
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

/**
 * @author tyx
 * @description:视频长度裁剪
 * @date :2024/5/10 17:18
 */
class VideoClipRangeView : View, VideoFrameHelper.VideoFrameListener {

	companion object {
		private const val TAG = "VideoClipRangeView"
	}

	private val mGestureDetector: GestureDetector
	private lateinit var mScroller: Scroller
	private var mMediaItem: MediaItem? = null
	private val mTimesMap by lazy { mutableMapOf<Int, Float>() }
	private val mFrameHelper by lazy { VideoFrameHelper().also { it.setFrameListener(this) } }
	private var mSecondWidth = 0f      //1秒对应的宽度
	private var mClipWith = SizeUtil.dp2px(327f)  //裁剪框的宽度
	private var mClipNum = 5   //裁剪的帧数
	private var mContentWidth = 0f  //内容的总宽度
	private var mScrollOffsetX = 0f  //X偏移的总量
	private val mPath by lazy { Path() }
	private var mClipListener: VideoClipRangeListener? = null
	private var mIsStartFling = false

	//裁剪框参数
	private lateinit var mClipPaint: Paint   //裁剪框画笔
	private var mClipStrokeWidth = SizeUtil.dp2px(2f)
	private var mClipStrokeColor = ContextCompat.getColor(context, com.ss.ugc.android.editor.track.R.color.white)
	private val mClipRectF by lazy { RectF() }
	private val mClipRadius = SizeUtil.dp2px(10f).toFloat()
	private var mClipMarginLeft = SizeUtil.dp2px(130f).toFloat()  //裁剪框左间距

	//裁剪框渐变背景
	private lateinit var mClipBgPaint: Paint
	private var mClipBgLinearGradient: LinearGradient? = null
	private var mClipBgStartColor = ContextCompat.getColor(context, R.color.color_99_000)
	private var mClipBgEndColor = Color.parseColor("#00000000")

	//封面帧
	private val mFrameRectF by lazy { RectF() }
	private lateinit var mFramePaint: Paint  //封面画笔
	private val mFrameHeight = SizeUtil.dp2px(70f)

	//进度线
	private lateinit var mProgressPaint: Paint
	private var mProgressLineColor = ContextCompat.getColor(context, R.color.color_FF53BB)
	private var mProgressLineWidth = SizeUtil.dp2px(2.5f)
	private var mPlayerProgress = 0f

	//背景遮罩
	private lateinit var mMaskPaint: Paint
	private var mMaskColor = ContextCompat.getColor(context, R.color.color_99_000)
	private val mMaskRectF by lazy { RectF() }
	private val mClearMode by lazy { PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

	//裁剪框中间文字
	private lateinit var mTextPaint: Paint
	private var mTextColor = ContextCompat.getColor(context, R.color.color_EEE)
	private var mTextSize = SizeUtil.sp2px(18f)
	private var mDrawText = ""

	constructor(context: Context) : this(context, null)
	constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
		initPaint()
		mGestureDetector = GestureDetector(context, mGestureDetectorListener)
		mScroller = Scroller(context)
	}

	private fun initPaint() {
		mClipPaint = Paint()
		mClipPaint.run {
			isDither = true
			isAntiAlias = true
			color = mClipStrokeColor
			style = Paint.Style.STROKE
			strokeWidth = mClipStrokeWidth.toFloat()
		}
		mClipBgPaint = Paint()
		mClipBgPaint.run {
			isDither = true
			isAntiAlias = true
			style = Paint.Style.FILL
		}
		mFramePaint = Paint()
		mFramePaint.run {
			isDither = true
			isAntiAlias = true
		}
		mProgressPaint = Paint()
		mProgressPaint.run {
			isDither = true
			isAntiAlias = true
			color = mProgressLineColor
			style = Paint.Style.STROKE
			strokeCap = Paint.Cap.ROUND
			strokeWidth = mProgressLineWidth.toFloat()
		}
		mMaskPaint = Paint()
		mMaskPaint.run {
			isDither = true
			isAntiAlias = true
			color = mMaskColor
			style = Paint.Style.FILL
		}
		mTextPaint = Paint()
		mTextPaint.run {
			isDither = true
			isAntiAlias = true
			textSize = mTextSize.toFloat()
			color = mTextColor
		}
	}

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)
		val widthSize = MeasureSpec.getSize(widthMeasureSpec)
		val widthMode = MeasureSpec.getMode(widthMeasureSpec)
		val heightSize = MeasureSpec.getSize(heightMeasureSpec)
		val heightMode = MeasureSpec.getMode(heightMeasureSpec)
		val mHeight = SizeUtil.dp2px(86f)
		val mWidth = SizeUtil.getScreenWidth(context)
		setMeasuredDimension(
			if (widthMode == MeasureSpec.EXACTLY) widthSize else mWidth,
			if (heightMode == MeasureSpec.EXACTLY) heightSize else mHeight
		)
	}

	override fun onDraw(canvas: Canvas?) {
		super.onDraw(canvas)
		canvas ?: return
		//裁剪圆角
		canvas.save()
		mPath.reset()
		val top = measuredHeight / 2f - mFrameHeight / 2f
		mPath.addRoundRect(0f, top, measuredWidth.toFloat(), top + mFrameHeight, mClipRadius, mClipRadius, Path.Direction.CCW)
		canvas.clipPath(mPath)
		//画封面帧
		drawFrame(canvas)
		canvas.restore()
		//画背景遮罩,挖出裁剪框的高亮区域
		drawMask(canvas)
		//画裁剪框
		drawClip(canvas)
		//文字
		drawText(canvas)
		//画进度条
		drawProgress(canvas)
	}

	/**
	 * 1.画内容（所有帧）宽度的圆角框 滑动通过改变mScrollOffsetX
	 * 2.在框内画bitmap帧，优化性能只画当前屏幕显示的帧数; 内容宽度 = 所有bitmap的宽度
	 */
	private fun drawFrame(canvas: Canvas) {
		mMediaItem?.let { mediaItem ->
			//画背景条
			canvas.save()
			mPath.reset()
			val top = measuredHeight / 2f - mFrameHeight / 2f
			val left = -mScrollOffsetX
			mFrameRectF.set(-mScrollOffsetX, top, left + mContentWidth, top + mFrameHeight)
			mPath.addRoundRect(mFrameRectF, mClipRadius, mClipRadius, Path.Direction.CCW)
			canvas.clipPath(mPath)
			canvas.drawRect(mFrameRectF, mFramePaint)
			//draw frame
			val num = 1  //左右两边屏幕之外显示的数量
			val frameWith = mClipWith / mClipNum   //一帧的宽度
			val leftGoneNum = maxOf(0, (floor(mScrollOffsetX / 1f / frameWith).toInt() - num)) //左边不可见的数量
			val maxNum = ceil(measuredWidth / 1f / frameWith) + num  //最大需要显示的数量
			val startTime = leftGoneNum * px2Time(frameWith.toFloat())  //需要显示的首帧时间
			val endTime = startTime + maxNum * px2Time(frameWith.toFloat()) //末尾帧时间
			//从缓存里取帧 map里存了每帧的开始时间和精确的时间用于计算X坐标
			mTimesMap.filter {
				it.key >= startTime && it.key <= endTime
			}.mapNotNull {
				val frame = mFrameHelper.getFrame(mediaItem.source, it.key)
				if (frame != null) it.value to frame else null
			}.onEach {
				it.second.let { bitmap ->
					//这里需要-mScrollOffsetX，跟随滑动
					val startX = time2Px(it.first) - mScrollOffsetX
					canvas.drawBitmap(bitmap, startX, top, mFramePaint)
				}
			}.also {
				LogUtil.d(TAG, "draw bitmap size:${it.size}")
			}
			canvas.restore()
		}
	}

	/**
	 * 裁剪圆角
	 * 1.画透明黑色遮罩，且仅绘制盖在帧图片上面的部分（超出裁剪框之外，view宽度之内），根据帧图片滑动值计算
	 * 2.裁剪框范围内挖洞高亮（裁剪框内渐变色）
	 */
	private fun drawMask(canvas: Canvas) {
		val top = measuredHeight / 2f - mFrameHeight / 2f
		//裁剪圆角，且计算遮罩左右边界，目的为仅绘制帧图片超出裁剪框之外的部分并且在整个view的宽度之内。
		val maskLeft = maxOf(0f, -mScrollOffsetX)
		val right = measuredWidth - mClipRectF.right
		val maxOffset = mContentWidth - measuredWidth + right       //最大滑动偏移值
		val rightValue = minOf(maxOffset - mScrollOffsetX, right) //右边超出裁剪框的部分
		val maskRight = mClipRectF.right + rightValue
		mMaskRectF.set(maskLeft, top, maskRight, top + mFrameHeight.toFloat())
		val layer = canvas.saveLayer(mMaskRectF, mMaskPaint)
		canvas.save()
		mPath.reset()
		mPath.addRoundRect(mMaskRectF, mClipRadius, mClipRadius, Path.Direction.CCW)
		canvas.clipPath(mPath)
		//画背景
		canvas.drawRect(mMaskRectF, mMaskPaint)
		canvas.restore()
		mMaskPaint.setXfermode(mClearMode)
		//挖洞
		val left = mClipMarginLeft
		mMaskRectF.set(left, top, left + mClipWith, top + mFrameHeight.toFloat())
		canvas.drawRoundRect(mMaskRectF, mClipRadius, mClipRadius, mMaskPaint)
		mMaskPaint.setXfermode(null)
		canvas.restoreToCount(layer)

		//画裁剪框渐变背景
		if (mClipBgLinearGradient == null) {
			mClipBgLinearGradient = LinearGradient(
				0f, top + mFrameHeight.toFloat(), 0f, top,
				intArrayOf(mClipBgStartColor, mClipBgEndColor), null, Shader.TileMode.CLAMP
			)
		}
		mClipBgLinearGradient?.let {
			mClipBgPaint.setShader(it)
			canvas.drawRoundRect(mMaskRectF, mClipRadius, mClipRadius, mClipBgPaint)
		}
	}

	private fun drawClip(canvas: Canvas) {
		val top = measuredHeight / 2f - mFrameHeight / 2f
		val left = mClipMarginLeft
		mClipRectF.set(left, top, left + mClipWith, top + mFrameHeight.toFloat())
		mClipRectF.inset(mClipStrokeWidth / 2f, mClipStrokeWidth / 2f)
		canvas.drawRoundRect(mClipRectF, mClipRadius, mClipRadius, mClipPaint)
	}

	private fun drawText(canvas: Canvas) {
		if (mDrawText.isEmpty()) return
		val fontMetrics = mTextPaint.fontMetrics
		val textCenterY = measuredHeight / 2f
		val left = mClipRectF.left + SizeUtil.dp2px(148f)
		val baseLine = textCenterY - (fontMetrics.ascent + fontMetrics.descent) / 2f
		canvas.drawText(mDrawText, left, baseLine, mTextPaint)
	}

	private fun drawProgress(canvas: Canvas) {
		val left = mClipRectF.left + mClipWith * mPlayerProgress
		val start = minOf(maxOf(mClipRectF.left, left), mClipRectF.right)
		canvas.drawLine(start, mProgressLineWidth.toFloat(), start, measuredHeight - mProgressLineWidth.toFloat(), mProgressPaint)
	}

	fun onPlayProgress(progress: Float) {
		mPlayerProgress = maxOf(0f, minOf(1f, progress))
		invalidate()
	}

	@SuppressLint("ClickableViewAccessibility")
	override fun onTouchEvent(event: MotionEvent): Boolean {
		when (event.action) {
			MotionEvent.ACTION_UP -> {
				mClipListener?.onEndScroll(px2Time(mScrollOffsetX + mClipRectF.left).toInt())
			}
		}
		mGestureDetector.onTouchEvent(event)
		return true
	}

	private val mGestureDetectorListener = object : GestureDetector.SimpleOnGestureListener() {
		override fun onDown(e: MotionEvent): Boolean {
			mScroller.abortAnimation()
			mIsStartFling = false
			mClipListener?.onStartScroll()
			return true
		}

		override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
			val offset = mScrollOffsetX + distanceX
			val left = mClipRectF.left
			val right = measuredWidth - mClipRectF.right
			//可以左右滑动到裁剪框的左右到头的位置
			if (offset > -left && offset < (mContentWidth - measuredWidth + right)) {
				mScrollOffsetX = offset
				mClipListener?.onScroll(px2Time(mScrollOffsetX + mClipRectF.left).toInt())
				LogUtil.d(TAG, "distanceX:$distanceX,scrollPos:$mScrollOffsetX")
				invalidate()
			}
			return true
		}

		override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
			val scaledMinimumFlingVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity
			//小于最小滑动速度则不开启惯性滑动
			if (abs(velocityX) < scaledMinimumFlingVelocity) return super.onFling(e1, e2, velocityX, velocityY)
			mIsStartFling = true
			val maxX = mContentWidth - measuredWidth + (measuredWidth - mClipRectF.right)
			val minX = -mClipRectF.left
			mScroller.fling(
				mScrollOffsetX.toInt(),  //当前滚动位置的x坐标
				0,
				-velocityX.toInt(),     //x方向上的滑动速度
				0,
				minX.toInt(),  //x最小边界
				maxX.toInt(),  //x最大边界
				0,
				0
			)
			invalidate()
			return true
		}
	}

	override fun computeScroll() {
		if (mScroller.computeScrollOffset()) {
			mScrollOffsetX = mScroller.currX.toFloat()
			mClipListener?.onScroll(px2Time(mScrollOffsetX + mClipRectF.left).toInt())
			invalidate()
		} else {
			if (mIsStartFling) {
				mClipListener?.onEndScroll(px2Time(mScrollOffsetX + mClipRectF.left).toInt())
				mIsStartFling = false
			}
		}
	}

	/**
	 * 时间转像素
	 */
	private fun time2Px(time: Float): Float {
		return (time / 1000f) * mSecondWidth
	}

	/**
	 * 像素转时间
	 */
	private fun px2Time(px: Float): Float {
		return px / mSecondWidth * 1000f
	}

	/**
	 * 初始化信息
	 * 根据裁剪框的宽度，裁剪的秒数，一帧的宽度等，计算相关数据
	 * 取帧
	 */
	fun initMediaItem(mediaItem: MediaItem) {
		mMediaItem = mediaItem
		val videoInfo = MediaUtil.getRealVideoMetaDataInfo(context, mediaItem.source)
		val slotTime = mediaItem.duration   //槽位时长
		val startTime = mediaItem.sourceStartTime
		val videoDuration = videoInfo.duration  //视频时长
		val frameWith = mClipWith / 1f / mClipNum  //一帧的宽度
		mSecondWidth = mClipWith / 1f / (slotTime / 1000f)    //一秒的宽度
		LogUtil.d(TAG, "videoDuration:$videoDuration,slotTime:$slotTime,startTime:$startTime")
		val frameTime = px2Time(frameWith)   //一帧的时长
		val frameCount = ceil(videoDuration / frameTime).toInt()  //总共需要的帧数
		mContentWidth = frameCount * frameWith
		LogUtil.d(TAG, "mContentWidth:$mContentWidth,mScrollOffsetX:$mScrollOffsetX")
		LogUtil.d(TAG, "frameWidth:$frameWith,frameTime:$frameTime,frameCount:$frameCount")
		mDrawText = String.format("已选取%.1fs", (mediaItem.duration / 1000f))
		mTimesMap.clear()
		for (i in 0 until frameCount) {
			mTimesMap[(i * frameTime).toInt()] = i * frameTime
		}
		val ptsArray = mTimesMap.map { it.key }.toIntArray()
		//取帧
		mFrameHelper.startGetFrame(mediaItem.source, ptsArray, frameWith.toInt(), mFrameHeight, false)

		post {
			//起始播放首帧跟裁剪框左对齐
			mClipRectF.left = mClipMarginLeft
			mScrollOffsetX = time2Px(startTime.toFloat()) - mClipRectF.left
			invalidate()
		}
	}

	/**
	 * 取帧刷新
	 */
	override fun onRefresh() {
		postInvalidate()
	}

	fun setOnVideoClipListener(listener: VideoClipRangeListener) {
		this.mClipListener = listener
	}

	override fun onDetachedFromWindow() {
		super.onDetachedFromWindow()
		mClipListener = null
		mFrameHelper.release()
	}
}