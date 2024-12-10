package com.volcengine.effectone.auto.moment.configure

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class MomentConfigureTagItem(
    var tags: List<String>,
    var confidence: Float,
    var priority: Int,
    var id: Long,
) {
}

class CKMomentConfigureTagItemViewModel : ViewModel(), MomentConfigureTagItemChangeListener {
    val tagItems: MutableLiveData<List<MomentConfigureTagItem>> = MutableLiveData()
    private var idCount: Long = 0

    fun newItem() : MomentConfigureTagItem {
        val newEmptyLabelItem = MomentConfigureTagItem(
            tags = emptyList(),
            confidence = 0.8f,
            priority = (tagItems.value?.size ?: 0) + 1,
            idCount
        )
        idCount++
        return newEmptyLabelItem
    }

    fun addNewItem() {
        val updatedItems = tagItems.value?.toMutableList() ?: mutableListOf()
        updatedItems.add(newItem())
        tagItems.value = updatedItems
    }

    override fun changeItemConfidence(item: MomentConfigureTagItem, conf: Float){
        if (conf >= 1.0 || conf <= 0.0) return
        val updatedItems = tagItems.value?.toMutableList() ?: mutableListOf()
        updatedItems.forEach {
            if(it.id == item.id){
                it.confidence = conf
            }
        }
        tagItems.value = updatedItems
    }

    override fun changeItemTages(item: MomentConfigureTagItem, newTags: List<String>) {
        val updatedItems = tagItems.value?.toMutableList() ?: mutableListOf()
        updatedItems.forEach {
            if(it.id == item.id){
                it.tags = newTags
            }
        }
        tagItems.value = updatedItems
    }

    override fun changeItemPriority(item: MomentConfigureTagItem, newPriority: Int) {
        val oldPos = item.priority - 1
        val newPos = newPriority - 1
        if( oldPos == newPos ) return
        val newItems = tagItems.value?.toMutableList() ?: return

        val element = newItems.removeAt(oldPos)
        newItems.add(newPos, element)

        newItems.forEachIndexed { index, it ->
            it.priority = index + 1
        }

        tagItems.value = newItems.toList()
    }

    override fun deleteItem(item: MomentConfigureTagItem) {
        if ((tagItems.value?.size ?: 0) <= 1) return
        val updatedItems = tagItems.value?.filter {
            item.id != it.id
        }?.toMutableList() ?: mutableListOf()
        updatedItems.forEachIndexed { i, it ->
            it.priority = i + 1
        }

        tagItems.value = updatedItems.toList()
    }

    fun setItem(newLabelItems : List<MomentConfigureTagItem>) {
        tagItems.value = newLabelItems.map{
            newItem().apply{
                tags = it.tags
                priority = it.priority
                confidence = it.confidence
            }
        }
    }
}

