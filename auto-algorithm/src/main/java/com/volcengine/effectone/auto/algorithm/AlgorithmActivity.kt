package com.volcengine.effectone.auto.algorithm

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.gyf.immersionbar.ktx.immersionBar
import com.volcengine.effectone.recordersdk.arsdk.algorithm.base.AlgorithmTaskKey

class AlgorithmActivity : AppCompatActivity() {
    companion object {
        fun startActivity(activity: Activity, algoKey: AlgorithmTaskKey) {
            val options = ActivityOptionsCompat.makeCustomAnimation(activity, com.volcengine.effectone.auto.recorder.R.anim.eo_recorder_bottom_in, 0)

            val intent = Intent(activity, AlgorithmActivity::class.java)
            intent.putExtra("algoKey", algoKey.key)
            activity.startActivity(intent, options.toBundle())
        }
    }

    private val algorithmFragment by lazy {
        AlgorithmFragment().apply {
            arguments = intent.extras
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        immersionBar {
            statusBarColor(com.volcengine.effectone.auto.recorder.R.color.BGPrimary)
            navigationBarColor(com.volcengine.effectone.auto.recorder.R.color.BGPrimary)
            statusBarDarkFont(false)
            fitsSystemWindows(true)
        }

        setContentView(com.volcengine.effectone.auto.recorder.R.layout.auto_recorder_activity_layout)

        supportFragmentManager.beginTransaction()
            .replace(com.volcengine.effectone.auto.recorder.R.id.auto_record_activity_root, algorithmFragment)
            .commitNow()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, com.volcengine.effectone.auto.recorder.R.anim.eo_recorder_bottom_out)
    }
}