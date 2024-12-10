package com.volcengine.effectone.auto.business

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import com.effectsar.labcv.effectsdk.EffectsSDKEffectConstants.EffectsSDKResultCode
import com.effectsar.labcv.licenselibrary.EffectsSDKLicenseWrapper
import com.ss.android.ugc.aweme.views.setDebounceOnClickListener
import com.volcengine.auth.api.EOAuthorizationInternal
import com.volcengine.ck.album.AlbumEntrance
import com.volcengine.ck.album.base.AlbumConfig
import com.volcengine.effectone.EffectOneSdk
import com.volcengine.effectone.auto.album.AutoAlbumEntrance
import com.volcengine.effectone.auto.business.hl.AutoHLMFinishImpl
import com.volcengine.effectone.auto.moment.AutoMomentListActivity
import com.volcengine.effectone.auto.moment.hl.launcher.StartMaterialRecognizeAlbumFinish
import com.volcengine.effectone.auto.recorder.config.AutoRecordConstant
import com.volcengine.effectone.auto.algorithm.AlgorithmActivity
import com.volcengine.effectone.auto.business.auth.AuthID
import com.volcengine.effectone.auto.templates.ui.AutoTemplatesHomeActivity
import com.volcengine.effectone.recordersdk.arsdk.algorithm.tasks.ChildrenAlgorithmTask
import com.volcengine.effectone.recordersdk.arsdk.algorithm.tasks.LicenseCakeAlgorithmTask
import com.volcengine.effectone.recorderui.util.RecordUtils
import com.volcengine.effectone.singleton.AppSingleton
import com.volcengine.effectone.utils.EOUtils
import com.volcengine.effectone.widget.EOToaster

/**
 *Author: gaojin
 *Time: 2023/12/17 18:13
 */
@Keep
class EffectAutoMainFragment : Fragment() {

    companion object {
        const val TAG = "EffectOneMainFragment"
    }

    private val albumPermissions by lazy {
        mutableListOf<String>().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
                add(Manifest.permission.READ_MEDIA_VIDEO)
                add(Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private var authResult = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AutoQuickInitHelper.prepareAndInit { isSuccess, msg ->
            authResult = isSuccess
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.eo_main_fragment_main, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        addInternalView(view as ViewGroup)
        view.findViewById<View>(R.id.main_title).setOnLongClickListener {
            try {
                startActivity(Intent("auto_debug_page").apply {
                    setPackage(AppSingleton.instance.packageName)
                    putExtra("eo_debug_fragment_title", "The Moment")
                })
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
            return@setOnLongClickListener true
        }
        //record and editor ability
        view.findViewById<View>(R.id.main_ordinary_record).setDebounceOnClickListener {
            if (authResult && checkRecord()) {
                AutoQuickInitHelper.startRecorder(requireActivity(), AutoRecordConstant.ORDINARY_SHOOT)
            } else {
                EOToaster.show(AppSingleton.instance, R.string.eo_frontpage_auth_fail)
            }
        }

        view.findViewById<View>(R.id.main_hl_record).setDebounceOnClickListener {
            if (authResult && highlightEnable() && checkRecord()) {
                AutoQuickInitHelper.startRecorder(requireActivity(), AutoRecordConstant.HIGHLIGHT_SHOOT)
            } else {
                EOToaster.show(AppSingleton.instance, R.string.eo_frontpage_auth_fail)
            }
        }

        //intelligent create ability
        view.findViewById<View>(R.id.main_timeline_story).setDebounceOnClickListener {
            if (authResult && cutSameEnable() && highlightEnable()) {
                EOUtils.permission.checkPermissions(requireActivity(), albumPermissions, {
                    startActivity(Intent(requireActivity(), AutoMomentListActivity::class.java))
                }, {
                    AlbumEntrance.showAlbumPermissionTips(requireActivity())
                })
            } else {
                EOToaster.show(AppSingleton.instance, R.string.eo_frontpage_auth_fail)
            }
        }

        view.findViewById<View>(R.id.main_highlight_film).setDebounceOnClickListener {
            if (authResult && cutSameEnable() && highlightEnable()) {
                EOUtils.permission.checkPermissions(requireActivity(), AutoAlbumEntrance.albumPermissions, {
                    val albumConfig = AlbumConfig(
                        allEnable = false,
                        imageEnable = false,
                        videoEnable = true,
                        maxSelectCount = EffectOneSdk.albumMaxSelectedCount,
                        finishClazz = AutoHLMFinishImpl::class.java
                    )
                    AutoAlbumEntrance.startChooseMedia(
                        requireActivity(), 1001, albumConfig
                    )
                }, {
                    AlbumEntrance.showAlbumPermissionTips(requireActivity())
                })
            } else {
                EOToaster.show(AppSingleton.instance, R.string.eo_frontpage_auth_fail)
            }
        }

        view.findViewById<View>(R.id.main_templates).setDebounceOnClickListener {
            if (authResult && cutSameEnable()) {
                EOUtils.permission.checkPermissions(requireActivity(), AutoAlbumEntrance.albumPermissions, {
                    AutoTemplatesHomeActivity.launch(requireActivity())
                }, {
                    AutoTemplatesHomeActivity.launch(requireActivity())
                })
            } else {
                EOToaster.show(AppSingleton.instance, R.string.eo_frontpage_auth_fail)
            }
        }

        //onboard_algorithms
        view.findViewById<View>(R.id.main_car_desensitization).setDebounceOnClickListener {
            val recordPermissions = mutableListOf<String>().apply {
                add(Manifest.permission.CAMERA)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.READ_MEDIA_IMAGES)
                    add(Manifest.permission.READ_MEDIA_VIDEO)
                    add(Manifest.permission.READ_MEDIA_AUDIO)
                } else {
                    add(Manifest.permission.READ_EXTERNAL_STORAGE)
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
            if (authResult && desensitizationEnable()) {
                EOUtils.permission.checkPermissions(requireActivity(), recordPermissions, {
                    AlgorithmActivity.startActivity(requireActivity(), LicenseCakeAlgorithmTask.LICENSE_CAKE)
                }, {
                    RecordUtils.showRecordPermissionTips(requireActivity(), it)
                })
            } else {
                EOToaster.show(AppSingleton.instance, R.string.eo_frontpage_auth_fail)
            }
        }

        view.findViewById<View>(R.id.main_car_category).setDebounceOnClickListener {
            if (authResult && highlightEnable()) {
                EOUtils.permission.checkPermissions(requireActivity(), AutoAlbumEntrance.albumPermissions, {
                    val albumConfig = AlbumConfig(
                        allEnable = true,
                        imageEnable = true,
                        videoEnable = true,
                        maxSelectCount = EffectOneSdk.albumMaxSelectedCount,
                        finishClazz = StartMaterialRecognizeAlbumFinish::class.java
                    )
                    AutoAlbumEntrance.startChooseMedia(
                        requireActivity(), 1001, albumConfig
                    )
                }, {
                    AlbumEntrance.showAlbumPermissionTips(requireActivity())
                })
            } else {
                EOToaster.show(AppSingleton.instance, R.string.eo_frontpage_auth_fail)
            }
        }
        view.findViewById<View>(R.id.main_car_child).setDebounceOnClickListener {
            val recordPermissions = mutableListOf<String>().apply {
                add(Manifest.permission.CAMERA)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.READ_MEDIA_IMAGES)
                    add(Manifest.permission.READ_MEDIA_VIDEO)
                    add(Manifest.permission.READ_MEDIA_AUDIO)
                } else {
                    add(Manifest.permission.READ_EXTERNAL_STORAGE)
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
            if (authResult && childEnable()) {
                EOUtils.permission.checkPermissions(requireActivity(), recordPermissions, {
                    AlgorithmActivity.startActivity(requireActivity(), ChildrenAlgorithmTask.CHILDREN_DETECTION)
                }, {
                    RecordUtils.showRecordPermissionTips(requireActivity(), it)
                })
            } else {
                EOToaster.show(AppSingleton.instance, R.string.eo_frontpage_auth_fail)
            }

        }
    }

    /**
     * 剪同款是否可用
     */
    private fun cutSameEnable(): Boolean {
        return EOAuthorizationInternal.getCutSameLicensePath() != null
    }

    /**
     * 儿童检测是否可用
     */
    private fun childEnable(): Boolean {
        return checkId(AuthID.CHILD)
    }

    /**
     * 车端脱敏是否可用
     */
    private fun desensitizationEnable(): Boolean {
        return checkId(AuthID.DESENSITIZATION.toInt())
    }

    /**
     * 高光算法是否可用
     */
    private fun highlightEnable(): Boolean {
        return checkId(AuthID.BACH_CLASSIFICATION.toInt()) && checkId(AuthID.BASIC.toInt())
    }

    private fun checkRecord(): Boolean {
        return checkId(AuthID.BASIC.toInt()) && checkId(AuthID.FUNCTION_REC_CREATE.toInt())
    }

    private fun checkId(id: Int): Boolean {
        val licensePath = EOAuthorizationInternal.getVELicensePath() ?: return false
        val code = EffectsSDKLicenseWrapper.checkLicenseNomap(licensePath, id, false)
        return code == EffectsSDKResultCode.BEF_RESULT_SUC
    }

    private fun addInternalView(rootView: ViewGroup) {
        // for aop hook
    }
}