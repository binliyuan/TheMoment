package com.volcengine.effectone.auto.templates.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gyf.immersionbar.ktx.immersionBar

/**
 * @author tyx
 * @description:
 * @date :2024/4/24 11:45
 */
class AutoTemplatesHomeActivity : AppCompatActivity() {

	companion object {
		private const val TAG = "TemplatesActivity"
		fun launch(context: Context) {
			val intent = Intent(context, AutoTemplatesHomeActivity::class.java)
			context.startActivity(intent)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		immersionBar {
			transparentStatusBar()
			navigationBarColor(com.gyf.immersionbar.R.color.abc_decor_view_status_guard)
			statusBarDarkFont(false)
			fitsSystemWindows(true)
		}

		val fragment = supportFragmentManager.findFragmentByTag("TemplatesHomeFragment") ?: AutoTemplatesHomeFragment()
		supportFragmentManager.beginTransaction()
			.replace(android.R.id.content, fragment, TAG)
			.commitNow()
	}
}