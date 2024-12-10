package com.volcengine.effectone.auto.moment.hl.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.NestedScrollView
import com.volcengine.ck.highlight.config.HLExtractorRequirement
import com.volcengine.ck.highlight.ila.ILASDKInit
import com.volcengine.ck.logkit.LogKit
import com.volcengine.effectone.auto.common.extention.onApplyWindowInsetsListener
import com.volcengine.effectone.auto.common.extention.textChangesFlow
import com.volcengine.effectone.auto.common.extention.updateViewPadding
import com.volcengine.effectone.auto.moment.R
import com.volcengine.effectone.auto.moment.hl.config.HLExtConfig
import com.volcengine.effectone.auto.moment.hl.data.AutoMaterialRecognizeMedia
import com.volcengine.effectone.auto.moment.hl.play.AutoMaterialHighLightPlayActivity
import com.volcengine.effectone.auto.moment.hl.player.AutoMaterialPlayerConfig
import com.volcengine.effectone.extensions.setNoDoubleClickListener
import com.volcengine.effectone.singleton.AppSingleton
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext


class AutoMaterialHighLightExtractDiaLog private constructor(private val contextValue: Context) :
    AppCompatDialog(contextValue, com.volcengine.effectone.auto.common.R.style.AutoTranslucentDialog) {

    private val mainScope by lazy { object :CoroutineScope{
        override val coroutineContext: CoroutineContext
            get() = Dispatchers.Main + SupervisorJob() + CoroutineName(TAG)

    } }
    private lateinit var autoMaterialRecognizeMedia: AutoMaterialRecognizeMedia

    private var highLightEditText:AppCompatEditText? = null
    private var categoryEditText:AppCompatEditText? = null
    private var recognizeNext :View? = null
    private var recognizeScroll: NestedScrollView? = null
    private var isImeVisible = false
    private var imeHeight = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_auto_material_recognize_hight_light)
        initView()
    }


    private fun findFocusedTarget() {
        categoryEditText?.run {
            scrollFocusTargetView(hasFocus())
        }
        highLightEditText?.run {
            scrollFocusTargetView(hasFocus())
        }
    }

    private fun initView() {
        recognizeScroll =  findViewById<NestedScrollView>(R.id.auto_material_recognize_scroll)?.apply {
            onApplyWindowInsetsListener { paddingBottom,imeVisible ->
                isImeVisible = imeVisible
                imeHeight = paddingBottom
                this.post {
                    updateViewPadding(bottom = paddingBottom)
                    this.takeIf { imeVisible }?.smoothScrollTo(0, this.top)?:run {
                        findFocusedTarget()
                    }
                }
            }
            setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                LogKit.d(
                    TAG,
                    "onScrollChange() called with: scrollX = $scrollX, scrollY = $scrollY, oldScrollX = $oldScrollX, oldScrollY = $oldScrollY"
                )
            })
        }
        findViewById<View>(R.id.auto_material_recognize_hl_close)?.setNoDoubleClickListener {
            dismiss()
        }
        findViewById<TextView>(R.id.auto_material_recognize_hl_duration)?.run {
            text = autoMaterialRecognizeMedia.recognizeMedia.duration.toString()
        }
        findViewById<AppCompatEditText>(R.id.auto_material_recognize_high_light_et)?.run {
            highLightEditText = this
            val duration = autoMaterialRecognizeMedia.recognizeMedia.duration
            filters =
                arrayOf(MaxDigitsDurationFilter(duration) {
                    Toast.makeText(AppSingleton.instance, "输入高光时长$it, ${if (it <= 0) "需要大于 0" else "不允许超过视频时长$duration"} ", Toast.LENGTH_SHORT).show()
                })
            mainScope.launch {
                textChangesFlow().collectLatest {

                }
            }
            onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                LogKit.d(TAG, "initView() 高光时长 with: hasFocus = $hasFocus")
                scrollFocusTargetView(hasFocus)
            }

        }
        findViewById<AppCompatEditText>(R.id.auto_material_recognize_category_et)?.apply {
            categoryEditText = this
            setOnEditorActionListener { _, actionId, _ ->
                //输入关键词时，点击了键盘的actionDone，触发下一步按钮点击
                if(EditorInfo.IME_ACTION_DONE == actionId){
                    recognizeNext?.performClick()
                    true
                }
                false
            }
            mainScope.launch {
                textChangesFlow().collectLatest {

                }
            }

            onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                LogKit.d(TAG, "initView()关键词 with: hasFocus = $hasFocus")
                scrollFocusTargetView(hasFocus)
            }
        }
        findViewById<View>(R.id.auto_material_recognize_cancel)?.setNoDoubleClickListener {
            dismiss()
        }

        recognizeNext = findViewById<View>(R.id.auto_material_recognize_next)?.apply {
            setNoDoubleClickListener {
            val highLightDuration = highLightEditText?.text?.trim()
            val categoryInput = categoryEditText?.text?.trim()?:""
            val categoryList =
                categoryInput.trim().split(" ").filter { it.isNotBlank() } as ArrayList<String>
            if (highLightDuration.isNullOrEmpty()) {
                Toast.makeText(AppSingleton.instance, "请输入高光时长", Toast.LENGTH_SHORT).show()
                highLightEditText?.requestFocus()
                return@setNoDoubleClickListener
            }else{
                val requireTime = highLightDuration.toString().toIntOrNull() ?: 0
                launchPlay(contextValue,mainScope, requireTime, categoryList, autoMaterialRecognizeMedia)
            }
          }
        }
    }

    private fun AppCompatEditText.scrollFocusTargetView(hasFocus: Boolean) {
        recognizeScroll?.takeIf { hasFocus }?.let {
            val (_, y) = IntArray(2).also {
                this.getLocationInWindow(it)
            }
            LogKit.d(TAG, "scrollFocusTargetView() called y = $y")
            it.post { it.smoothScrollTo(0, y)}
        }
    }

    override fun dismiss() {
        super.dismiss()
        mainScope.cancel()
    }

    private  fun launchPlay(
        context: Context, scope: CoroutineScope,
        requireTime: Int, categoryList: ArrayList<String>, media: AutoMaterialRecognizeMedia
    ) {
        val hlResults = media.hlResults ?: return
        scope.launch(Dispatchers.IO) {
            val recognizeMedia = media.recognizeMedia
            val requirement = HLExtractorRequirement(requireTime, categoryList)
            val result = ILASDKInit.extract(requirement, hlResults, media.recognizeMedia.duration)
            withContext(Dispatchers.Main) {
                dismiss()
                val intent = Intent(context, AutoMaterialHighLightPlayActivity::class.java).apply {
                    putExtra(HLExtConfig.VIDEO_PATH, media.recognizeMedia.path)
                    putExtra(HLExtConfig.HL_START_TIME, result.startTime)
                    putExtra(HLExtConfig.HL_END_TIME, result.endTime)
                    putExtra(AutoMaterialPlayerConfig.HL_VIDEO_DURATION, recognizeMedia.duration)
                    putExtra(AutoMaterialPlayerConfig.HL_VIDEO_WIDTH, recognizeMedia.width)
                    putExtra(AutoMaterialPlayerConfig.HL_VIDEO_HEIGHT, recognizeMedia.height)

                    putStringArrayListExtra(HLExtConfig.HL_CATEGORY_LIST, categoryList)
                }
                context.startActivity(intent)
            }
        }
    }

    class MaxDigitsDurationFilter(private val maxDuration: Long, private val reachAction: (Int) -> Unit) :
        InputFilter {
        override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
            try {
                if (!TextUtils.isDigitsOnly(source)) {
                    return ""
                }
                val input = (dest.toString() + source.toString()).toInt()
                if (input<=0 || input > maxDuration) {
                    reachAction(input)
                    return ""
                }
            } catch (_: NumberFormatException) {
            }
            return null
        }

    }
    private fun setAutoMaterialRecognizeMedia(autoMaterialRecognizeMedia: AutoMaterialRecognizeMedia) {
        this.autoMaterialRecognizeMedia = autoMaterialRecognizeMedia
    }

    companion object {
        const val ARGUMENTS_KEY_AUTO_MATERIAL_MEDIA = "arguments_key_auto_material_media"
        const val TAG = "AutoMaterialHighLightExtractFragment"

        @JvmStatic
        @JvmOverloads
        fun createAutoMaterialHighLightExtractDiaLog(
            context: Context,
            autoMaterialRecognizeMedia: AutoMaterialRecognizeMedia,
            cancelable: Boolean = true,
            touchOutSideCancelable: Boolean = true,
        ): AutoMaterialHighLightExtractDiaLog {
            return AutoMaterialHighLightExtractDiaLog(context).apply {
                setCancelable(cancelable)
                setCanceledOnTouchOutside(touchOutSideCancelable)
                setAutoMaterialRecognizeMedia(autoMaterialRecognizeMedia)
                create()
            }
        }
    }

}