package com.volcengine.effectone.auto.moment.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.volcengine.effectone.auto.moment.R
import com.volcengine.effectone.auto.moment.configure.MomentConfigureHelper.TagSelectStatus

interface MomentConfigureSelectTagListener {
    fun onFinishedSelectTags(choices: List<String>)
}

class MomentConfigureSelectLabelDialog(
    context: Context,
    private val newSelectedTags: Set<String>,
    private val tagWithStatus: Map<String, TagSelectStatus>,
    private val listener: MomentConfigureSelectTagListener
) : Dialog(context) {
    private val confirmBtn by lazy {
        findViewById<Button>(R.id.confirm_btn)
    }
    private val resetBtn by lazy {
        findViewById<Button>(R.id.reset_btn)
    }
    private val cancelBtn by lazy {
        findViewById<Button>(R.id.cancel_btn)
    }

    private val selectedItems = newSelectedTags.toMutableSet()
    private lateinit var adapter : MomentConfigureSelectTagAdapter

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_auto_moment_select_tag)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // selected tags should move from SELECTED, in case of
        // changing it to UNSELECTED
        val actualTagWithStatus = tagWithStatus.toMutableMap()
        newSelectedTags.forEach{
            actualTagWithStatus[it] = TagSelectStatus.UNSELECTED
        }

        val recyclerView = findViewById<RecyclerView>(R.id.ck_moment_configure_select_labels_view)
        recyclerView.layoutManager = GridLayoutManager(context, 3)
        adapter = MomentConfigureSelectTagAdapter(context ,newSelectedTags, actualTagWithStatus,
            object : MomentConfigureSelectTagAdapter.ItemClickListener {
                override fun onItemClick(item: String, isSelected: Boolean) {
                    if(isSelected){
                        selectedItems.add(item)
                    }else{
                        selectedItems.remove(item)
                    }
                }
            })
        recyclerView.adapter = adapter

        confirmBtn.setOnClickListener {
            listener.onFinishedSelectTags(selectedItems.toList())
            super.dismiss()
        }

        resetBtn.setOnClickListener {
            selectedItems.clear()
            newSelectedTags.toMutableSet().forEach {
                selectedItems.add(it)
            }
            adapter.resetSelectedTags()
        }

        cancelBtn.setOnClickListener {
            dismiss()
        }
    }

    override fun dismiss() {
        super.dismiss()
    }
}

class MomentConfigureSelectTagAdapter(
    private val context: Context,
    private val selectedTags: Set<String>,
    private val tagWithStatus: Map<String, TagSelectStatus>,
    private val itemClickListener: ItemClickListener
) : RecyclerView.Adapter<MomentConfigureSelectTagAdapter.SelectLabelViewHolder>() {

    private val selectedPositions = mutableSetOf<Int>()
    private val items = tagWithStatus.keys.toList()

    init {
        items.forEachIndexed { index, s ->
            if(selectedTags.contains(s)){
                selectedPositions.add(index)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun resetSelectedTags() {
        selectedPositions.clear()
        items.forEachIndexed { index, s ->
            if(selectedTags.contains(s)){
                selectedPositions.add(index)
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectLabelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.auto_moment_selectable_tag_view, parent, false)
        return SelectLabelViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: SelectLabelViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, selectedPositions.contains(position),
                    tagWithStatus[item] == TagSelectStatus.UNSELECTED)
        holder.itemView.setOnClickListener {
            if(tagWithStatus[item] == TagSelectStatus.SELECTED) {
                return@setOnClickListener
            }
            val isSelected = selectedPositions.contains(position)
            if (isSelected) {
                selectedPositions.remove(position)
            } else {
                selectedPositions.add(position)
            }
            itemClickListener.onItemClick(item, !isSelected)
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = tagWithStatus.size

    class SelectLabelViewHolder(itemView: View, val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.textViewItem)

        fun bind(text: String, isSelected: Boolean, isAvailable: Boolean) {
            textView.text = text
            textView.setTextColor(
                if (isSelected) {
                    ContextCompat.getColor(context, R.color.selected_label_text)
                } else {
                    if (isAvailable) {
                        ContextCompat.getColor(context, R.color.normal_widget_text)
                    } else {
                        ContextCompat.getColor(context, R.color.disabled_label_text)
                    }
                }
            )
        }
    }

    interface ItemClickListener {
        fun onItemClick(item: String, isSelected: Boolean)
    }
}