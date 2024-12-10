package com.volcengine.effectone.auto.moment.hl.detail

import android.content.Context
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import androidx.viewpager2.widget.ViewPager2
import com.bytedance.creativex.visibleOrGone
import com.volcengine.ck.highlight.data.HLResult
import com.volcengine.ck.highlight.utils.isVideo
import com.volcengine.ck.logkit.LogKit
import com.volcengine.effectone.auto.moment.R
import com.volcengine.effectone.auto.moment.hl.adapter.AutoMaterialRecognizeFrameAdapter
import com.volcengine.effectone.auto.moment.hl.data.AutoMaterialRecognizeMedia
import com.volcengine.effectone.extensions.setNoDoubleClickListener


class AutoMaterialRecognizeDetailDiaLog private constructor(context: Context) :
    AppCompatDialog(context, com.volcengine.effectone.auto.common.R.style.AutoTranslucentDialog) {
    private val autoMaterialRecognizeFrameAdapter by lazy { AutoMaterialRecognizeFrameAdapter() }

    private var viewPager2: ViewPager2? = null
    private var preButton: View? = null
    private var nextButton: View? = null
    private var categoryInfoDetail: TextView? = null
    private var scoreInfoDetail: TextView? = null
    private lateinit var autoMaterialRecognizeMedia: AutoMaterialRecognizeMedia
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_auto_material_recognize_detail)
        initView()
        initData()
    }

    private fun initData() {
        autoMaterialRecognizeFrameAdapter.updateItem(autoMaterialRecognizeMedia)
    }
    private fun updateCurTabData(curPos: Int) {
        val hlResults = autoMaterialRecognizeMedia.hlResults ?: return
        val totalFramesSize = hlResults.size
        if (totalFramesSize < 2) {
            preButton?.visibleOrGone = false
            nextButton?.visibleOrGone = false
            return
        }
        preButton?.visibleOrGone = curPos in 1 until totalFramesSize

        nextButton?.visibleOrGone = curPos in 0 until totalFramesSize - 1

        categoryInfoDetail?.text =
            hlResults.markCategoryInfo(curPos)
        scoreInfoDetail?.text = hlResults.markScoreInfo(curPos)
    }

    private fun initView() {
        findViewById<View>(R.id.auto_material_recognize_detail_close)?.setNoDoubleClickListener {
            dismiss()
        }

        viewPager2 =
            findViewById<ViewPager2>(R.id.auto_material_recognize_detail_frame_viewpager)?.apply {
                isUserInputEnabled =
                    autoMaterialRecognizeMedia.recognizeMedia.isVideo()
                adapter = autoMaterialRecognizeFrameAdapter
                offscreenPageLimit = 4
                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        updateCurTabData(position)
                    }

                    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) =Unit
                })
            }
        categoryInfoDetail =
            findViewById<TextView>(R.id.auto_material_recognize_detail_category_info_detail)?.apply {
                movementMethod = ScrollingMovementMethod.getInstance()
                text = autoMaterialRecognizeMedia.hlResults?.markCategoryInfo()
            }
        scoreInfoDetail =
            findViewById<TextView>(R.id.auto_material_recognize_detail_score_info_detail)?.apply {
                movementMethod = ScrollingMovementMethod.getInstance()
                text = autoMaterialRecognizeMedia.hlResults?.markScoreInfo()
            }
        preButton = findViewById<View>(R.id.auto_material_recognize_detail_frame_pre)?.apply {
            setNoDoubleClickListener {
                viewPager2?.run {
                    setCurrentItem(this.currentItem.dec(), true)
                }
            }
        }
        nextButton = findViewById<View>(R.id.auto_material_recognize_detail_frame_next)?.apply {
            setNoDoubleClickListener {
                viewPager2?.run {
                    setCurrentItem(this.currentItem.inc(), true)
                }
            }
        }
    }

    private fun setAutoMaterialRecognizeMedia(autoMaterialRecognizeMedia: AutoMaterialRecognizeMedia) {
        this.autoMaterialRecognizeMedia = autoMaterialRecognizeMedia
    }

    companion object {
        const val TAG = "AutoMaterialRecognizeDetailFragment"

        @JvmStatic
        @JvmOverloads
        fun createAutoMaterialRecognizeDetailDiaLog(
            context: Context,
            autoMaterialRecognizeMedia: AutoMaterialRecognizeMedia,
            cancelable: Boolean = true,
            touchOutSideCancelable: Boolean = true,
        ): AutoMaterialRecognizeDetailDiaLog {
            return AutoMaterialRecognizeDetailDiaLog(context).apply {
                setCancelable(cancelable)
                setCanceledOnTouchOutside(touchOutSideCancelable)
                setAutoMaterialRecognizeMedia(autoMaterialRecognizeMedia)
                create()
            }
        }

        fun List<HLResult>.markCategoryInfo(post: Int = 0): String {
            return StringBuilder().apply {
                this@markCategoryInfo.takeIf { post in this@markCategoryInfo.indices }?.run {
                    this[post].c3.run {
                        val maxNameLength =
                            this.map { it.name }.takeIf { it.isNotEmpty() }?.maxOf { it.length }
                                ?: -1
                        takeIf { maxNameLength != -1 } ?: return ""
                        this.forEach {
                            append(it.name)
                            val repeatCount = maxNameLength - it.name.length
                            repeat(repeatCount) {
                                /*
                                    U+3000 是一个全角空格,占据一个中文字符的宽度。
                                    U+0020 是一个半角空格,占据一个英文字符的宽度。
                                    U+00A0 是一个不换行空格,在文本中不会导致换行。
                                    */
                                append("\u3000")
                            }
                            append(" 关联性: ").append(it.prob.format(4)).append("\n")
                        }
                    }
                }

                LogKit.d(TAG, "markCategoryInfo() result ${this.toString()}")
            }.toString()
        }

        fun List<HLResult>.markScoreInfo(post: Int = 0): String {
            return this.map { it.score }.takeIf { post in it.indices }
                ?.get(post)?.score.toString() ?: ""

        }

        fun Float.format(digits: Int) = "%.${digits}f".format(this)

    }
}