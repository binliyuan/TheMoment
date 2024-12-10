package com.volcengine.effectone.auto.recorder

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.gyf.immersionbar.ktx.immersionBar
import com.volcengine.effectone.auto.recorder.config.AutoRecordConstant
import com.volcengine.effectone.resource.api.EOResourceManager

class AutoRecordActivity : AppCompatActivity() {

    companion object {
        fun startRecord(activity: Activity, type: Int) {
            if (!EOResourceManager.isDefaultResourcesReady()) {
                Log.e("RecordActivity", "StartRecord Failed: default resources loading is not finished.")
                return
            }
            val options = ActivityOptionsCompat.makeCustomAnimation(activity, R.anim.eo_recorder_bottom_in, 0)
            val intent = Intent(activity, AutoRecordActivity::class.java)
            intent.putExtra(AutoRecordConstant.TYPE_KEY, type)
            activity.startActivity(intent, options.toBundle())
        }
    }

    private val autoRecordFragment by lazy {
        AutoRecordFragment().apply {
            arguments = intent.extras
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        immersionBar {
            statusBarColor(R.color.BGPrimary)
            navigationBarColor(R.color.BGPrimary)
            statusBarDarkFont(false)
            fitsSystemWindows(true)
        }

        setContentView(R.layout.auto_recorder_activity_layout)

        supportFragmentManager.beginTransaction()
            .replace(R.id.auto_record_activity_root, autoRecordFragment)
            .commitNow()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.eo_recorder_bottom_out)
    }

}