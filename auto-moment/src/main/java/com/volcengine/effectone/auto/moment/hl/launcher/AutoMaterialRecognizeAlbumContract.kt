package com.volcengine.effectone.auto.moment.hl.launcher

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.FragmentActivity
import bytedance.io.BdFileSystem
import com.bytedance.creativex.mediaimport.repository.api.IMaterialItem
import com.bytedance.creativex.mediaimport.util.isMediaExists
import com.volcengine.ck.album.api.IAlbumFinish
import com.volcengine.ck.album.base.ALBUM_CONFIG_KEY
import com.volcengine.ck.album.base.AlbumConfig
import com.volcengine.ck.logkit.LogKit
import com.volcengine.effectone.auto.album.AutoAlbumActivity
import com.volcengine.effectone.auto.moment.hl.AutoMaterialRecognizeActivity
import com.volcengine.effectone.singleton.AppSingleton

class AutoMaterialRecognizeAlbumContract(private val activity: FragmentActivity) :
    ActivityResultContract<AlbumConfig, List<IMaterialItem>>() {
    override fun createIntent(context: Context, input: AlbumConfig): Intent {
        return Intent(activity, AutoAlbumActivity::class.java).apply {
            putExtra(ALBUM_CONFIG_KEY, input)
        }
    }
    override fun parseResult(resultCode: Int, intent: Intent?): List<IMaterialItem> {
        return intent?.let { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            it.getParcelableArrayListExtra(AutoMaterialRecognizeAlbumLauncher.ARGUMENT_KEY_MEDIA_LIST,IMaterialItem::class.java)
        } else {
            it.getParcelableArrayListExtra(AutoMaterialRecognizeAlbumLauncher.ARGUMENT_KEY_MEDIA_LIST)
        }
        }?: emptyList()
    }

    companion object{
         const val TAG = "AutoMaterialRecognizeAlbumContract"
    }
}

class StartMaterialRecognizeAlbumFinish : IAlbumFinish {
    override suspend fun finishAction(
        activity: Activity,
        mediaList: List<IMaterialItem>,
        albumConfig: AlbumConfig
    ) {
        mediaList.forEach {
            val fileExist = isMediaExists(it)
            if (fileExist.not()) {
                Toast.makeText(AppSingleton.instance, com.volcengine.effectone.auto.album.R.string.eo_album_material_not_exist,Toast.LENGTH_SHORT).show()
                return
            }
        }
        val itemList = mediaList.distinctBy { it.uri }
        AutoMaterialRecognizeActivity.launchPage(activity, itemList)

        //关闭相册页
        if (!activity.isFinishing) {
            activity.finish()
        }
    }
}

class StartMaterialRecognizeLauchAlbumFinish : IAlbumFinish {
    override suspend fun finishAction(
        activity: Activity,
        mediaList: List<IMaterialItem>,
        albumConfig: AlbumConfig
    ) {
        mediaList.forEach {
            val fileExist = isMediaExists(it)
            if (fileExist.not()) {
                Toast.makeText(AppSingleton.instance, com.volcengine.effectone.auto.album.R.string.eo_album_material_not_exist,Toast.LENGTH_SHORT).show()
                return
            }
        }
        if (mediaList.isEmpty()) {
            LogKit.d(AutoMaterialRecognizeAlbumContract.TAG, "finishAction()  mediaList is empty")
            return
        }
        val itemList = mediaList.distinctBy { it.uri }
        val data = Intent()
        data.putParcelableArrayListExtra(AutoMaterialRecognizeAlbumLauncher.ARGUMENT_KEY_MEDIA_LIST, ArrayList(itemList))
        activity.setResult(Activity.RESULT_OK, data)

        //关闭相册页
        if (!activity.isFinishing) {
            activity.finish()
        }
    }
}
