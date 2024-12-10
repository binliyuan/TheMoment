package com.volcengine.effectone.auto.moment.configure

import android.content.Context
import com.google.gson.Gson
import com.volcengine.ck.moment.base.CKMoment
import com.volcengine.ck.moment.base.CKMomentConfig
import com.volcengine.ck.moment.base.CKMomentRequiredTag
import com.volcengine.ck.moment.base.CKMomentRequirements
import com.volcengine.effectone.auto.moment.configure.Constants.MOMENT_ALL_LOCATION
import com.volcengine.effectone.auto.moment.configure.Constants.MOMENT_CONFIGURE_ROOT_PATH
import com.volcengine.effectone.auto.moment.configure.Constants.MOMENT_DEFAULT_CONFIG_ASSET_JSON_NAME
import com.volcengine.effectone.auto.moment.configure.Constants.MOMENT_DEFAULT_CONFIG_ASSET_PATH
import com.volcengine.effectone.utils.EOUtils.fileUtil
import com.volcengine.effectone.singleton.AppSingleton
import java.io.File

class MomentConfigureHelper(
    context: Context,
    private val directoryName: String = MOMENT_CONFIGURE_ROOT_PATH) {
    private lateinit var directoryPath: String
    private var lastExportedConfigs: List<CKMoment> = listOf()
    private val gson by lazy { Gson()}

    private val defaultConfigs: MutableList<CKMoment> = mutableListOf()
    private val availableTagsMap: MutableMap<String, List<String>> = mutableMapOf()

    // follow members will change when switch CKMoment
    private val tagSelectStatues: MutableMap<String, TagSelectStatus> = mutableMapOf()

    enum class TagSelectStatus {
        UNSELECTED,
        SELECTED,
    }

    init {
        val workPath = File(context.filesDir, directoryName)
        if(!workPath.exists()){
            workPath.mkdirs()
        }
        directoryPath = workPath.absolutePath
    }

    // Read and deserialize JSON files from the specified directory
    fun loadConfigs(): List<CKMoment> {
        val configs = File(directoryPath).listFiles { _, name -> name.endsWith(".json") }
            ?.mapNotNull { file ->
                try {
                    gson.fromJson<CKMoment>(file.readText(), CKMoment::class.java)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }.orEmpty().ifEmpty { defaultCKMomentConfig() }
        lastExportedConfigs = configs
        return configs
    }

    // Compare and update configurations if there are any changes
    fun updateConfigs(newConfigs: List<CKMoment>) {
        val changes = findChanges(lastExportedConfigs, newConfigs)
        changes.forEach { (file, config) ->
            File(directoryPath, "$file.json").writeText(gson.toJson(config))
        }
        lastExportedConfigs = newConfigs
    }

    fun startEditMoment(moment: CKMoment) {
        tagSelectStatues.clear()
        getAllAvailableTags(moment.config.id)?.forEach {
            tagSelectStatues[it] = TagSelectStatus.UNSELECTED
        }
        moment.config.requirement.requireTags.forEach {
            selectTag(it.tag)
        }
    }

    fun getAllTagWithStatus() : Map<String, TagSelectStatus> {
        return tagSelectStatues
    }

    fun unselectAllTag() {
        tagSelectStatues.keys.forEach { tag ->
            tagSelectStatues[tag] = TagSelectStatus.UNSELECTED
        }
    }

    fun selectTag(tag: String) {
        tagSelectStatues[tag] = TagSelectStatus.SELECTED
    }

    fun unselectTag(tag: String) {
        tagSelectStatues[tag] = TagSelectStatus.UNSELECTED
    }

    fun getAllLocation() : List<String> {
        return MOMENT_ALL_LOCATION
    }

    // Identify changes in configurations
    private fun findChanges(oldConfigs: List<CKMoment>, newConfigs: List<CKMoment>): Map<String, CKMoment> {
        val oldConfigMap = oldConfigs.associateBy { it.config.id }
        val newConfigMap = newConfigs.associateBy { it.config.id }

//        return newConfigMap.filter { (id, newConfig) ->
//            val oldConfig = oldConfigMap[id]
//            oldConfig == null || oldConfig != newConfig
//        }
        return newConfigMap
    }

    private fun defaultCKMomentConfig(): List<CKMoment>{
        if (defaultConfigs.isNotEmpty()) {
            return defaultConfigs
        }

        val files = AppSingleton.instance.assets.list(MOMENT_DEFAULT_CONFIG_ASSET_PATH) ?: return emptyList()
        defaultConfigs.addAll(files.map{path ->
            val content = fileUtil.readJsonFromAssets(AppSingleton.instance,
                "${MOMENT_DEFAULT_CONFIG_ASSET_PATH}/${path}/${MOMENT_DEFAULT_CONFIG_ASSET_JSON_NAME}")
            if (content == "") {
                CKMoment(
                    id = "xxxxxx",
                    title = "read error 读取错误",
                    config = CKMomentConfig(
                        id = "xxxxxxxx",
                        requirement = CKMomentRequirements(
                            requireTags = listOf(
                                CKMomentRequiredTag(
                                    "xxxxx", 1, 0.8F
                                ),
                            ),
                            minScores = 0.3F,
                            minCount = 10,
                        ),
                        momentTemplates = emptyList()
                    ),
                )
            } else {
                Gson().fromJson(content, CKMoment::class.java)
            }
        }
        )

        return defaultConfigs
    }

    private fun getAllAvailableTags(id : String) : List<String>? {
        if (availableTagsMap.isNotEmpty()) {
            return availableTagsMap[id]
        }
        val configs = defaultCKMomentConfig()
        configs.forEach { moment ->
            availableTagsMap[moment.config.id] =
                moment.config.requirement.requireTags.map {
                it.tag
            }
        }

        return availableTagsMap[id]
    }
}