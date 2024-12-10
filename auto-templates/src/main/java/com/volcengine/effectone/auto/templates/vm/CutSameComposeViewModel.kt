package com.volcengine.effectone.auto.templates.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.cutsame.solution.source.ComposeSourceListener
import com.cutsame.solution.source.CutSameSource
import com.ss.android.ugc.cut_log.LogUtil
import com.ss.android.ugc.cut_ui.MediaItem
import com.volcengine.effectone.auto.templates.bean.BaseResultData
import com.volcengine.effectone.auto.templates.bean.ComposeResult

/**
 * @author tyx
 * @description:
 * @date :2024/5/15 14:57
 */
open class CutSameComposeViewModel : ViewModel() {

	companion object {
		private const val TAG = "CutSameComposeViewModel"
		fun create(owner: ViewModelStoreOwner): CutSameComposeViewModel {
			return ViewModelProvider(owner).get(CutSameComposeViewModel::class.java)
		}
	}

	val mComposeLiveData = MutableLiveData<ComposeResult>()

	fun composeSource(cutSameSource: CutSameSource?, pickerMediaItems: ArrayList<MediaItem>) {
		LogUtil.d(TAG, "picker media item size:${pickerMediaItems.size}")
		val composeResult = ComposeResult().reset().setState(BaseResultData.START)
		mComposeLiveData.postValue(composeResult)
		cutSameSource?.composeSource(pickerMediaItems, object : ComposeSourceListener {
			override fun onError(errorCode: Int, message: String?) {
				LogUtil.d(TAG, "compose error,code:$errorCode,msg:$message")
				composeResult.setResultData(Result.failure(Throwable(message))).setState(BaseResultData.END)
				mComposeLiveData.postValue(composeResult)
			}

			override fun onProgress(progress: Float) {
				LogUtil.d(TAG, "compose progress:$progress")
				composeResult.setState(BaseResultData.PROGRESS).setProgress(progress * 100f)
				mComposeLiveData.postValue(composeResult)
			}

			override fun onSuccess(mediaItems: ArrayList<MediaItem>?) {
				LogUtil.d(TAG, "compose success,media size:${mediaItems?.size}")
				val list = mediaItems ?: arrayListOf()
				composeResult.setState(BaseResultData.END).setResultData(Result.success(list))
				mComposeLiveData.postValue(composeResult)
			}
		})
	}
}