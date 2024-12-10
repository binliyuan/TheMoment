package com.volcengine.effectone.auto.common.widget

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import com.volcengine.effectone.auto.common.R

class AutoOneShotOverlay
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0,
    ) : View(context, attrs, defStyleAttr, defStyleRes) {
        // 闪屏绘制画笔
        private val flashPaint by lazyFast {
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = flashColor
            }
        }

        // 动画进度更新
        private val flashUpdateAnimation by lazyFast {
            ValueAnimator.AnimatorUpdateListener {
                val alpha = 255 * it.animatedValue as Float
                this@AutoOneShotOverlay.alpha = alpha
                invalidate()
            }
        }

        // 差值器
        private val flashInterceptor by lazyFast { LinearInterpolator() }

        // 动画监听器
        private val flashAnimationListener by lazyFast {
            object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    visibility = VISIBLE
                }

                override fun onAnimationEnd(animation: Animator) {
                    visibility = INVISIBLE
                    flashAnimation = null
                }

                override fun onAnimationCancel(animation: Animator) = Unit

                override fun onAnimationRepeat(animation: Animator) = Unit
            }
        }

        // 闪屏区域
        private val flashRegion = Rect()

        // 闪屏区域颜色
        @ColorInt
        private var flashColor = DEFAULT_FLASH_COLOR

        // 闪屏动画
        private var flashAnimation: AnimatorSet? = null

        init {
            val typeArray =
                context.theme?.run {
                    this.obtainStyledAttributes(
                        attrs,
                        R.styleable.AutoOneShotOverlay,
                        defStyleAttr,
                        defStyleRes,
                    )
                } ?: kotlin.run {
                    context.obtainStyledAttributes(
                        attrs,
                        R.styleable.AutoOneShotOverlay,
                        defStyleAttr,
                        defStyleRes,
                    )
                }

            typeArray.getColor(R.styleable.AutoOneShotOverlay_flash_color, DEFAULT_FLASH_COLOR).also {
                flashPaint.color = it
            }
        }

        fun startFlashAnimation(oneShort: Boolean = true) {
            if (flashIsRunning()) {
                flashAnimation!!.cancel()
            }
            val maxAlpha =
                if (oneShort) {
                    ONE_SHORT_FLASH_MAX_ALPHA
                } else {
                    FLASH_MAX_ALPHA
                }
            val step1 =
                ValueAnimator.ofFloat(maxAlpha).apply {
                    addUpdateListener(flashUpdateAnimation)
                    interpolator = flashInterceptor
                }
            val step2 =
                ValueAnimator.ofFloat(maxAlpha, .0f).apply {
                    addUpdateListener(flashUpdateAnimation)
                    interpolator = flashInterceptor
                }
            if (oneShort) {
                step1.duration = ONE_SHORT_FLASH_FULL_DURATION_MS
                step2.duration = ONE_SHORT_FLASH_DECREASE_DURATION_MS
            } else {
                step1.duration = FLASH_FULL_DURATION_MS
                step2.duration = FLASH_DECREASE_DURATION_MS
            }

            flashAnimation =
                AnimatorSet().apply {
                    addListener(flashAnimationListener)
                    play(step1).before(step2)
                    start()
                }
        }

        fun updateFlashRegion(region: Rect) {
            flashRegion.set(region)
        }

        override fun onSizeChanged(
            w: Int,
            h: Int,
            oldw: Int,
            oldh: Int,
        ) {
            super.onSizeChanged(w, h, oldw, oldh)
            // 默认控件大小
            flashRegion.set(0, 0, w, h)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.takeIf { flashIsRunning() }?.run {
                drawRect(flashRegion, flashPaint)
                clipRect(flashRegion)
            }
        }

        private fun flashIsRunning() = flashAnimation != null && flashAnimation!!.isRunning

        private fun <T> lazyFast(operation: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE, operation)

        companion object {
            private const val DEFAULT_FLASH_COLOR = Color.BLACK
            private const val FLASH_MAX_ALPHA = 0.85f
            private const val FLASH_FULL_DURATION_MS = 65L
            private const val FLASH_DECREASE_DURATION_MS = 150L
            private const val ONE_SHORT_FLASH_MAX_ALPHA = 0.75f
            private const val ONE_SHORT_FLASH_FULL_DURATION_MS = 34L
            private const val ONE_SHORT_FLASH_DECREASE_DURATION_MS = 100L
        }
    }
