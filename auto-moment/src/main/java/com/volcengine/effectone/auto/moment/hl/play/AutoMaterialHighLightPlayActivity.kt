package com.volcengine.effectone.auto.moment.hl.play

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gyf.immersionbar.ktx.immersionBar
import com.volcengine.ck.logkit.LogKit
import com.volcengine.effectone.auto.moment.R

class AutoMaterialHighLightPlayActivity : AppCompatActivity() {
    private val autoMaterialRecognizePlayFragment by lazy {
        AutoMaterialHighLightPlayFragment().apply {
            arguments = intent.extras
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersionBar {
            transparentStatusBar()
            statusBarDarkFont(false)
            navigationBarColor(R.color.ConstBGInverse)
        }
        if (savedInstanceState == null) {
            val fragment =
                supportFragmentManager.findFragmentByTag(AutoMaterialHighLightPlayFragment.TAG)
                    ?: autoMaterialRecognizePlayFragment
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, fragment, AutoMaterialHighLightPlayFragment.TAG)
                .commitNow()
        } else {
            LogKit.d(TAG, "onCreate : savedInstanceState = $savedInstanceState ,recreate ")
        }
    }

    companion object {
        private const val TAG = "AutoMaterialHighLightPlayActivity"
    }
}