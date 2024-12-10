package com.volcengine.effectone.auto.moment.hl

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bytedance.creativex.mediaimport.repository.api.IMaterialItem
import com.gyf.immersionbar.ktx.immersionBar
import com.volcengine.ck.logkit.LogKit
import com.volcengine.effectone.auto.moment.R
import com.volcengine.effectone.auto.moment.hl.launcher.AutoMaterialRecognizeAlbumLauncher

class AutoMaterialRecognizeActivity : AppCompatActivity() {

    private val autoMaterialRecognizeFragment by lazy {
        AutoMaterialRecognizeFragment().apply {
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

        setupFragment(savedInstanceState)
    }

    private fun setupFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            val fragment =
                supportFragmentManager.findFragmentByTag(AutoMaterialRecognizeFragment.TAG)
                    ?: autoMaterialRecognizeFragment
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, fragment, AutoMaterialRecognizeFragment.TAG)
                .commitNow()
        } else {
            LogKit.d(TAG, "onCreate : savedInstanceState = $savedInstanceState ,recreate ")
        }
    }

    companion object{
        @JvmStatic
        fun launchPage(activity: Activity, mediaList: List<IMaterialItem>) {
           activity.startActivity(Intent(activity,AutoMaterialRecognizeActivity::class.java).apply {
               putParcelableArrayListExtra(AutoMaterialRecognizeAlbumLauncher.ARGUMENT_KEY_MEDIA_LIST, ArrayList(mediaList))
           })
        }

        private const val TAG = "AutoMaterialRecognizeActivity"
    }
}