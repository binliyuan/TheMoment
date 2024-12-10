package com.volcengine.effectone.auto.common.extention

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewTreeObserver
import androidx.annotation.Px
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.volcengine.ck.logkit.LogKit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun View.onApplyWindowInsetsListener(block: (Int,Boolean) -> Unit) {

    var previousInsets: WindowInsetsCompat? = null

    ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
        if (previousInsets == null || insets != previousInsets) {
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val naviBarVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val bottomPadding = if (naviBarVisible) imeHeight - navigationBarHeight else imeHeight
            block(bottomPadding,imeVisible)
            LogKit.d(
                "onApplyWindowInsetsListener",
                "imeVisible:$imeVisible imeHeight:$imeHeight navigationBarHeight:$navigationBarHeight"
            )
        }
        insets
    }
}

fun View.updateViewPadding(
    @Px left: Int = paddingLeft,
    @Px top: Int = paddingTop,
    @Px right: Int = paddingRight,
    @Px bottom: Int = paddingBottom
) {
    setPadding(left, top, right, bottom)
}

fun View.updateViewMargin(
    @Px marginStart: Int = (layoutParams as? MarginLayoutParams)?.marginStart ?: 0,
    @Px topMargin: Int = (layoutParams as? MarginLayoutParams)?.topMargin ?: 0,
    @Px marginEnd: Int = (layoutParams as? MarginLayoutParams)?.marginEnd ?: 0,
    @Px bottomMargin: Int = (layoutParams as? MarginLayoutParams)?.bottomMargin ?: 0
) {
    (layoutParams as? MarginLayoutParams)?.apply {
        this.marginStart = marginStart
        this.topMargin = topMargin
        this.marginEnd = marginEnd
        this.bottomMargin = bottomMargin
        layoutParams = this
    }
}

fun AppCompatEditText.textChangesFlow(): Flow<String> {
    return callbackFlow<String> {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                trySend(s.toString())
            }
        }
        this@textChangesFlow.addTextChangedListener(textWatcher)

        awaitClose {
            this@textChangesFlow.removeTextChangedListener(textWatcher)
        }
    }
}

fun View.applyOnGlobalLayoutListener(blockAction: (View) -> Unit) {
    this.viewTreeObserver.addOnGlobalLayoutListener(object :ViewTreeObserver.OnGlobalLayoutListener{
        override fun onGlobalLayout() {
            this@applyOnGlobalLayoutListener.viewTreeObserver.removeOnGlobalLayoutListener(this)
            blockAction(this@applyOnGlobalLayoutListener)
        }
    })
}

fun View.applyLocationParentOnGlobalLayoutListener(locationParentAction: (Int, Int) -> Unit) {
    this.applyOnGlobalLayoutListener{view->
        val (x, y) = IntArray(2).also {
            view.getLocationInWindow(it)
        }
        locationParentAction(x,y)

    }
}
