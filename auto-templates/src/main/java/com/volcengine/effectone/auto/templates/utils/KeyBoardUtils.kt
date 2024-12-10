package com.volcengine.effectone.auto.templates.utils

import android.app.Activity
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.gyf.immersionbar.ImmersionBar
import com.volcengine.effectone.singleton.AppSingleton
import kotlin.math.abs

/**
 * @author tyx
 * @description:
 * @date :2024/5/24 14:38
 */
object KeyBoardUtils {
	private const val TAG_ON_GLOBAL_LAYOUT_LISTENER = -8
	private var millis: Long = 0
	private var sDecorViewDelta = 0
	fun showSoftInput() {
		val imm = ContextCompat.getSystemService(AppSingleton.instance, InputMethodManager::class.java)
		imm?.toggleSoftInput(2, 1)
	}

	fun showSoftInput(activity: Activity) {
		if (!isSoftInputVisible(activity)) {
			toggleSoftInput()
		}
	}

	fun showSoftInput(view: View, flags: Int = 0) {
		val imm = ContextCompat.getSystemService(AppSingleton.instance, InputMethodManager::class.java)
		if (imm != null) {
			view.isFocusable = true
			view.setFocusableInTouchMode(true)
			view.requestFocus()
			imm.showSoftInput(view, flags, object : ResultReceiver(Handler(Looper.getMainLooper())) {
				override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
					if (resultCode == 1 || resultCode == 3) {
						toggleSoftInput()
					}
				}
			})
			imm.toggleSoftInput(2, 1)
		}
	}

	fun hideSoftInput(activity: Activity) {
		hideSoftInput(activity.window)
	}

	fun hideSoftInput(window: Window) {
		var view = window.currentFocus
		if (view == null) {
			val decorView = window.decorView
			val focusView = decorView.findViewWithTag<View>("keyboardTagView")
			if (focusView == null) {
				view = EditText(window.context)
				(view as View).tag = "keyboardTagView"
				(decorView as ViewGroup).addView(view as View?, 0, 0)
			} else {
				view = focusView
			}
			view.requestFocus()
		}
		hideSoftInput(view)
	}

	fun hideSoftInput(view: View) {
		val imm = ContextCompat.getSystemService(AppSingleton.instance, InputMethodManager::class.java)
		imm?.hideSoftInputFromWindow(view.windowToken, 0)
	}

	fun hideSoftInputByToggle(activity: Activity) {
		val nowMillis = SystemClock.elapsedRealtime()
		val delta = nowMillis - millis
		if (abs(delta.toDouble()) > 500L && isSoftInputVisible(activity)) {
			toggleSoftInput()
		}
		millis = nowMillis
	}

	fun toggleSoftInput() {
		val imm = ContextCompat.getSystemService(AppSingleton.instance, InputMethodManager::class.java)
		imm?.toggleSoftInput(0, 0)
	}

	fun isSoftInputVisible(activity: Activity): Boolean {
		return getDecorViewInvisibleHeight(activity.window) > 0
	}

	private fun getDecorViewInvisibleHeight(window: Window): Int {
		val decorView = window.decorView
		val outRect = Rect()
		decorView.getWindowVisibleDisplayFrame(outRect)
		Log.d("KeyboardUtils", "getDecorViewInvisibleHeight: " + (decorView.bottom - outRect.bottom))
		val delta = abs((decorView.bottom - outRect.bottom).toDouble()).toInt()
		return if (delta <= ImmersionBar.getNavigationBarHeight(decorView.context) + ImmersionBar.getStatusBarHeight(decorView.context)) {
			sDecorViewDelta = delta
			0
		} else {
			delta - sDecorViewDelta
		}
	}

	fun registerSoftInputChangedListener(activity: Activity, listener: OnSoftInputChangedListener) {
		registerSoftInputChangedListener(activity.window, listener)
	}

	fun registerSoftInputChangedListener(window: Window, listener: OnSoftInputChangedListener) {
		val flags = window.attributes.flags
		if (flags and 512 != 0) {
			window.clearFlags(512)
		}
		val contentView = window.findViewById<View>(android.R.id.content) as FrameLayout
		val decorViewInvisibleHeightPre = intArrayOf(getDecorViewInvisibleHeight(window))
		val onGlobalLayoutListener = OnGlobalLayoutListener {
			val height = getDecorViewInvisibleHeight(window)
			if (decorViewInvisibleHeightPre[0] != height) {
				listener.onSoftInputChanged(height)
				decorViewInvisibleHeightPre[0] = height
			}
		}
		contentView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener)
		contentView.setTag(-8, onGlobalLayoutListener)
	}

	fun unregisterSoftInputChangedListener(window: Window) {
		val contentView = window.findViewById<View>(android.R.id.content)
		if (contentView != null) {
			val tag = contentView.getTag(-8)
			if (tag is OnGlobalLayoutListener) {
				contentView.getViewTreeObserver().removeOnGlobalLayoutListener(tag)
			}
		}
	}

	fun fixAndroidBug5497(activity: Activity) {
		fixAndroidBug5497(activity.window)
	}

	fun fixAndroidBug5497(window: Window) {
		val softInputMode = window.attributes.softInputMode
		window.setSoftInputMode(softInputMode and -17)
		val contentView = window.findViewById<View>(android.R.id.content) as FrameLayout
		val contentViewChild = contentView.getChildAt(0)
		val paddingBottom = contentViewChild.paddingBottom
		val contentViewInvisibleHeightPre5497 = intArrayOf(getContentViewInvisibleHeight(window))
		contentView.getViewTreeObserver().addOnGlobalLayoutListener {
			val height = getContentViewInvisibleHeight(window)
			if (contentViewInvisibleHeightPre5497[0] != height) {
				contentViewChild.setPadding(
					contentViewChild.getPaddingLeft(),
					contentViewChild.paddingTop,
					contentViewChild.getPaddingRight(),
					paddingBottom + getDecorViewInvisibleHeight(window)
				)
				contentViewInvisibleHeightPre5497[0] = height
			}
		}
	}

	private fun getContentViewInvisibleHeight(window: Window): Int {
		val contentView = window.findViewById<View>(android.R.id.content)
		return if (contentView == null) {
			0
		} else {
			val outRect = Rect()
			contentView.getWindowVisibleDisplayFrame(outRect)
			Log.d("KeyboardUtils", "getContentViewInvisibleHeight: " + (contentView.bottom - outRect.bottom))
			val delta = abs((contentView.bottom - outRect.bottom).toDouble()).toInt()
			if (delta <= ImmersionBar.getStatusBarHeight(contentView.context) + ImmersionBar.getNavigationBarHeight(contentView.context)) 0 else delta
		}
	}

	fun fixSoftInputLeaks(activity: Activity) {
		fixSoftInputLeaks(activity.window)
	}

	fun fixSoftInputLeaks(window: Window) {
		val imm = ContextCompat.getSystemService(AppSingleton.instance, InputMethodManager::class.java)
		if (imm != null) {
			val leakViews = arrayOf("mLastSrvView", "mCurRootView", "mServedView", "mNextServedView")
			val var4 = leakViews.size
			for (leakView in leakViews) {
				try {
					val leakViewField = InputMethodManager::class.java.getDeclaredField(leakView)
					if (!leakViewField.isAccessible) {
						leakViewField.isAccessible = true
					}
					val obj = leakViewField[imm]
					if (obj is View) {
						if (obj.getRootView() === window.decorView.getRootView()) {
							leakViewField[imm] = null as Any?
						}
					}
				} catch (ignored: Throwable) {
				}
			}
		}
	}

	fun clickBlankArea2HideSoftInput() {
		Log.i("KeyboardUtils", "Please refer to the following code.")
	}

	interface OnSoftInputChangedListener {
		fun onSoftInputChanged(var1: Int)
	}
}
