package com.volcengine.effectone.auto.album

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import com.gyf.immersionbar.ktx.immersionBar
import com.ss.android.ugc.tools.view.activity.AVActivityOnKeyDownListener
import com.ss.android.ugc.tools.view.activity.AVActivityResultListener
import com.ss.android.ugc.tools.view.activity.AVListenableActivityRegistry
import com.volcengine.ck.album.init.AlbumInit

class AutoAlbumActivity : AppCompatActivity(), AVListenableActivityRegistry {

    companion object {
        private const val TAG = "CutSameAlbumActivity"
    }

    private val activityResultListeners = ArrayList<AVActivityResultListener>()
    private val activityOnKeyDownListeners = ArrayList<AVActivityOnKeyDownListener>()

    private val autoAlbumFragment by lazy {
        AutoAlbumFragment().apply {
            arguments = intent.extras
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        overridePendingTransition(R.anim.bottom_in, 0)

        immersionBar {
            transparentStatusBar()
            statusBarDarkFont(false)
            navigationBarColor(R.color.ConstBGInverse)
        }

        AlbumInit.init(application)

        setContentView(R.layout.auto_activity_album)

        val currentFragment = supportFragmentManager.findFragmentByTag(TAG)
        if (currentFragment == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.auto_album_root_content, autoAlbumFragment, TAG)
                .commitNowAllowingStateLoss()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        activityResultListeners.forEach {
            if (it.onActivityResult(requestCode, resultCode, data)) {
                return
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        activityOnKeyDownListeners.forEach {
            if (it.onKeyDown(keyCode, event)) {
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.bottom_out)
    }

    override fun registerActivityResultListener(listener: AVActivityResultListener) {
        activityResultListeners.add(listener)
    }

    override fun unRegisterActivityResultListener(listener: AVActivityResultListener) {
        activityResultListeners.remove(listener)
    }

    override fun registerActivityOnKeyDownListener(listener: AVActivityOnKeyDownListener) {
        activityOnKeyDownListeners.add(listener)
    }

    override fun unRegisterActivityOnKeyDownListener(listener: AVActivityOnKeyDownListener) {
        activityOnKeyDownListeners.remove(listener)
    }

    override fun registerActivityOnKeyDownListenerHead(listener: AVActivityOnKeyDownListener) {
        //do nothing
    }
}