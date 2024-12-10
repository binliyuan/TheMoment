package com.volcengine.effectone.auto.templates.event

/**
 * @author tyx
 * @description:
 * @date :2024/7/11 11:46
 */
object TrackNodeEvent {

	private var mReport: IEventReport? = null

	fun setEventReport(mEventReport: IEventReport) {
		mReport = mEventReport
	}

	fun onPrepareSourceStart(hasSwitch: Boolean) {
		mReport?.onPrepareSourceStart(hasSwitch)
	}

	fun onPrepareSourceEnd(hasSwitch: Boolean) {
		mReport?.onPrepareSourceEnd(hasSwitch)
	}

	fun onComposeStart() {
		mReport?.onComposeStart()
	}

	fun onComposeEnd() {
		mReport?.onComposeEnd()
	}

	fun onExportStart() {
		mReport?.onExportStart()
	}

	fun onExportEnd() {
		mReport?.onExportEnd()
	}

	fun onPlayFps(fps: Float) {
		mReport?.onPlayFps(fps)
	}
}