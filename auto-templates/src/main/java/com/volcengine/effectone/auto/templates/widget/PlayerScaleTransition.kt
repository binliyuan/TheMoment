package com.volcengine.effectone.auto.templates.widget

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.transition.Transition
import androidx.transition.TransitionValues

/**
 * @author tyx
 * @description:
 * @date :2024/5/29 13:48
 */
class PlayerScaleTransition(
	private var liveData: MutableLiveData<Float>
) : Transition() {
	override fun captureStartValues(p0: TransitionValues) {

	}

	override fun captureEndValues(p0: TransitionValues) {

	}

	override fun createAnimator(sceneRoot: ViewGroup, startValues: TransitionValues?, endValues: TransitionValues?): Animator? {
		val animator = ValueAnimator.ofFloat(0f, 1f)
		animator.duration = duration
		animator.addUpdateListener {
			val animatedValue = it.animatedValue as Float
			liveData.value = animatedValue
		}
		return animator
	}
}