package com.volcengine.effectone.auto.templates.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.cutsame.solution.template.model.TemplateItem

/**
 * @author tyx
 * @description: 模板切换
 * @date :2024/4/26 17:43
 */
class TemplatesSelectViewModel : ViewModel() {

	companion object {
		fun get(owner: ViewModelStoreOwner): TemplatesSelectViewModel {
			return ViewModelProvider(owner).get(TemplatesSelectViewModel::class.java)
		}
	}

	val mStartSwitchTemplate = MutableLiveData<TemplateItem>()     //开始切换模板
	val mIsShowTemplatePanel = MutableLiveData<Boolean>()          //是否显示切换模板
	val mShowTemplatePanelProgress = MutableLiveData<Float>()
}