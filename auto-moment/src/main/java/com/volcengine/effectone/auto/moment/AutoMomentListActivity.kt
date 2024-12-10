package com.volcengine.effectone.auto.moment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.gyf.immersionbar.ktx.immersionBar

class AutoMomentListActivity : AppCompatActivity() {

    private val autoMomentFragment by lazy {
        AutoMomentFragment().apply {
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

        setContentView(R.layout.activity_auto_moment_list)

        supportFragmentManager.beginTransaction()
            .replace(R.id.auto_moment_fragment_container, autoMomentFragment)
            .commitNow()
    }
}