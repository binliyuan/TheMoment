package com.volcengine.effectone.auto.templates.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.bytedance.ies.nle.editor_jni.NLEModel
import com.cutsame.solution.CutSameSolution
import com.cutsame.solution.source.CutSameSource
import com.cutsame.solution.source.PrepareSourceListener
import com.cutsame.solution.source.SourceInfo
import com.cutsame.solution.template.model.TemplateItem
import com.ss.android.ugc.cut_log.LogUtil
import com.ss.android.ugc.cut_ui.MediaItem
import com.ss.android.ugc.cut_ui.TextItem
import com.volcengine.effectone.auto.templates.bean.BaseResultData
import com.volcengine.effectone.auto.templates.bean.PrepareResult
import com.volcengine.effectone.auto.templates.bean.PrepareResultData
import com.volcengine.effectone.auto.templates.bean.TemplateByMedias
import com.volcengine.effectone.auto.templates.cutsame.CutSameContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * @author tyx
 * @description: 剪同款相关
 * @date :2024/4/25 16:45
 */
open class CutSameViewModel : CutSameComposeViewModel() {

	companion object {
		private const val TAG = "CutSameViewModel"
		fun get(owner: ViewModelStoreOwner): CutSameViewModel {
			return ViewModelProvider(owner).get(CutSameViewModel::class.java)
		}
	}

	val mScope by lazy { CoroutineScope(Dispatchers.Main + SupervisorJob()) }
	var mCutSameSource: CutSameSource? = null
	val mTemplatesByMedias = mutableListOf<TemplateByMedias>()
	var mFirstSelectIndex = 0
	val mMutableMediaItems by lazy { arrayListOf<MediaItem>() }               //可变槽位素材
	val mFinalMediaItems by lazy { arrayListOf<MediaItem>() }                 //最终的
	var mTextItemList: ArrayList<TextItem>? = null
	val mPrepareLiveData = MutableLiveData<PrepareResult>()                   //prepare状态
	var mCurrentTemplateItem: TemplateItem? = null                            //当前选择的模板
	val mUpdateBottomVideoList = MutableLiveData<List<MediaItem>>()           //底部列表刷新
	val mPlayerDataReady = MutableLiveData<ArrayList<MediaItem>>()            //资源Ok

	fun inject(templatesList: List<TemplateByMedias>, firstSelectIndex: Int) {
		mTemplatesByMedias.clear()
		mTemplatesByMedias.addAll(templatesList)
		mFirstSelectIndex = firstSelectIndex
	}

	fun getMediasByTemplate(templateItem: TemplateItem): TemplateByMedias? {
		return mTemplatesByMedias.find {
			it.templateItem.zipPath == templateItem.zipPath
		}
	}

	/**
	 * 初始化模板
	 */
	fun prepareSource(templateItem: TemplateItem) {
		mCurrentTemplateItem?.let {
			CutSameContext.removeCutSameSource(it.zipPath)
			CutSameContext.removeCutSamePlayer(it.zipPath)
		}
		mCutSameSource?.release()
		val hasSwitch = mCurrentTemplateItem != null && mCurrentTemplateItem?.zipPath != templateItem.zipPath
		mCurrentTemplateItem = templateItem
		val sourceInfo = SourceInfo(templateItem.templateUrl, templateItem.zipPath, templateItem.templateType, templateItem.zipPath)
		mCutSameSource = CutSameSolution.createCutSameSource(sourceInfo)
		CutSameContext.addCutSameSource(templateItem.zipPath, mCutSameSource)
		val prepareResult = PrepareResult().reset().setHasSwitch(hasSwitch).setState(BaseResultData.START)
		mPrepareLiveData.value = prepareResult
		mCutSameSource?.prepareSource(object : PrepareSourceListener {
			override fun onError(code: Int, message: String?) {
				LogUtil.d(TAG, "prepare source error,code:$code,msg:$message")
				mPrepareLiveData.postValue(prepareResult.setState(BaseResultData.END).setResultData(Result.failure(Throwable(message))))
			}

			override fun onProgress(progress: Float) {
				LogUtil.d(TAG, "prepare source progress:$progress")
				mPrepareLiveData.postValue(prepareResult.setState(BaseResultData.PROGRESS).setProgress(progress * 100f))
			}

			override fun onSuccess(mediaItemList: ArrayList<MediaItem>?, textItemList: ArrayList<TextItem>?, model: NLEModel) {
				if (mediaItemList.isNullOrEmpty()) {
					LogUtil.d(TAG, "prepare success,but mediaItems list is empty")
					mPrepareLiveData.postValue(prepareResult.setState(BaseResultData.END).setResultData(Result.failure(Throwable("No slot to be replaced"))))
					return
				}
				mTextItemList = textItemList
				mMutableMediaItems.clear()
				mediaItemList.filter { it.isMutable }.onEach { mMutableMediaItems.add(it) }
				LogUtil.d(TAG, "prepare success mutableMediaItems size : ${mMutableMediaItems.size},textItem size:${textItemList?.size}")
				val prepareResultData = PrepareResultData(templateItem, mMutableMediaItems, textItemList ?: emptyList())
				prepareResult.setState(BaseResultData.END).setResultData(Result.success(prepareResultData))
				mPrepareLiveData.postValue(prepareResult)
			}
		})
	}


	/**
	 * 只有在切换模板 或者 时光故事跳转时才会使用
	 * 从相册选择的链路不需要
	 */
	fun composeSource(mediaItems: ArrayList<MediaItem>) {
		mCutSameSource?.let { composeSource(it, mediaItems) }
	}

	fun setFinalMediaItems(list: List<MediaItem>) {
		if (mFinalMediaItems != list) {
			mFinalMediaItems.clear()
			mFinalMediaItems.addAll(list)
		}
	}

	fun release() {
		LogUtil.d(TAG, "release")
		mScope.cancel()
		mCurrentTemplateItem?.let {
			CutSameContext.removeCutSameSource(it.zipPath)
			CutSameContext.removeCutSamePlayer(it.zipPath)
		}
		mCutSameSource?.release()
		mCutSameSource?.cancelCompose()
		mCutSameSource = null
	}
}