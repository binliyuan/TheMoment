package com.volcengine.effectone.auto.templates.download

import java.io.File

/**
 *Author: gaojin
 *Time: 2022/10/31 14:35
 */

data class DownloadResult(
    val file: File?,
    val e: Exception?
)