package com.volcengine.effectone.auto.business

import android.os.Bundle
import android.os.RemoteException
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gyf.immersionbar.ktx.immersionBar

class EffectAutoMainActivity : AppCompatActivity() {

    companion object {
        const val TOAST_SHOW_DURATION = 2000L
    }

    private val effectAutoMainFragment by lazy { EffectAutoMainFragment() }

    private var boo: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersionBar {
            transparentStatusBar()
            navigationBarColor(R.color.ConstBGInverse)
            statusBarDarkFont(false)
        }

        supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, effectAutoMainFragment, EffectAutoMainFragment.TAG)
            .commitNowAllowingStateLoss()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if ((System.currentTimeMillis() - boo) > TOAST_SHOW_DURATION) {
            Toast.makeText(this, "再按一次返回键退出程序", Toast.LENGTH_SHORT).show()
            boo = System.currentTimeMillis()
        } else {
            confirmDoubleClickBack()
        }
    }

    private fun confirmDoubleClickBack() {
        if (isDestroyed) {
            return
        }
        try {
            moveTaskToBack(true)
        } catch (e: RemoteException) {
            //do nothing
        }
    }
}