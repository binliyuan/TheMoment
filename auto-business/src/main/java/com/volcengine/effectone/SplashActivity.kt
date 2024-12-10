package com.volcengine.effectone

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.volcengine.effectone.auto.business.EffectAutoMainActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent().apply {
            setClass(this@SplashActivity, EffectAutoMainActivity::class.java)
        })
        finish()
    }
}