package com.volcengine.effectone.auto.templates.cutsame

import android.app.Application
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.cutsame.solution.AuthorityConfig
import com.cutsame.solution.CutSameSolution
import com.cutsame.solution.EffectFetcherConfig
import com.cutsame.solution.TemplateFetcherConfig
import com.ss.android.ugc.cut_log.LogConfig
import com.ss.android.ugc.cut_log.LogIF
import com.ss.android.ugc.cut_log.LogWrapper
import com.volcengine.effectone.auto.templates.utils.ApiUtil
import com.volcengine.effectone.singleton.AppSingleton

/**
 * @author tyx
 * @description:
 * @date :2024/4/24 15:04
 */
object CutSameInit {

	const val TAG = "ICutSameInit"

	fun initCutSame(
		application: Application,
		licensePath: String,
		modelPath: String
	) {
		AppSingleton.bindInstance(application)
		val logWrapper = LogWrapper().apply {
			init(
				logConfig = LogConfig.Builder()
					.logcatLevel(LogIF.LOG_LEVER.DEBUG)
					.localLevel(LogIF.LOG_LEVER.WARNING)
					.toLocal(true)
					.toLogcat(true)
					.showThreadInfo(true)
					.localPath(Environment.getExternalStorageDirectory().absolutePath)
					.build()
			)
		}
		Log.d(TAG, "order_id: ${ApiUtil.getOrderId(application)}, host: ${ApiUtil.host}")
		CutSameSolution.setLogIf(logWrapper)

		CutSameSolution.init(
			context = application,
			authorityConfig = AuthorityConfig.Builder()
				.licensePath(licensePath)
				.authorityListener(object : AuthorityConfig.AuthorityListener {
					override fun onError(errorCode: Int, errorMsg: String) {
						Log.d(TAG, "onError $errorCode $errorMsg")
						Toast.makeText(application, errorMsg, Toast.LENGTH_SHORT).show()
					}
				}).build(),

			templateFetcherConfig = TemplateFetcherConfig.Builder()
				.host(ApiUtil.host)
				.build(),

			effectFetcherConfig = EffectFetcherConfig.Builder()
				.host(ApiUtil.host)
				.modelPath("/api/modellistinfo")
				.localModelPath(modelPath)
				.effectLitPath("/api/effectlist")
				.effectLitExtraMap(mutableMapOf("order_id" to ApiUtil.getOrderId(application)))
				.build()
		)
	}
}