package com.volcengine.effectone.auto.moment.hl.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

/**
 *Author: gaojin
 *Time: 2024/5/23 20:43
 */
class AutoMomentScope : CoroutineScope, Closeable {

    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun close() {
        job.cancel()
    }
}