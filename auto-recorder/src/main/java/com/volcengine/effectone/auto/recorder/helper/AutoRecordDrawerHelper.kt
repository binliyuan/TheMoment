package com.volcengine.effectone.auto.recorder.helper

import android.graphics.Color
import android.os.Looper
import android.os.MessageQueue.IdleHandler
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
import androidx.drawerlayout.widget.DrawerLayout.SimpleDrawerListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.recorder.R
import com.volcengine.effectone.auto.recorder.fragment.AutoDrawerFragment
import com.volcengine.effectone.auto.recorder.viewmodel.AutoRecordUIViewModel

class AutoRecordDrawerHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
) : IUIHelper {
    companion object {
        private const val TAG = "AutoRecordDrawerHelper"
    }

    private val autoBeautyPanelFragment by lazy {
        AutoDrawerFragment().apply {
            arguments = activity?.intent?.extras
        }
    }

    private val recordUIViewModel by lazy { AutoRecordUIViewModel.get(activity) }

    private val simpleDrawerListener by lazy {
        object : SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                attachFragmentDrawerMenuLayout()
                recordUIViewModel.drawerViewState.value = true
                onBackPressedCallback.isEnabled = true
                recordUIViewModel.rootViewVisible.value = false
            }

            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                recordUIViewModel.drawerViewState.value = false
                onBackPressedCallback.isEnabled = false
                recordUIViewModel.rootViewVisible.value = true
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                val alpha = 1.0f - slideOffset
                recordUIViewModel.leftDockerViewAlpha.value = alpha
            }
        }
    }

    private val onBackPressedCallback by lazy {
        object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(drawerView)) {
                    drawerLayout.closeDrawers()
                } else {
                    activity.onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }

    private val preLoadIdleHandler by lazy {
        IdleHandler {
            attachFragmentDrawerMenuLayout()
            false
        }
    }

    private fun attachFragmentDrawerMenuLayout() {
        var autoBeautyPanel: Fragment? =
            activity.supportFragmentManager.findFragmentByTag(AutoDrawerFragment.TAG)
        if (autoBeautyPanel == null) {
            autoBeautyPanel = autoBeautyPanelFragment
            activity.supportFragmentManager.beginTransaction().replace(
                R.id.auto_record_menu,
                autoBeautyPanel,
                AutoDrawerFragment.TAG
            ).commitNowAllowingStateLoss()
        }
    }

    init {
        activity.onBackPressedDispatcher.addCallback(owner, onBackPressedCallback)
    }

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var drawerView: View
    override fun initView(rootView: ViewGroup) {
        drawerView = rootView.findViewById(R.id.auto_record_menu)
        drawerLayout = rootView.findViewById<DrawerLayout>(R.id.auto_record_drawer).apply {
            setScrimColor(Color.TRANSPARENT)
            addDrawerListener(simpleDrawerListener)
            setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED)
        }
        initObserver()
        Looper.myQueue().addIdleHandler(preLoadIdleHandler)
    }

    private fun initObserver() {
        recordUIViewModel.toggleDrawerState.observe(owner) {
            drawerLayout.run {
                if (isDrawerOpen(drawerView)) {
                    closeDrawers()
                } else {
                    openDrawer(drawerView)
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        onBackPressedCallback.remove()
        Looper.myQueue().removeIdleHandler(preLoadIdleHandler)
    }

}