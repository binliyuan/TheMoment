package com.volcengine.effectone.auto.templates.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cutsame.solution.template.model.TemplateCategory
import com.cutsame.solution.template.model.TemplateItem
import com.volcengine.effectone.InnerEffectOneConfigList
import com.volcengine.effectone.auto.templates.bean.BaseResultData
import com.volcengine.effectone.auto.templates.bean.TemplateListResult
import com.volcengine.effectone.auto.templates.bean.TemplateLoadResult
import com.volcengine.effectone.auto.templates.cutsame.ILoadTemplatesResourceCallback
import com.volcengine.effectone.auto.templates.cutsame.TemplateConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author tyx
 * @description:
 * @date :2024/4/25 13:58
 */
class TemplatesPageViewModel : ViewModel() {

	private val mScope by lazy { CoroutineScope(Dispatchers.Main + SupervisorJob()) }

	private val mTemplateConfig: TemplateConfig? = InnerEffectOneConfigList.getConfig()
	val mTemplateListResult = MutableLiveData<TemplateListResult>()
	val mTemplateLoadResult = MutableLiveData<TemplateLoadResult>()

	fun requestTemplatesList(templateCategory: TemplateCategory?) {
		mScope.launch {
			val templates = TemplateListResult()
			mTemplateListResult.value = templates.setState(BaseResultData.START)
			val list = withContext(Dispatchers.IO) {
				mTemplateConfig?.let { config ->
					config.getTemplateList().filter { it.templateTags == templateCategory?.name }
				} ?: emptyList()
			}
			val result = if (list.isNotEmpty()) Result.success(list.toMutableList()) else Result.failure(Throwable("fail"))
			mTemplateListResult.value = templates.setState(BaseResultData.END).setResultData(result)
		}
	}

	fun filterTemplatesList(templateCategory: TemplateCategory?, templates: List<TemplateItem>) {
		val templateResult = TemplateListResult()
		val list = templates.filter { it.templateTags == templateCategory?.name }.toMutableList()
		mTemplateListResult.value = templateResult.setState(BaseResultData.END).setResultData(Result.success(list))
	}

	fun loadTemplateResource(templateItem: TemplateItem) {
		mScope.launch {
			val templateLoadResult = TemplateLoadResult()
			mTemplateLoadResult.value = templateLoadResult.setState(BaseResultData.START)
			mTemplateConfig?.let { config ->
				val template = config.loadTemplateResource(templateItem, object : ILoadTemplatesResourceCallback {
					override fun onProgress(progress: Int) {
						super.onProgress(progress)
						mTemplateLoadResult.postValue(templateLoadResult.setState(BaseResultData.PROGRESS).setProgress(progress.toFloat()))
					}
				})
				val result = if (template.zipPath == templateItem.zipPath)
					Result.failure(Throwable("load template fail")) else Result.success(template)
				mTemplateLoadResult.value = templateLoadResult.setState(BaseResultData.END).setResultData(result)
			}
		}
	}

	override fun onCleared() {
		super.onCleared()
		mScope.cancel()
	}
}