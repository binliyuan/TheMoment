package com.volcengine.effectone.auto.templates.event

import com.volcengine.effectone.perf.AutoPerf

/**
 * @author tyx
 * @description:
 * @date :2024/7/11 14:02
 */
open class CutSameEventReport : IEventReport {

	protected var mPrepareStartTime = 0L
	protected var mComposeStartTime = 0L
	protected var mExportStartTime = 0L

	override fun onPrepareSourceStart(switch: Boolean) {
		mPrepareStartTime = System.currentTimeMillis()
		AutoPerf.cutsame("解析槽位信息开始，startTime:$mPrepareStartTime")
	}

	override fun onPrepareSourceEnd(switch: Boolean) {
		val now = System.currentTimeMillis()
		val delay = now - mPrepareStartTime
		AutoPerf.cutsame("解析槽位结束,endTime:$now,耗时:$delay")
	}

	override fun onComposeStart() {
		mComposeStartTime = System.currentTimeMillis()
		AutoPerf.cutsame("合成视频开始，startTime:$mComposeStartTime")
	}

	override fun onComposeEnd() {
		val now = System.currentTimeMillis()
		val delay = now - mComposeStartTime
		AutoPerf.cutsame("合成视频结束,endTime:$now,耗时:$delay")
	}

	override fun onExportStart() {
		mExportStartTime = System.currentTimeMillis()
		AutoPerf.cutsame("视频导出开始,startTime:$mExportStartTime")
	}

	override fun onExportEnd() {
		val now = System.currentTimeMillis()
		val delay = now - mExportStartTime
		AutoPerf.cutsame("视频导出结束,endTime:$now,耗时:$delay")
	}

	override fun onPlayFps(fps: Float) {
		AutoPerf.cutsame("预览帧率fps:$fps")
	}
}