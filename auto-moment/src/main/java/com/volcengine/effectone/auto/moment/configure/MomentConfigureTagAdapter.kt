package com.volcengine.effectone.auto.moment.configure

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.volcengine.effectone.auto.moment.R
import com.volcengine.effectone.auto.moment.configure.Constants.MOMENT_CONFIDENCE_CHANGE_STEP
import com.volcengine.effectone.auto.moment.dialog.MomentConfigureSelectLabelDialog
import com.volcengine.effectone.auto.moment.dialog.MomentConfigureSelectTagListener

interface MomentConfigureTagItemChangeListener {
    fun changeItemTages(item: MomentConfigureTagItem, newTags: List<String>)
    fun changeItemPriority(item: MomentConfigureTagItem, to: Int)
    fun changeItemConfidence(item: MomentConfigureTagItem, conf: Float)
    fun deleteItem(item: MomentConfigureTagItem)
}

class MomentConfigureTagAdapter(private var items: List<MomentConfigureTagItem>,
                                private var helper: MomentConfigureHelper,
                                private val onItemChangeListener: MomentConfigureTagItemChangeListener
                                ) :
    RecyclerView.Adapter<MomentConfigureTagAdapter.TagItemViewHolder>() {

    inner class TagItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val tagTitleView: TextView = view.findViewById(R.id.ck_moment_label_tag_label)
        val tagView: EditText = view.findViewById(R.id.ck_moment_configure_tag_view)
        val prioritySpinner: Spinner = view.findViewById(R.id.ck_moment_configure_tag_priority)
        val deleteView: TextView = view.findViewById(R.id.ck_moment_configure_item_delete)
        val confView: TextView = view.findViewById(R.id.ck_moment_configure_confidence)
        val confDecrementView: TextView = view.findViewById(R.id.ck_moment_configure_confidence_button_decrement)
        val confIncrementView: TextView = view.findViewById(R.id.ck_moment_configure_confidence_button_increment)

        var selectedTags: Set<String> = setOf()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagItemViewHolder {
        val itemView = LayoutInflater.from(parent.context).
            inflate(R.layout.auto_moment_configure_tag_item, parent, false)
        return TagItemViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TagItemViewHolder, position: Int) {
        val currentItem = items[position]
        holder.tagTitleView.text =
            holder.tagTitleView.context.getString(R.string.ck_one_moment_configure_tag) + currentItem.priority.toString()

        val priorities = items.map {it.priority}

        val adapter = ArrayAdapter(holder.view.context,
            R.layout.auto_moment_configure_spinner_item,
            priorities)
        holder.prioritySpinner.adapter = adapter
        holder.prioritySpinner.setSelection(currentItem.priority - 1)

        holder.prioritySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                onItemChangeListener.changeItemPriority(currentItem,
                    (p1!! as TextView).text.toString().toInt())
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        holder.selectedTags = currentItem.tags.toSet()
        holder.tagView.setText(formatTagsToView(currentItem.tags))
        holder.tagView.setOnClickListener {
            val dialog = MomentConfigureSelectLabelDialog(holder.tagView.context,
                holder.selectedTags,
                helper.getAllTagWithStatus(),
            object : MomentConfigureSelectTagListener {
                override fun onFinishedSelectTags(choices: List<String>) {
                    holder.selectedTags.forEach {ch ->
                        helper.unselectTag(ch)
                    }
                    holder.tagView.setText(formatTagsToView(choices))
                    holder.selectedTags = choices.toSet()
                    choices.forEach {ch ->
                        helper.selectTag(ch)
                    }
                    onItemChangeListener.changeItemTages(currentItem, choices)
                }
            })

            dialog.show()
        }

        if (items.size > 1) {
            holder.deleteView.visibility = View.VISIBLE
            holder.deleteView.setOnClickListener {
                currentItem.tags.forEach {
                    helper.unselectTag(it)
                }
                onItemChangeListener.deleteItem(currentItem)
            }
        } else {
            holder.deleteView.visibility = View.INVISIBLE
            holder.deleteView.setOnClickListener {
            }
        }

        holder.confView.text = String.format("%.1f", currentItem.confidence)
        holder.confDecrementView.setOnClickListener {
            onItemChangeListener.changeItemConfidence(currentItem,
                currentItem.confidence - MOMENT_CONFIDENCE_CHANGE_STEP)
        }
        holder.confIncrementView.setOnClickListener {
            onItemChangeListener.changeItemConfidence(currentItem,
                currentItem.confidence + MOMENT_CONFIDENCE_CHANGE_STEP)
        }
    }

    override fun getItemCount() = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(newItems : List<MomentConfigureTagItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    private fun formatTagsToView(tags: List<String>): String {
        return tags.joinToString(", ")
    }

}