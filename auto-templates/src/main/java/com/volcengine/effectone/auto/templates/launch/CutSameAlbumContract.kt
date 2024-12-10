package com.volcengine.effectone.auto.templates.launch

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.result.contract.ActivityResultContract
import com.cutsame.solution.template.model.TemplateItem
import com.ss.android.ugc.cut_ui.MediaItem
import com.volcengine.effectone.auto.common.cutsame.CutSameContract
import com.volcengine.effectone.auto.templates.ui.AutoComposeActivity

class CutSameComposeContract :ActivityResultContract<Pair<ArrayList<MediaItem>,TemplateItem>,ArrayList<MediaItem>>(){

    private lateinit var slotsItems: ArrayList<MediaItem>
    override fun createIntent(
        context: Context,
        input: Pair<ArrayList<MediaItem>, TemplateItem>
    ): Intent {
        slotsItems = input.first
        val templateItem = input.second
        return AutoComposeActivity.createIntent(context,slotsItems,templateItem)
    }


    override fun parseResult(resultCode: Int, intent: Intent?): ArrayList<MediaItem> {
      val result =  intent?.takeIf {  resultCode == Activity.RESULT_OK }?.let {
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
               it.getParcelableArrayListExtra(CutSameContract.ARG_DATA_COMPRESS_RESULT_MEDIA_ITEMS,MediaItem::class.java)
           } else {
               it.getParcelableArrayListExtra<MediaItem>(CutSameContract.ARG_DATA_COMPRESS_RESULT_MEDIA_ITEMS)
           }
       }
        return result ?: arrayListOf()
    }

}
