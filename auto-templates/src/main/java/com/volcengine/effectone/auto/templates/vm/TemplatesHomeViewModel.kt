package com.volcengine.effectone.auto.templates.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.cutsame.solution.template.model.TemplateCategory
import com.cutsame.solution.template.model.TemplateItem
import com.volcengine.effectone.InnerEffectOneConfigList
import com.volcengine.effectone.auto.templates.bean.BaseResultData
import com.volcengine.effectone.auto.templates.bean.TemplateCategoryResult
import com.volcengine.effectone.auto.templates.cutsame.TemplateConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author tyx
 * @description:
 * @date :2024/4/24 17:48
 */
class TemplatesHomeViewModel : ViewModel() {
	companion object {
		fun create(owner: ViewModelStoreOwner): TemplatesHomeViewModel {
			return ViewModelProvider(owner, ViewModelProvider.NewInstanceFactory()).get(TemplatesHomeViewModel::class.java)
		}
	}

	private val mScope by lazy { CoroutineScope(Dispatchers.Main + SupervisorJob()) }
	val templatesCategoryList = MutableLiveData<TemplateCategoryResult>()
	private val mTemplateConfig: TemplateConfig? = InnerEffectOneConfigList.getConfig()
	var mTemplatesList = listOf<TemplateItem>()

	fun loadTemplateCategoryList() {
		mScope.launch {
			val list = arrayListOf<TemplateCategory>()
			val templates = TemplateCategoryResult()
			templatesCategoryList.value = templates.setState(BaseResultData.START)
			withContext(Dispatchers.IO) {
				mTemplateConfig?.let { config ->
					val tags = config.getTemplateList().also { mTemplatesList = it }.map { it.templateTags }.distinct()
					tags.forEach { tag ->
						list.add(TemplateCategory(0, tag))
					}
				}
			}
			val result = if (list.isNotEmpty()) Result.success(list.toMutableList()) else Result.failure(Throwable("fail"))
			templatesCategoryList.value = templates.setState(BaseResultData.END).setResultData(result)
		}
	}
}