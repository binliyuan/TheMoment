package com.volcengine.effectone.auto.templates.event

import com.volcengine.effectone.perf.AutoPerf

/**
 * @author tyx
 * @description:
 * @date :2024/7/11 14:29
 */
class CutSameMomentEventReport : CutSameEventReport() {
	private var switch = false
	override fun onPrepareSourceStart(switch: Boolean) {
		this.switch = switch
		mPrepareStartTime = System.currentTimeMillis()
		val msg = if (switch) "切换模板开始" else "合成视频开始"
		AutoPerf.moment("$msg，startTime:$mPrepareStartTime")
	}

	override fun onPrepareSourceEnd(switch: Boolean) {}

	override fun onComposeStart() {}

	override fun onComposeEnd() {
		val now = System.currentTimeMillis()
		val delay = now - mPrepareStartTime
		val msg = if (switch) "切换模板结束" else "合成视频结束"
		AutoPerf.moment("$msg,endTime:$now,耗时:$delay")
	}

	override fun onExportStart() {
		mExportStartTime = System.currentTimeMillis()
		AutoPerf.moment("视频导出开始,startTime:$mExportStartTime")
	}

	override fun onExportEnd() {
		val now = System.currentTimeMillis()
		val delay = now - mExportStartTime
		AutoPerf.moment("视频导出结束,endTime:$now,耗时:$delay")
	}

	override fun onPlayFps(fps: Float) {
		AutoPerf.moment("预览帧率fps:$fps")
	}
}