package com.volcengine.effectone.auto.templates.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cutsame.solution.template.model.TemplateItem
import com.gyf.immersionbar.ktx.immersionBar

/**
 * @author tyx
 * @description:
 * @date :2024/5/22 9:50
 */
class AutoTemplatePreviewActivity : AppCompatActivity() {

	companion object {
		const val TAG = "TemplatePreviewActivity"
		const val TEMPLATE_ITEM = "template_item"
		fun launch(context: Context, templates: TemplateItem) {
			val intent = Intent(context, AutoTemplatePreviewActivity::class.java)
			intent.putExtra(TEMPLATE_ITEM, templates)
			context.startActivity(intent)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		immersionBar {
			transparentStatusBar()
			navigationBarColor(com.gyf.immersionbar.R.color.abc_decor_view_status_guard)
			statusBarDarkFont(false)
		}

		val fragment = supportFragmentManager.findFragmentByTag(AutoTemplatePreviewFragment.TAG) ?: AutoTemplatePreviewFragment()
		fragment.arguments = intent.extras
		supportFragmentManager.beginTransaction()
			.replace(android.R.id.content, fragment, AutoTemplatePreviewFragment.TAG)
			.commitNow()
	}
}