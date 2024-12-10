package com.volcengine.effectone.auto.moment.hl.helper

import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.bytedance.creativex.visibleOrGone
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.moment.R
import com.volcengine.effectone.auto.moment.hl.vm.AutoMaterialHighLightPlayVM
import com.volcengine.effectone.auto.moment.hl.widget.AutoDynamicSizeTextView
import com.volcengine.effectone.extensions.setNoDoubleClickListener
import com.volcengine.effectone.utils.SizeUtil
import com.volcengine.effectone.widget.popover.DuxPopover
import java.util.Locale

class AutoMaterialHighLightPlayInfoHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
) : IUIHelper {
    private val autoMaterialHighLightPlayVM by lazy { AutoMaterialHighLightPlayVM.get(activity) }
    private val mPopContentView by lazy {
        val rootView = LayoutInflater.from(activity)
            .inflate(R.layout.layout_auto_material_recognize_category_info, null, false).apply {
            findViewById<AutoDynamicSizeTextView>(R.id.category_info)?.text = StringBuilder().apply {
                autoMaterialHighLightPlayVM.highLightCategoryList.forEach { append(it) }
            }
        }
        rootView
    }

    private val mDuxPopover by lazy {
        DuxPopover.Builder(activity)
            .setUseDefaultView(false)
            .setNeedArrow(true)
            .setBgColor(ContextCompat.getColor(activity, com.volcengine.effectone.auto.common.R.color.color_232530))
            .setCornerRadius(SizeUtil.dp2px(8F).toFloat())
            .setShowElevationShadow(true)
            .setAutoDismissDelayMillis(Long.MIN_VALUE)
            .setView(mPopContentView)
            .setGravity(Gravity.TOP)
            .build()}

    override fun initView(rootView: ViewGroup) {
        rootView.findViewById<TextView>(R.id.auto_material_play_bottom_highLight_info_play)?.setNoDoubleClickListener {
                autoMaterialHighLightPlayVM.highLightPlayAction.value = Unit
        }
        rootView.findViewById<View>(R.id.auto_material_play_bottom_highLight_info_play_view)?.setNoDoubleClickListener {
            autoMaterialHighLightPlayVM.highLightPlayAction.value = Unit
        }
        rootView.findViewById<TextView>(R.id.auto_material_play_bottom_highLight_info_hl)?.apply {
            text = SpannableStringBuilder().apply {
                //高光时长：23000ms-27000ms
                val hlTime = String.format(
                    Locale.ENGLISH,
                    activity.getString(R.string.auto_material_recognize_high_light_duration_info),
                    autoMaterialHighLightPlayVM.highLightStartTime,
                    autoMaterialHighLightPlayVM.highLightEndTime
                )
                SpannableString(hlTime).apply {
                    setSpan(ForegroundColorSpan(ContextCompat.getColor(activity,com.volcengine.effectone.auto.common.R.color.color_80EEEEEE)),0,length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
                    append(this)

                }
                //"关键词：天空汽车风景人物画…"
                val highLightCategoryList = autoMaterialHighLightPlayVM.highLightCategoryList
                val category = if (highLightCategoryList.isEmpty()) {
                    ""
                } else {
                    val highLightCategoryBuilder = StringBuilder()
                    highLightCategoryList.forEach { highLightCategoryBuilder.append(it) }
                    String.format(
                        Locale.ENGLISH,
                        activity.getString(R.string.auto_material_recognize_high_light_keywords),
                        highLightCategoryBuilder
                    )
                }

                SpannableString(category).apply {
                    setSpan(ForegroundColorSpan(ContextCompat.getColor(activity,com.volcengine.effectone.auto.common.R.color.color_80EEEEEE)),0,length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
                    append(this)
                }
            }
            rootView.findViewById<View>(R.id.auto_material_play_bottom_highLight_info_pop)?.apply {
                visibleOrGone = autoMaterialHighLightPlayVM.highLightCategoryList.isEmpty().not()
                setNoDoubleClickListener {
                    mDuxPopover.show(it, Gravity.TOP, true, 0f, 0, -SizeUtil.dp2px(8f))
                }
            }
        }
    }

}
