package com.volcengine.effectone.auto.moment.hl.vm

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.bytedance.creativex.mediaimport.repository.api.IMaterialItem
import com.volcengine.ck.highlight.config.HLResultByMedia
import com.volcengine.ck.highlight.ila.ILASDKInit
import com.volcengine.ck.highlight.ila.api.IMediaRecognizeListener
import com.volcengine.ck.highlight.utils.isImage
import com.volcengine.ck.highlight.utils.isVideo
import com.volcengine.ck.logkit.LogKit
import com.volcengine.effectone.auto.moment.hl.AutoMaterialRecognizeTabType
import com.volcengine.effectone.auto.moment.hl.data.AutoMaterialRecognizeMedia
import com.volcengine.effectone.auto.moment.hl.detail.AutoMaterialHighLightExtractDiaLog
import com.volcengine.effectone.auto.moment.hl.detail.AutoMaterialRecognizeDetailDiaLog
import com.volcengine.effectone.auto.moment.hl.launcher.AutoMaterialRecognizeAlbumLauncher
import com.volcengine.effectone.auto.moment.utils.toRecognizeMedia
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class AutoMaterialRecognizeViewModel : ViewModel() {
    //原始数据（不记录hlResults值）
    private val originalMediaItems = mutableListOf<AutoMaterialRecognizeMedia>()
    //ui刷新
    val updateAdapterUI = MutableLiveData<Unit>()
    //记录ILASDK初始化结果
    val initializerILASDK = MutableLiveData<Boolean>()
    val updateItem = MutableLiveData<AutoMaterialRecognizeMedia>()

    private val mainScope by lazy { object :CoroutineScope{
        override val coroutineContext: CoroutineContext
            get() = Dispatchers.Main + SupervisorJob() + CoroutineName(TAG)

    } }

    private val mediaRecognizeListener by lazy { object : IMediaRecognizeListener {
        override fun onMediaRecognized(hlResultByMedia: HLResultByMedia) {
            LogKit.d(TAG, "onMediaRecognized() mediaRecognizeListener : hlResultByMedia = $hlResultByMedia")
            val processData = originalMediaItems
            if (processData.isEmpty()) {
                return
            }
            val markAutoMaterialRecognizeMedia =
                markAutoMaterialRecognizeMedia(hlResultByMedia, processData)
            if (markAutoMaterialRecognizeMedia.isEmpty()) {
                return
            }
           updateItem.value =  markAutoMaterialRecognizeMedia.first()
        }
    } }
    override fun onCleared() {
        super.onCleared()
        mainScope.cancel()
        ILASDKInit.unRegisterRecognizeListener()
    }

    fun parseArguments(bundle: Bundle): ArrayList<IMaterialItem>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelableArrayList(AutoMaterialRecognizeAlbumLauncher.ARGUMENT_KEY_MEDIA_LIST, IMaterialItem::class.java)
        } else {
            bundle.getParcelableArrayList<IMaterialItem>(AutoMaterialRecognizeAlbumLauncher.ARGUMENT_KEY_MEDIA_LIST)
        }?.also {
            originalMediaItems.run {
                clear()
                addAll(it.map {
                    AutoMaterialRecognizeMedia(it.toRecognizeMedia())
                })
            }
        }
    }
    fun startScan(){
        if (originalMediaItems.isEmpty()) {
            return
        }
       recognizeMedias(originalMediaItems)
    }

    fun concatRecognizeMedias(selectItem: List<AutoMaterialRecognizeMedia>) {
        val diffRecognizeMedias = ArrayList(selectItem).apply {
            removeAll(originalMediaItems.toSet())
        }
        originalMediaItems.addAll(0,diffRecognizeMedias)
        updateAdapterUI.value = Unit
        recognizeMedias(originalMediaItems)
    }

    fun goDetail(activity: FragmentActivity, result: AutoMaterialRecognizeMedia) {
        if (result.hlResults.isNullOrEmpty()) {
            return
        }
        AutoMaterialRecognizeDetailDiaLog.createAutoMaterialRecognizeDetailDiaLog(activity,result).show()
    }

    fun showHighLightFragment(activity: FragmentActivity, result: AutoMaterialRecognizeMedia) {
        if (result.hlResults.isNullOrEmpty()) {
            return
        }
        AutoMaterialHighLightExtractDiaLog.createAutoMaterialHighLightExtractDiaLog(activity,result).show()
    }

    private fun recognizeMedias(autoMaterialRecognizeMedias: MutableList<AutoMaterialRecognizeMedia>) {
        mainScope.launch{
            val processData = originalMediaItems
            if (processData.isEmpty()) {
                return@launch
            }
            withContext(Dispatchers.IO) {
                val currentRecognizeMedia = autoMaterialRecognizeMedias.map { it.recognizeMedia }
                val recognizeResult = ILASDKInit.recognizeMedias(currentRecognizeMedia)
                recognizeResult.flatMapTo(mutableListOf<AutoMaterialRecognizeMedia>()) { item ->
                    LogKit.d(TAG, "recognizeMedias() recognizeResult flatMapTo: item = ${item.media.path}")
                    markAutoMaterialRecognizeMedia(item, processData)
                }
            }
            updateAdapterUI.value = Unit
        }
    }

    private fun markAutoMaterialRecognizeMedia(
        item: HLResultByMedia,
        processData: MutableList<AutoMaterialRecognizeMedia>
    ): List<AutoMaterialRecognizeMedia> {
        val recognizeMedia = item.media
        val hlResults = item.hlResults
        return processData.filter { it.recognizeMedia.id == recognizeMedia.id }
            .map {
                it.apply {
                    it.takeIf { it.hlResults.isNullOrEmpty() }
                        ?.also { it.hlResults = hlResults }
                }
            }
    }

    fun getAutoMaterialMediaByType(
        tabType: AutoMaterialRecognizeTabType,
        allDates: List<AutoMaterialRecognizeMedia> = originalMediaItems.toMutableList()
    ): List<AutoMaterialRecognizeMedia> {
        return when (tabType) {
            AutoMaterialRecognizeTabType.MATERIAL_ALL -> allDates
            AutoMaterialRecognizeTabType.MATERIAL_IMG -> allDates.filter { it.recognizeMedia.isImage() }
            AutoMaterialRecognizeTabType.MATERIAL_VIDEO -> allDates.filter { it.recognizeMedia.isVideo() }
        }
    }

    fun getCurTabItems(curTabType: AutoMaterialRecognizeTabType): List<AutoMaterialRecognizeMedia> {
       return getAutoMaterialMediaByType(curTabType).map { it.copy()  }
    }


    companion object{
        private const val TAG = "AutoMaterialRecognizeViewModel"
        fun get(viewModelStoreOwner: ViewModelStoreOwner):AutoMaterialRecognizeViewModel{
            return ViewModelProvider(viewModelStoreOwner).get(AutoMaterialRecognizeViewModel::class.java)
        }
    }
}