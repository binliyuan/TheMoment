package com.volcengine.effectone.auto.business

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.volcengine.auth.api.EOAuthErrCode
import com.volcengine.auth.api.EOAuthorization
import com.volcengine.auth.api.EOAuthorizationInternal
import com.volcengine.auth.core.EOAuthConfig
import com.volcengine.auth.core.EOAuthResData
import com.volcengine.ck.album.base.AlbumConfig
import com.volcengine.ck.logkit.DebugLogger
import com.volcengine.ck.logkit.LogKit
import com.volcengine.effectone.EffectOneSdk
import com.volcengine.effectone.api.EffectOneConfigList
import com.volcengine.effectone.auto.business.hl.ILASDKInitHelper
import com.volcengine.effectone.auto.recorder.AutoRecordActivity
import com.volcengine.effectone.auto.templates.cutsame.CutSameInit
import com.volcengine.effectone.auto.templates.cutsame.OfflineTemplateResourceProvider
import com.volcengine.effectone.auto.templates.cutsame.OnlineTemplateResourceProvider
import com.volcengine.effectone.auto.templates.cutsame.TemplateConfig
import com.volcengine.effectone.base.PhotoEditingMode
import com.volcengine.effectone.data.IEOResourceItem
import com.volcengine.effectone.image.ImageLoader
import com.volcengine.effectone.image.ImageOption
import com.volcengine.effectone.recorderui.base.RecorderInitConfig
import com.volcengine.effectone.recorderui.beauty.BeautyUIConfig
import com.volcengine.effectone.recorderui.util.RecordUtils
import com.volcengine.effectone.recorderui.vesdk.VEInit
import com.volcengine.effectone.resource.api.EOResourceConfig
import com.volcengine.effectone.resource.api.EOResourceManager
import com.volcengine.effectone.resource.api.EOResourcePanelKey
import com.volcengine.effectone.resource.impl.DefaultLocalResourceLoader
import com.volcengine.effectone.resource.impl.LocalConfigsLoader
import com.volcengine.effectone.singleton.AppSingleton
import com.volcengine.effectone.sticker.data.EOBaseStickerUIConfig
import com.volcengine.effectone.utils.EOUtils
import com.volcengine.effectone.widget.EOToaster
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

interface IQuickInit {
	var licenseFileName: String
	fun initApplication(application: Application)
	fun prepareAndInit(callback: (Boolean, String) -> Unit)
	fun startRecorder(activity: FragmentActivity, type: Int)
}

object AutoQuickInitHelper : IQuickInit {

	override var licenseFileName: String = "labcv_test_20240606_20250707_com.volcengine.effectone.auto_1.0.0_201.licbag"

	private val coroutineScope by lazy { CoroutineScope(Dispatchers.Main) }

	override fun initApplication(application: Application) {
		//绑定AppContext
		AppSingleton.bindInstance(application)

		// 指定素材文件保存目录、素材使用语言
		val config = EOResourceConfig.Builder()
			.setResourceSavePath(EOUtils.pathUtil.internalResource())
			.setSysLanguage(Locale.getDefault().language)
			//.setDomainConfig(ParaCloudResource.config) // 使用在线素材时需要该配置
			//.setNetWorker(DefaultNetWorker()) // 使用在线素材时需要该配置
			.build()

		// 素材sdk初始化
		EOResourceManager.init(config)
		//初始化EffectOneSdk
		EffectOneSdk.run {
			imageLoader = DefaultImageLoader()
			logger = DebugLogger()
			modelPath = EOResourceManager.getModelRootPath()
			photoEditingMode = PhotoEditingMode.MODE_PICTURE
		}
	}

	override fun prepareAndInit(callback: (Boolean, String) -> Unit) {
		coroutineScope.launch(Dispatchers.Main) {
			System.loadLibrary("effect")
			initConfigs()
			initResourceData()
			val authResult = withContext(Dispatchers.IO) {
				auth()
			}
			if (authResult.resCode != EOAuthErrCode.EO_SUCCESS) {
				EOToaster.show(AppSingleton.instance, authResult.resMsg)
				callback(false, "auth failed:${authResult.resMsg}")
			} else {
				VEInit.init()
				EOAuthorizationInternal.getCutSameLicensePath()?.let { cutSameLicensePath ->
					CutSameInit.initCutSame(
						AppSingleton.instance,
						cutSameLicensePath,
						EOResourceManager.getModelRootPath()
					)
				}
				val success = ILASDKInitHelper.init()
				if (success) {
					callback(true, "auth success")
				} else {
					callback(false, "ILASDK init failed")
				}
			}
		}
	}

	private fun initConfigs() {
		//拍摄模块整体配置
		EffectOneConfigList.configure(RecorderInitConfig()) {
			it.albumConfig = AlbumConfig(
				allEnable = true,
				imageEnable = true,
				videoEnable = true,
				maxSelectCount = EffectOneSdk.albumMaxSelectedCount,
				//maxDuration Unit:ms
				maxDuration = 60 * 60 * 1000L,
			)
		}
		//人脸特效面板
		EffectOneConfigList.configure(EOBaseStickerUIConfig()) {
			it.resourceLoader = DefaultLocalResourceLoader.instance
		}

		//拍摄专用面板配置
		//美颜面板
		EffectOneConfigList.configure(BeautyUIConfig()) {
			it.resourceLoader = DefaultLocalResourceLoader.instance
			it.defaultBeautyAction = {
				EOResourceManager.syncLoadConfigByPanelKey(EOResourcePanelKey.RECORDER_BEAUTY.value, true).tabs
			}
		}

		//模板
		EffectOneConfigList.configure(TemplateConfig()) {
			it.templateResourceProvider = OnlineTemplateResourceProvider()
		}
	}

	private fun initResourceData() {
		val configsPathMap: Map<String, String> = mutableMapOf<String, String>().apply {
			put(EOResourcePanelKey.RECORDER_BEAUTY.value, "Panel_configs/recorder_beauty.json")
			put(EOResourcePanelKey.RECORDER_STICKER.value, "Panel_configs/recorder_sticker.json")
		}

		DefaultLocalResourceLoader.instance.localConfigsLoader = object : LocalConfigsLoader {
			override fun loadConfigs(panelKey: String, type: String): String {
				val value = configsPathMap.getValue(panelKey)
				return EOUtils.fileUtil.readJsonFromAssets(AppSingleton.instance, "${EOUtils.pathUtil.assetsResourcePath}/${value}")
			}
		}

		// 加载默认美颜资源
		CoroutineScope(Dispatchers.IO).launch {
			EOResourceManager.loadDefaultBeautyResources()
		}
	}

	private fun auth(): EOAuthResData {
		val authConfig = EOAuthConfig()
		// 根据文件名，拷贝离线鉴权文件到App私有目录下
		val licenseFile = File(EOUtils.pathUtil.internalLicense(), licenseFileName)
		if (!licenseFile.exists()) {
			EOUtils.fileUtil.copyAssetFile("license/${licenseFileName}", licenseFile.absolutePath)
		}
		authConfig.offlineLicensePath = licenseFile.absolutePath
		return EOAuthorization.makeAuthWithConfig(authConfig)
	}

	//启动拍摄页
	override fun startRecorder(activity: FragmentActivity, type: Int) {
		val recordPermissions = mutableListOf<String>().apply {
			add(Manifest.permission.CAMERA)
			add(Manifest.permission.RECORD_AUDIO)
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				add(Manifest.permission.READ_MEDIA_IMAGES)
				add(Manifest.permission.READ_MEDIA_VIDEO)
				add(Manifest.permission.READ_MEDIA_AUDIO)
			} else {
				add(Manifest.permission.READ_EXTERNAL_STORAGE)
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        EOUtils.permission.checkPermissions(activity, recordPermissions, {
            AutoRecordActivity.startRecord(activity, type)
        }, {
            RecordUtils.showRecordPermissionTips(activity, it)
        })
    }
}

class DefaultImageLoader : ImageLoader {
	companion object {
		private const val TAG = "ImageLoader"
	}

	override fun loadImageView(imageView: ImageView, path: String?, option: ImageOption?) {
		if (path.isNullOrBlank()) {
			return
		}
		var requestBuilder = Glide.with(AppSingleton.instance).load(path)
		option?.let {
			requestBuilder = requestBuilder.apply(it.toGlideRequestOption())
		}
		bindListener(requestBuilder, option)
		requestBuilder.into(imageView)
	}

	override fun loadImageView(imageView: ImageView, uri: Uri, option: ImageOption?) {
		var requestBuilder = Glide.with(AppSingleton.instance).load(uri)
		option?.let {
			requestBuilder = requestBuilder.apply(it.toGlideRequestOption())
		}
		bindListener(requestBuilder, option)
		requestBuilder.into(imageView)
	}

	override fun loadImageView(imageView: ImageView, eoResourceItem: IEOResourceItem, option: ImageOption?) {
		if (eoResourceItem.icon.isNotEmpty()) {
			loadImageView(imageView, eoResourceItem.icon, option)
		} else {
			setLocalImage(imageView, eoResourceItem.builtInIcon, option)
		}
	}

	override fun loadImageView(imageView: ImageView, resId: Int, option: ImageOption?) {
		var requestBuilder = Glide.with(AppSingleton.instance).load(resId)
		option?.let {
			requestBuilder = requestBuilder.apply(it.toGlideRequestOption())
		}
		bindListener(requestBuilder, option)
		requestBuilder.into(imageView)
	}

	override fun loadBitmapSync(context: Context, cacheFilePath: String, option: ImageOption): Bitmap? {
		return Glide.with(context).asBitmap().apply(option.toGlideRequestOption())
			.load(cacheFilePath)
			.submit(option.width, option.height)
			.get()
	}

	override fun resizeBitmapSync(context: Context, bitmap: Bitmap, width: Int, height: Int): Bitmap {
		return Glide.with(context).asBitmap().apply(RequestOptions().centerCrop())
			.load(bitmap)
			.submit(width, height)
			.get()
	}

	@SuppressLint("DiscouragedApi")
	private fun setLocalImage(imageView: ImageView, builtInIcon: String, option: ImageOption?) {
		try {
			val iconName = getIconName(builtInIcon)
			if (iconName.isNotEmpty()) {
				val iconResId = AppSingleton.instance.resources.getIdentifier(iconName, "drawable", AppSingleton.instance.packageName)
				//Glide load使用iconResId唯一值可以缓存,防止getDrawable每次返回一个新的去加载导致ui刷新回闪烁\
				if (iconResId != 0) {
					var requestBuilder = Glide.with(AppSingleton.instance)
						.load(iconResId)
					option?.let {
						requestBuilder = requestBuilder.apply(it.toGlideRequestOption())
					}
					bindListener(requestBuilder, option)
					requestBuilder.into(imageView)
					return
				}
				LogKit.e(TAG, "$builtInIcon NotFoundException", null)
			}
		} catch (e: Resources.NotFoundException) {
			LogKit.e(TAG, "$builtInIcon NotFoundException", null)
		}
	}

	private fun getIconName(builtInIcon: String): String {
		val lastDotIndex = builtInIcon.lastIndexOf(".")
		return if (lastDotIndex != -1) {
			builtInIcon.substring(0, lastDotIndex)
		} else {
			""
		}
	}

	private fun bindListener(requestBuilder: RequestBuilder<Drawable>, option: ImageOption?) {
		option?.listener?.let {
			requestBuilder.listener(object : RequestListener<Drawable> {
				override fun onLoadFailed(
					e: GlideException?,
					model: Any?,
					target: Target<Drawable>?,
					isFirstResource: Boolean
				): Boolean {
					return it.onLoadFailed(e)
				}

				override fun onResourceReady(
					resource: Drawable,
					model: Any?,
					target: Target<Drawable>?,
					dataSource: DataSource?,
					isFirstResource: Boolean
				): Boolean {
					return it.onResourceReady(resource)
				}
			})
		}
	}
}

fun ImageOption.toGlideRequestOption(): RequestOptions {
	var requestOptions = RequestOptions()
	if (skipDiskCached) {
		requestOptions = requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE)
	}
	requestOptions = requestOptions.skipMemoryCache(false)
	if (this.width != 0 && this.height != 0) {
		requestOptions = requestOptions.override(this.width, this.height)
	}
	if (this.placeHolder != 0) {
		requestOptions = requestOptions.placeholder(this.placeHolder)
	}
	when (this.scaleType) {
		ImageView.ScaleType.CENTER_CROP -> {
			requestOptions = requestOptions.centerCrop()
		}

		ImageView.ScaleType.CENTER_INSIDE -> {
			requestOptions = requestOptions.centerInside()
		}

		ImageView.ScaleType.FIT_CENTER -> {
			requestOptions = requestOptions.fitCenter()
		}

		else -> {}
	}
	when (this.format) {
		Bitmap.Config.ARGB_8888 -> {
			requestOptions = requestOptions.format(DecodeFormat.PREFER_ARGB_8888)
		}

		Bitmap.Config.RGB_565 -> {
			requestOptions = requestOptions.format(DecodeFormat.PREFER_RGB_565)
		}

		else -> {
			//do nothing
		}
	}
	return requestOptions
}