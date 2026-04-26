package com.ae.logs

import com.ae.logs.core.AELogsPlugin
import com.ae.logs.plugins.logs.LogsPlugin
import kotlinx.atomicfu.atomic

/**
 * One-stop setup helper for apps using the `:logs` all-in-one dependency.
 *
 * ## Idempotent
 * [init] is safe to call multiple times — only the **first** call installs plugins.
 * `install()` deduplicates by plugin ID so even a concurrent double-init
 * just results in harmless no-op second installs.
 *
 * ```kotlin
 * AELogsSetup.init()   // installs LogsPlugin
 * AELogsSetup.init()   // no-op — returns same instance immediately
 * ```
 *
 * ## Custom plugins
 * ```kotlin
 * AELogsSetup.init(
 *     config  = AELogsConfig(),
 *     plugins = listOf(LogsPlugin(maxEntries = 2000), NetworkPlugin(), AnalyticsPlugin()),
 * )
 * ```
 *
 * ## Logging after init
 * ```kotlin
 * AELogs.d("MyTag", "Hello!")
 * AELogs.e("MyTag", "Crash!", throwable)
 * ```
 */
public object AELogsSetup {
    private val initialized = atomic(false)

    /**
     * Initialize AELogs. **Idempotent** — safe to call multiple times.
     *
     * Only the first call installs [plugins] onto [AELogs.default].
     * Subsequent calls return immediately with the same instance.
     *
     * @param config   Core configuration (only applied on first call).
     * @param plugins  Plugins to install. Defaults to [LogsPlugin] only.
     * @return The shared [AELogs.default] instance.
     */
    public fun init(
        config: AELogsConfig = AELogsConfig(),
        plugins: List<AELogsPlugin> = listOf(LogsPlugin()),
    ): AELogs {
        if (!initialized.compareAndSet(expect = false, update = true)) {
            return AELogs.default
        }

        val inspector = AELogs.default
        plugins.forEach { inspector.install(it) } // install() deduplicates by plugin ID
        return inspector
    }
}
