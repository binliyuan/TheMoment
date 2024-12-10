package com.volcengine.effectone.auto.moment.configure

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.volcengine.effectone.auto.moment.R


class MomentConfigureConfigSelectArrayAdapter(context: Context,
                                              resource: Int,
                                              objects: MutableList<String>,
                                              private var idMapMomentName: (String) -> String) :
    ArrayAdapter<String>(context, resource, objects){

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val id = getItem(position) ?: ""
        val tagString = idMapMomentName(id)

        val textView = view.findViewById<TextView>(R.id.ck_moment_configure_spinner_item_text_view)
        textView.text = tagString

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getView(position, convertView, parent)
    }

}