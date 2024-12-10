package com.volcengine.effectone.auto.recorder.hl

/**
 *Author: gaojin
 *Time: 2023/10/10 17:06
 */

data class PreviewData(
    val width: Int,
    val height: Int,
    val ptsMs: Long,
    val intArray: IntArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PreviewData

        if (width != other.width) return false
        if (height != other.height) return false
        if (!intArray.contentEquals(other.intArray)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + intArray.contentHashCode()
        return result
    }
}