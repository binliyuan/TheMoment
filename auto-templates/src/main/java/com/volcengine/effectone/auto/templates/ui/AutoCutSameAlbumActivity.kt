package com.volcengine.effectone.auto.templates.ui

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import com.gyf.immersionbar.ktx.immersionBar
import com.ss.android.ugc.tools.view.activity.AVActivityOnKeyDownListener
import com.ss.android.ugc.tools.view.activity.AVActivityResultListener
import com.ss.android.ugc.tools.view.activity.AVListenableActivityRegistry
import com.volcengine.ck.album.init.AlbumInit
import com.volcengine.effectone.auto.templates.R

class AutoCutSameAlbumActivity : AppCompatActivity(), AVListenableActivityRegistry {

    companion object {
        private const val TAG = "CutSameAlbumActivity"
    }

    private val activityResultListeners = ArrayList<AVActivityResultListener>()
    private val activityOnKeyDownListeners = ArrayList<AVActivityOnKeyDownListener>()

    private val autoCutSameAlbumFragment by lazy {
        AutoCutSameAlbumFragment().apply {
            arguments = intent.extras
        }
    }

    private val cutSameBottomDockerFragment by lazy {
        CutSameBottomDockerFragment().apply {
            arguments = intent.extras
            registerActivityOnKeyDownListener(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        overridePendingTransition(com.volcengine.effectone.auto.album.R.anim.bottom_in, 0)
        immersionBar {
            transparentStatusBar()
            statusBarDarkFont(false)
            navigationBarColor(R.color.ConstBGInverse)
        }
        AlbumInit.init(application)

        setContentView(R.layout.cutsame_activity_album)

        val currentFragment = supportFragmentManager.findFragmentByTag(TAG)
        if (currentFragment == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.auto_cutsame_album_content, autoCutSameAlbumFragment, TAG)
                .replace(R.id.auto_cutsame_album_bottom_docker, cutSameBottomDockerFragment,
                    CutSameBottomDockerFragment.TAG
                )
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
        overridePendingTransition(0, com.volcengine.effectone.auto.album.R.anim.bottom_out)
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