package com.volcengine.effectone.auto.algorithm.helper

import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.volcengine.effectone.auto.algorithm.viewmodel.AlgorithmViewModel
import com.volcengine.effectone.auto.recorder.helper.AutoRecordButtonHelper
import com.volcengine.effectone.recordersdk.api.EOPictureCallback
import com.volcengine.effectone.recordersdk.base.FlashState
import com.volcengine.effectone.recordersdk.base.RecordAction
import com.volcengine.effectone.recorderui.api.EORecordTakePicListener

class AlgorithmRecordButtonHelper (
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
) : AutoRecordButtonHelper(activity, owner) {
    companion object {
        private const val TAG = "AlgorithmRecordButtonHelper"
    }
    override val cameraViewModel by lazy { AlgorithmViewModel.get(activity) }

    override fun initView(rootView: ViewGroup) {
        super.initView(rootView)

        recordButton.setOnTakePicListener(object : EORecordTakePicListener {
            override fun takePic() {
                cameraViewModel.recordAction.value = RecordAction.TAKE_PIC_START
                cameraViewModel.recordManager.takePicture(cameraViewModel.flashState.value == FlashState.ON, object :
                    EOPictureCallback {
                    override fun onPictureTaken(path: String) {
                        Toast.makeText(activity, "图片保存成功", Toast.LENGTH_SHORT).show()
                        cameraViewModel.recordAction.value = RecordAction.TAKE_PIC_FINISH
                    }

                    override fun onTakenFailed() {
                        Toast.makeText(activity, "拍照失败", Toast.LENGTH_SHORT).show()
                        cameraViewModel.recordAction.value = RecordAction.TAKE_PIC_FINISH
                    }
                })
            }
        })
    }
}