package com.volcengine.effectone.auto.business.hl

import android.app.Activity
import android.widget.Toast
import androidx.annotation.Keep
import com.bytedance.creativex.mediaimport.repository.api.IMaterialItem
import com.cutsame.solution.template.model.TemplateItem
import com.volcengine.ck.album.api.IAlbumFinish
import com.volcengine.ck.album.base.AlbumConfig
import com.volcengine.ck.highlight.HLLog
import com.volcengine.ck.highlight.ila.ILASDKInit
import com.volcengine.effectone.InnerEffectOneConfigList
import com.volcengine.effectone.auto.common.widget.AutoLoadingDialog
import com.volcengine.effectone.auto.moment.utils.toMediaItem
import com.volcengine.effectone.auto.moment.utils.toRecognizeMedia
import com.volcengine.effectone.auto.templates.bean.TemplateByMedias
import com.volcengine.effectone.auto.templates.cutsame.TemplateConfig
import com.volcengine.effectone.auto.templates.ui.AutoMomentCutSameActivity
import com.volcengine.effectone.singleton.AppSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 *Author: gaojin
 *Time: 2023/7/11 17:48
 *HLM: High Light Movie
 */

@Keep
class AutoHLMFinishImpl : IAlbumFinish {

    private val mTemplateConfig: TemplateConfig? = InnerEffectOneConfigList.getConfig()

    override suspend fun finishAction(activity: Activity, mediaList: List<IMaterialItem>, albumConfig: AlbumConfig) {
        val loadingDialog = AutoLoadingDialog.Builder(activity).setTipMsg("加载中...").create()
        loadingDialog.show()
        HLLog.i(HLLog.HLM_PERF, "==========一键成片开始==========")
        //开始匹配逻辑
        val matchedInfo = withContext(Dispatchers.IO) {
            val list = mediaList.map { it.toRecognizeMedia() }
            ILASDKInit.getTemplateFinder().findSuitedTemplate(
                ILASDKInit.recognizeMedias(list),
                ILASDKInit.getTemplateProvider()
            )
        }
        if (!loadingDialog.isShowing) {
            return
        }

        if (matchedInfo.isNotEmpty()) {
            val data = arrayListOf<TemplateByMedias>()
            matchedInfo.forEach { info ->
                val templateItem = info.template.any as? TemplateItem
                if (templateItem != null) {
                    withContext(Dispatchers.IO) {
                        val result = mTemplateConfig!!.loadTemplateResource(templateItem)
                        data.add(TemplateByMedias(result, info.mediaList.map { it.toMediaItem() }))
                    }
                }
            }
            loadingDialog.dismiss()
            if (data.isNotEmpty()) {
                val intent = AutoMomentCutSameActivity.createIntent(activity, data)
                activity.startActivity(intent)
            } else {
                Toast.makeText(AppSingleton.instance, "模板配置错误", Toast.LENGTH_SHORT).show()
            }
        } else {
            loadingDialog.dismiss()
            Toast.makeText(AppSingleton.instance, "未匹配到模板", Toast.LENGTH_SHORT).show()
        }
    }
}