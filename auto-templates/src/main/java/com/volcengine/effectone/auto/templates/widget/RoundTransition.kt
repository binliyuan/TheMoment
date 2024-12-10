package com.volcengine.effectone.auto.templates.widget

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.ViewGroup
import androidx.transition.Transition
import androidx.transition.TransitionValues

/**
 * @author tyx
 * @description:
 * @date :2024/4/30 14:50
 */
class RoundTransition(
	private val mRoundFrameLayout: TopRoundFrameLayout,
	private val start: Int,
	private val end: Int
) : Transition() {
	override fun captureStartValues(p0: TransitionValues) {
		p0.values["radius"] = start.toFloat()
	}

	override fun captureEndValues(p0: TransitionValues) {
		p0.values["radius"] = end.toFloat()
	}

	override fun createAnimator(sceneRoot: ViewGroup, startValues: TransitionValues?, endValues: TransitionValues?): Animator? {
		if (startValues == null || endValues == null) return null
		val start = startValues.values["radius"] as Float
		val end = endValues.values["radius"] as Float
		val animator = ValueAnimator.ofFloat(start, end)
		animator.duration = duration
		animator.addUpdateListener {
			val animatedValue = it.animatedValue as Float
			mRoundFrameLayout.setRadius(animatedValue)
		}
		return animator
	}
}