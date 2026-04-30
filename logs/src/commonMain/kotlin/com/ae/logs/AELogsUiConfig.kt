package com.ae.logs

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * UI-specific configuration for the AELogs overlay.
 *
 * Passed to [AELogsProvider] separately from the core [AELogsConfig],
 * keeping Compose types out of the `logs-core` module.
 *
 * ```kotlin
 * AELogsProvider(
 *     inspector = inspector,
 *     uiConfig = AELogsUiConfig(
 *         showFloatingButton = true,
 *         presentationMode = PresentationMode.Adaptive,
 *     ),
 * ) { ... }
 * ```
 */
public data class AELogsUiConfig(
    /** Show the floating debug button overlay. Default: `true`. */
    val showFloatingButton: Boolean = true,
    /** Floating button screen position. Default: `BottomEnd`. */
    val floatingButtonAlignment: Alignment = Alignment.BottomEnd,
    /**
     * Bottom offset for the floating button — useful to clear nav bars or bottom bars.
     * Default: 0.dp.
     */
    val floatingButtonOffset: Dp = 0.dp,
    /** Enable long-press anywhere on screen to open the panel. Default: `false`. */
    val enableLongPress: Boolean = false,
    /**
     * Custom Material3 [ColorScheme] for the AELogs UI.
     * `null` uses the built-in brand theme.
     */
    val colorScheme: ColorScheme? = null,
    /**
     * How the AELogs panel is presented on screen.
     * Default: [PresentationMode.Adaptive] (bottom sheet on phone, dialog on tablet).
     */
    val presentationMode: PresentationMode = PresentationMode.Adaptive,
)

/**
 * Controls how the AELogs panel container is displayed.
 */
public enum class PresentationMode {
    /** Bottom sheet on compact screens, centered dialog on large screens (default). */
    Adaptive,

    /** Always use a bottom sheet, regardless of screen size. */
    BottomSheet,

    /** Always use a centered dialog, regardless of screen size. */
    Dialog,
}
