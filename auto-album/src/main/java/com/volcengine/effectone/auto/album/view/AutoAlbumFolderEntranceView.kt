package com.volcengine.effectone.auto.album.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bytedance.creativex.mediaimport.view.internal.folder.MediaSelectFolderEntranceView
import com.volcengine.effectone.auto.album.R

class AutoAlbumFolderEntranceView(
    root: ViewGroup,
    folderListContentView: View,
    enableFolderList: Boolean = true,
    viewConfigureBuilder: ((ViewConfigure) -> Unit)? = null
) : MediaSelectFolderEntranceView(root, folderListContentView, enableFolderList, viewConfigureBuilder) {

    override fun provideContentView(root: ViewGroup): ViewGroup {
        return LayoutInflater.from(root.context).inflate(R.layout.auto_album_import_folder_entrance_view, root, false) as ViewGroup
    }
}