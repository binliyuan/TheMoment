package com.volcengine.effectone.auto.recorder.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.SeekBar
import com.volcengine.effectone.widget.EOTwoWayIndicatorSeekBar

class EOVerticalIndicatorSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : EOTwoWayIndicatorSeekBar(context, attrs, defStyleAttr) {
    companion object{
        private const val TAG = "EOVerticalIndicatorSeek"
    }
    private var mThumb: Drawable? = null
    private val mOnSeekBarChangeListeners by lazy { mutableListOf<OnSeekBarChangeListener?>() }
    private val onSeekBarChangeListenerDelegate by lazy {
        object : OnSeekBarChangeListenerAdapter(){
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val range = max - min
                val scale: Float = if (range > 0) (progress - min) / range.toFloat() else 0f
                onProgressRefresh(scale, fromUser)
            }
        }
    }
    init {
        //禁用进度指示器
        drawIndicatorProgress(false)
    }
    override fun setMin(min: Int) {
        super.setMin(min)
    }

    override fun getMin(): Int {
        return super.getMin()
    }

    override fun setOnSeekBarChangeListener(l: OnSeekBarChangeListener) {
        if (mOnSeekBarChangeListeners.contains(l).not()) {
            mOnSeekBarChangeListeners.add(l)
        }
        super.setOnSeekBarChangeListener(onSeekBarChangeListenerDelegate)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(h, w, oldh, oldw)
    }

    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    override fun onDraw(c: Canvas) {
        c.rotate(-90f)
        c.translate(-height.toFloat(), 0f)
        super.onDraw(c)
    }

    fun onProgressRefresh(scale: Float, fromUser: Boolean) {
        val thumb = mThumb
        if (thumb != null) {
            setThumbPos(height, thumb, scale, Int.MIN_VALUE)
            invalidate()
        }
        mOnSeekBarChangeListeners.filterNotNull().forEach{
            it.onProgressChanged(this, progress, fromUser)
        }
    }

    private fun setThumbPos(w: Int, thumb: Drawable, scale: Float, gap: Int) {
        val available = w - getPaddingLeft() - getPaddingRight()
        val thumbWidth = thumb.intrinsicWidth
        val thumbHeight = thumb.intrinsicHeight
        val thumbPos = (scale * available + 0.5f).toInt()

        val topBound: Int
        val bottomBound: Int
        if (gap == Int.MIN_VALUE) {
            val oldBounds = thumb.getBounds()
            topBound = oldBounds.top
            bottomBound = oldBounds.bottom
        } else {
            topBound = gap
            bottomBound = gap + thumbHeight
        }
        thumb.setBounds(thumbPos, topBound, thumbPos + thumbWidth, bottomBound)
    }

    override fun setThumb(thumb: Drawable) {
        mThumb = thumb
        super.setThumb(thumb)
    }

    private fun onStartTrackingTouch() {
        mOnSeekBarChangeListeners.filterNotNull().forEach{
            it.onStartTrackingTouch(this)
        }
    }

   private fun onStopTrackingTouch() {
       mOnSeekBarChangeListeners.filterNotNull().forEach{
           it.onStopTrackingTouch(this)
       }
    }

    private fun attemptClaimDrag() {
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                setPressed(true)
                onStartTrackingTouch()
            }

            MotionEvent.ACTION_MOVE -> {
                attemptClaimDrag()
                progress = max - (max * event.y / height).toInt()
            }

            MotionEvent.ACTION_UP -> {
                onStopTrackingTouch()
                setPressed(false)
            }

            MotionEvent.ACTION_CANCEL -> {
                onStopTrackingTouch()
                setPressed(false)
            }
        }
        return true
    }
    internal abstract class OnSeekBarChangeListenerAdapter : OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }
    }
}