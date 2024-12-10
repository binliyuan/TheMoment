package com.volcengine.effectone.auto.album.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bytedance.creativex.mediaimport.view.internal.main.MediaSelectTitleView
import com.volcengine.effectone.auto.album.R

/**
 *Author: gaojin
 *Time: 2021/11/18 4:55 PM
 */
class AutoAlbumSelectTitleView(
    private val root: ViewGroup,
    private val showCloseView: Boolean = true
) : MediaSelectTitleView(root, showCloseView) {

    override fun provideContentView(root: ViewGroup): ViewGroup {
        return LayoutInflater.from(root.context).inflate(R.layout.auto_album_import_title_view, root, true) as ViewGroup
    }

    override fun provideCloseView(content: ViewGroup): View? {
        return content.findViewById(R.id.btn_close)
    }
}