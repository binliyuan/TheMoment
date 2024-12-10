package com.volcengine.effectone.auto.templates.bean

/**
 * @author tyx
 * @description:
 * @date :2024/5/8 10:12
 */
abstract class BaseResultData<D, T : BaseResultData<D, T>> {
	var state: Int = 0
	var progress: Float = 0f
	var resultData: Result<D>? = null

	open fun setState(state: Int): T {
		this.state = state
		return build()
	}

	open fun setProgress(progress: Float): T {
		this.progress = progress
		return build()
	}

	open fun setResultData(result: Result<D>): T {
		this.resultData = result
		return build()
	}

	open fun reset(): T {
		state = 0
		progress = 0f
		resultData = null
		return build()
	}

	open fun build(): T {
		return create()
	}

	abstract fun create(): T

	companion object {
		const val START = 1
		const val PROGRESS = 2
		const val END = 3
	}
}