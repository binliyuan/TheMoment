package com.volcengine.effectone.auto.recorder.helper

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.recorder.R
import com.volcengine.effectone.auto.recorder.viewmodel.AutoRecordUIViewModel
import com.volcengine.effectone.auto.recorder.widget.EOVerticalIndicatorSeekBar
import com.volcengine.effectone.recorderui.beauty.BeautyViewModel
import java.util.Locale

class AutoRecordDrawerSeekbarHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
) : IUIHelper {
        companion object{
            private const val TAG = "AutoDrawerPanelHelper"
        }
    private val recordUIViewModel by lazy { AutoRecordUIViewModel.get(activity) }
    private val beautyViewModel by lazy { BeautyViewModel.get(activity) }
    private lateinit var beautySeekBar: EOVerticalIndicatorSeekBar
    private lateinit var beautySeekBarIndicator: TextView
    private lateinit var beautySeekBarContainer: LinearLayout
    private lateinit var beautySeekBarName: TextView
    override fun initView(rootView: ViewGroup) {
        beautySeekBar = rootView.findViewById<EOVerticalIndicatorSeekBar?>(R.id.auto_recorder_seekbar).apply {
            updateProgressShader()
            updateBackgroundPaintColor(R.color.SecondaryNormal)
        }
        beautySeekBarIndicator = rootView.findViewById<TextView?>(R.id.auto_recorder_progress).apply {
            text = "${String.format(Locale.ENGLISH,"%d",beautySeekBar.progress)}%"
        }
        beautySeekBar.setOnSeekBarChangeListener(object: EOVerticalIndicatorSeekBar.OnSeekBarChangeListenerAdapter(){
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                beautySeekBarIndicator.text = "${String.format(Locale.ENGLISH,"%d",progress)}%"
                val currentComposerItem = beautyViewModel.currentVisibleComposerItem.value ?: return
                if (currentComposerItem.hasChild()) {
                    return
                }
                currentComposerItem.composeNode.value = beautySeekBar.progress2Intensity(progress)
                beautyViewModel.updateComposerIntensityEvent.value = currentComposerItem
            }
        } )
        beautySeekBarContainer = rootView.findViewById(R.id.auto_recorder_beauty_container)
        beautySeekBarName = rootView.findViewById(R.id.auto_recorder_beauty_name)
        initObserver()
    }

    private fun initObserver() {
        recordUIViewModel.toggleDrawerState.observe(owner){
            Log.d(TAG, "initObserver() toggleDrawerState with: item = ${beautyViewModel.currentVisibleComposerItem.value}")

        }
        beautyViewModel.currentVisibleComposerItem.observe(owner) {
            it?.let { item ->
                Log.d(TAG, "initObserver() currentVisibleComposerItem with: item = $item")
                val range = item.composeNode.range
                if (range.size == 2 && !item.hasChild()) {
                    beautySeekBarContainer.visibility = View.VISIBLE
                    beautySeekBarName.text = item.title
                    beautySeekBar.setRange(range.first(), range.last())
                    beautySeekBar.setIntensity(item.composeNode.value)
                } else {
                    beautySeekBarContainer.visibility = View.GONE
                }
            } ?: kotlin.run {
                beautySeekBarContainer.visibility = View.GONE
            }
        }

    }

    private  fun EOVerticalIndicatorSeekBar.updateProgressShader() {
        val linearGradient = LinearGradient(
            0f,
            0f,
            width.toFloat(),
            0f,
            Color.parseColor("#FFFF284B"),
            Color.parseColor("#FFFD2DDB"),
            Shader.TileMode.CLAMP
        )
        Log.d(TAG, "updateProgressShader() width  = $width")
        updateProgressPaintShader(linearGradient)
    }
}