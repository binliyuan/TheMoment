package com.volcengine.effectone.auto.templates.event

/**
 * @author tyx
 * @description:
 * @date :2024/7/11 13:51
 */
interface IEventReport {
	fun onPrepareSourceStart(switch: Boolean)
	fun onPrepareSourceEnd(switch: Boolean)
	fun onComposeStart()
	fun onComposeEnd()
	fun onExportStart()
	fun onExportEnd()
	fun onPlayFps(fps: Float)
}
