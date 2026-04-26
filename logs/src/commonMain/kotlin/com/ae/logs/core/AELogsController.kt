package com.ae.logs.core

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Controls the visibility of the AELogs UI overlay.
 *
 * Access via [LocalAELogsController] inside [com.ae.logs.AELogsProvider].
 */
public class AELogsController {
    private val _isVisible = MutableStateFlow(false)
    public val isVisible: StateFlow<Boolean> = _isVisible.asStateFlow()

    public fun show() {
        _isVisible.value = true
    }

    public fun hide() {
        _isVisible.value = false
    }

    public fun toggle() {
        _isVisible.value = !_isVisible.value
    }
}

/**
 * CompositionLocal providing the [AELogsController].
 *
 * Available anywhere inside [com.ae.logs.AELogsProvider].
 */
public val LocalAELogsController: ProvidableCompositionLocal<AELogsController> =
    compositionLocalOf {
        error("AELogsController not provided. Wrap your content with AELogsProvider.")
    }
