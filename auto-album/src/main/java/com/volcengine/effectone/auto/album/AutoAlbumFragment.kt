package com.volcengine.effectone.auto.album

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.bytedance.creativex.mediaimport.preview.api.IPreviewView
import com.bytedance.creativex.mediaimport.preview.internal.IPreviewViewModel
import com.bytedance.creativex.mediaimport.preview.internal.main.MaterialAndPreviewCombiner
import com.bytedance.creativex.mediaimport.preview.internal.main.PreviewView
import com.bytedance.creativex.mediaimport.preview.internal.viewmodel.PreviewViewModel
import com.bytedance.creativex.mediaimport.repository.api.BuiltInMaterialType
import com.bytedance.creativex.mediaimport.repository.api.FolderKey
import com.bytedance.creativex.mediaimport.repository.api.IMaterialItem
import com.bytedance.creativex.mediaimport.repository.api.IMaterialRepository
import com.bytedance.creativex.mediaimport.repository.api.IMediaItem
import com.bytedance.creativex.mediaimport.repository.api.MaterialCategory
import com.bytedance.creativex.mediaimport.repository.api.MaterialSourceType
import com.bytedance.creativex.mediaimport.repository.api.defaultAllMaterialCategory
import com.bytedance.creativex.mediaimport.repository.api.defaultLocalImageCategory
import com.bytedance.creativex.mediaimport.repository.api.defaultLocalVideoCategory
import com.bytedance.creativex.mediaimport.repository.api.realFolderId
import com.bytedance.creativex.mediaimport.repository.api.realFolderName
import com.bytedance.creativex.mediaimport.repository.internal.cursor.DEFAULT_IMAGE_QUERY_PARAM
import com.bytedance.creativex.mediaimport.repository.internal.cursor.DEFAULT_IMAGE_QUERY_PARAM_NON_GIF
import com.bytedance.creativex.mediaimport.repository.internal.cursor.DEFAULT_VIDEO_QUERY_PARAM
import com.bytedance.creativex.mediaimport.repository.internal.main.DefaultMaterialCategoryFetcher
import com.bytedance.creativex.mediaimport.view.api.IMaterialSelectView
import com.bytedance.creativex.mediaimport.view.internal.IMaterialSelectorViewModel
import com.bytedance.creativex.mediaimport.view.internal.MultiSelectStrategy
import com.bytedance.creativex.mediaimport.view.internal.SingleSelectBehavior
import com.bytedance.creativex.mediaimport.view.internal.main.MaterialSelectView
import com.bytedance.creativex.mediaimport.view.internal.viewmodel.MediaSelectViewModel
import com.ss.android.ugc.tools.view.activity.SimpleActivityRegistryConverter
import com.volcengine.ck.album.R.color
import com.volcengine.ck.album.base.ALBUM_CONFIG_KEY
import com.volcengine.ck.album.base.AlbumConfig
import com.volcengine.ck.album.base.AlbumCoroutineScope
import com.volcengine.ck.album.utils.getSupportCategory
import com.volcengine.ck.album.validator.DurationCheckValidator
import com.volcengine.ck.album.validator.MediaCountCheckValidator
import com.volcengine.ck.album.validator.VECheckValidator
import com.volcengine.ck.album.viewmodel.EOMaterialRepositoryFactory
import com.volcengine.ck.album.viewmodel.RepeatMediaSelectViewModel
import com.volcengine.ck.logkit.LogKit
import com.volcengine.ck.smart.ExtraSourceType
import com.volcengine.ck.smart.SmartAlbumFetcher
import com.volcengine.ck.smart.SmartCategoryStrategy
import com.volcengine.ck.smart.SmartFolderStrategy
import com.volcengine.effectone.auto.album.preview.AutoAlbumPreviewView
import com.volcengine.effectone.auto.album.view.AutoAlbumMaterialSelectView
import com.volcengine.effectone.singleton.AppSingleton
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.launch

/**
 *Author: gaojin
 *Time: 2023/1/28 11:34
 */

open class AutoAlbumFragment : Fragment() {

    companion object {
        private const val ITEM_COLUMN_SPACING = 15F
        private const val LIST_SPAN_COUNT = 5
    }

    private val albumCoroutineScope: AlbumCoroutineScope by lazy {
        AlbumCoroutineScope()
    }

    private val disposable = CompositeDisposable()
    protected lateinit var albumConfig: AlbumConfig
    protected lateinit var mediaSelectViewModel: MediaSelectViewModel

    private val previewViewModel by lazy { PreviewViewModel.create(mediaSelectViewModel) }
    protected lateinit var previewView: PreviewView
    protected lateinit var mediaView: MaterialSelectView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        albumConfig = arguments?.getParcelable(ALBUM_CONFIG_KEY) ?: AlbumConfig()

        //MVVM -> Model
        val mediaRepository: IMaterialRepository = EOMaterialRepositoryFactory(this).apply {
            setCategoryFetcherProvider {
                val categories = mutableListOf<MaterialCategory>()
                if (albumConfig.albumExtraConfig?.readFromTargetPath == true) {
                    categories.add(defaultLocalVideoCategory(getString(com.volcengine.ck.album.R.string.eo_album_photos_videos)))
                } else {
                    if (albumConfig.allEnable) {
                        categories.add(defaultAllMaterialCategory(getString(com.volcengine.ck.album.R.string.eo_album_all)))
                    }
                    if (albumConfig.videoEnable) {
                        categories.add(defaultLocalVideoCategory(getString(com.volcengine.ck.album.R.string.eo_album_photos_videos)))
                    }
                    if (albumConfig.imageEnable) {
                        categories.add(defaultLocalImageCategory(getString(com.volcengine.ck.album.R.string.eo_album_photos)))
                    }
                }
                DefaultMaterialCategoryFetcher(categories = categories)
            }
            setImageQueryParamProvider {
                if (albumConfig.showGif) {
                    DEFAULT_IMAGE_QUERY_PARAM
                } else {
                    DEFAULT_IMAGE_QUERY_PARAM_NON_GIF
                }
            }
            setVideoQueryParamProvider {
                DEFAULT_VIDEO_QUERY_PARAM
            }

            val types = mutableListOf<MaterialSourceType>()
            if (albumConfig.allEnable) {
                types.add(BuiltInMaterialType.ALL)
            }
            if (albumConfig.imageEnable) {
                types.add(BuiltInMaterialType.IMAGE)
            }
            if (albumConfig.videoEnable) {
                types.add(BuiltInMaterialType.VIDEO)
            }
            setSourceTypesInFolder(types)

            albumConfig.albumExtraConfig?.let {
                if (it.readFromTargetPath) {
                    setCategoryStrategy(SmartCategoryStrategy())
                    setFolderStrategy(SmartFolderStrategy(requireActivity()))
                    registerFetcher(ExtraSourceType.SMART) {
                        SmartAlbumFetcher(it.targetPath)
                    }
                }
            }
        }.create()


        //MVVM -> ViewModel
        mediaSelectViewModel = MediaSelectViewModel(
            lifecycleOwner = this,
            mediaRepository = mediaRepository,
            folderKeyProvider = {
                (it as? IMediaItem)?.let { mediaItem ->
                    FolderKey(mediaItem.realFolderName, mediaItem.realFolderId)
                } ?: FolderKey(it.path, it.path)
            },
            categoryType = albumConfig.getSupportCategory(),
            selectorViewModel = RepeatMediaSelectViewModel(this)
        ).also {
            it.requestData()
            it.mediaSelectorViewModel?.let { mediaSelectorViewModel ->
                mediaSelectorViewModel.updateSelectorConfigure { config ->
                    config.multiSelectStrategy = if (albumConfig.singleSelect) {
                        MultiSelectStrategy.NON
                    } else {
                        MultiSelectStrategy.ALL
                    }
                    config.singleSelectBehavior = if (albumConfig.confirmDirectly) {
                        SingleSelectBehavior.ConfirmDirectly
                    } else {
                        SingleSelectBehavior.SelectAndCancelable
                    }
                }
                addValidators(mediaSelectorViewModel)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.auto_fragment_album_root, container, false)
        initAlbumView(rootView)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val combiner = MaterialAndPreviewCombiner(
            materialSelectViewProvider = { mediaView },
            viewModelProvider = { mediaSelectViewModel },
            previewViewProvider = { previewView },
            listenableActivityRegistry = SimpleActivityRegistryConverter().convert(requireActivity()),
            {
                it.handleKeyEventOnlyInPreview = true
            }
        )

        mediaSelectViewModel.mediaCategoryViewModel?.loadMediaCategory()

        combiner.showMaterialSelectView()
    }

    private fun initAlbumView(rootView: View) {
        //MVVM -> View
        mediaView = getMediaView(rootView)
        previewView = getAlbumPreview(rootView, this, previewViewModel, albumConfig)
        initListener(mediaView, previewView)
    }

    private fun confirmSelected(mediaList: List<IMaterialItem>) {
        mediaList.firstOrNull() ?: return
        albumCoroutineScope.launch {
            albumConfig.finishClazz.newInstance().finishAction(requireActivity(), mediaList, albumConfig)
        }
    }

    private fun addValidators(viewModel: IMaterialSelectorViewModel<IMaterialItem>) {
        val mediaCountCheckValidator = MediaCountCheckValidator(
            albumConfig.maxSelectCount, albumConfig.minSelectCount
        )
        //数量检查
        viewModel.addPreSelectValidator(mediaCountCheckValidator)
        //时长检查
        viewModel.addPreSelectValidator(DurationCheckValidator(albumConfig.minDuration, albumConfig.maxDuration))
        val sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(AppSingleton.instance)
        val enableVEChecker = sp.getBoolean("album_import_ve_checker", true)
        if (enableVEChecker) {
            viewModel.addPreSelectValidator(VECheckValidator())
        }
        viewModel.addPostSelectValidator(mediaCountCheckValidator)
    }

    private fun initListener(mediaView: MaterialSelectView, previewView: PreviewView) {
        if (albumConfig.singleSelect && albumConfig.confirmDirectly) {
            mediaSelectViewModel.mediaSelectorViewModel?.observeSelectionConfirmed()?.observeOn(AndroidSchedulers.mainThread())?.subscribe { list ->
                confirmSelected(list)
            }?.addTo(disposable)
        } else {
            mediaView.observeViewEvent().observeOn(AndroidSchedulers.mainThread())
                .subscribe { event ->
                    when (event) {
                        is IMaterialSelectView.ViewEvent.Confirm -> {
                            confirmSelected(mediaSelectViewModel.mediaSelectorViewModel?.selectedMaterials?.value ?: emptyList())
                        }

                        is IMaterialSelectView.ViewEvent.ListItemClick -> {
                            LogKit.i("ListItemClick", event.data.toString())
                        }

                        else -> {
                            //do nothing
                        }
                    }
                }.addTo(disposable)
            previewView.observeViewEvent().observeOn(AndroidSchedulers.mainThread())
                .subscribe { event ->
                    when (event) {
                        is IPreviewView.ViewEvent.Confirm -> {
                            confirmSelected(mediaSelectViewModel.mediaSelectorViewModel?.selectedMaterials?.value ?: emptyList())
                        }

                        else -> {
                            //do nothing
                        }
                    }
                }.addTo(disposable)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
        albumCoroutineScope.close()
    }

    open fun getPreviewContainer(rootView: View): ViewGroup {
        return rootView.findViewById(R.id.auto_album_preview_root)
    }

    open fun getAlbumPreview(
        rootView: View,
        lifecycleOwner: LifecycleOwner,
        previewViewModel: IPreviewViewModel<IMaterialItem>,
        albumConfig: AlbumConfig
    ): PreviewView {
        return AutoAlbumPreviewView(
            root = getPreviewContainer(rootView),
            lifecycle = lifecycleOwner,
            viewModel = previewViewModel,
            albumConfig = albumConfig,
            viewConfigureBuilder = { config ->
                config.enableTransition = false
                config.enableDocker = true
                config.selectorViewConfigure.viewConfigureBuilder = {
                    it.preCheckSelectorValid = true
                }
                config.pagerViewConfigure.imagePageViewConfigureBuilder = {
                    it.needResize = true
                    it.enableScale = false
                }
                config.pagerViewConfigure.videoPageViewConfigureBuilder = {
                    it.enableScale = false
                }
            }
        )
    }

    open fun getMediaView(rootView: View): MaterialSelectView {
        return AutoAlbumMaterialSelectView(
            root = rootView.findViewById<FrameLayout>(R.id.auto_album_root_content_layout),
            albumConfig = albumConfig,
            lifecycleOwner = this,
            viewModel = mediaSelectViewModel,
            viewConfigureBuilder = { config ->
                config.lazyLoadData = true
                config.enableDragClose = false
                config.enableTransition = false
                config.enableTopSelector = false
                config.enableLazyPager = false
                config.enableDocker = albumConfig.enableDocker
                config.hideDelegate = {
                    requireActivity().finish()
                }
                config.selectorViewConfigure.run {
                    listViewConfigureBuilder = {
                        it.enableMoveItem = false
                    }
                    enableIndexedSelect = false
                    enableDuplicatedSelect = true
                }
                config.contentListConfigureBuilders = {
                    { listConfig ->
                        listConfig.listSpanCount = LIST_SPAN_COUNT
                        listConfig.itemColumnSpacing = 30F
                        listConfig.itemDecorationSpacing = 15F
                        listConfig.itemDecorationIncludeEdge = false
                        listConfig.itemPadding = 22F
                        listConfig.itemViewRatio = 178.0 / 316.0
                    }
                }
                config.pagerViewConfigure.run {
                    tabIndicatorColor = R.color.auto_album_indicator_color
                    tabSelectedTextColor = R.color.ConstTextInverse1
                    tabUnSelectedTextColor = R.color.TextTertiary
                    pagerViewConfigureBuilder = {
                        it.tabSelectedColor =
                            ContextCompat.getColor(this@AutoAlbumFragment.requireContext(), color.ConstTextInverse1)
                        it.tabUnSelectedColor = ContextCompat.getColor(
                            this@AutoAlbumFragment.requireContext(),
                            color.TextTertiary
                        )
                        it.tabTextOnlyBoldOnSelection = true
                    }
                }
            }
        )
    }
}