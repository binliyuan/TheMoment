package com.volcengine.effectone.auto.moment

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.gyf.immersionbar.ktx.immersionBar
import com.volcengine.ck.album.AlbumEntrance
import com.volcengine.effectone.utils.EOUtils

class AutoMomentHomeActivity : AppCompatActivity() {

    private val albumPermissions by lazy {
        mutableListOf<String>().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
                add(Manifest.permission.READ_MEDIA_VIDEO)
                add(Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        immersionBar {
            transparentStatusBar()
            statusBarDarkFont(false)
            navigationBarColor(R.color.ConstBGInverse)
        }

        setContentView(R.layout.activity_auto_moment_home)

        findViewById<View>(R.id.auto_iv_back).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.auto_open_moment).setOnClickListener {
            EOUtils.permission.checkPermissions(this, albumPermissions, {
                startActivity(Intent(this, AutoMomentListActivity::class.java))
            }, {
                AlbumEntrance.showAlbumPermissionTips(this)
            })
        }

        findViewById<View>(R.id.auto_config_moment).setOnClickListener {
            startActivity(Intent(this, AutoMomentConfigureActivity::class.java))
        }
    }
}