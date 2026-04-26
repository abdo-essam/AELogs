package com.ae.logs

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.ae.logs.core.AELogsController
import com.ae.logs.core.LocalAELogsController
import com.ae.logs.core.UIPlugin
import com.ae.logs.ui.AELogsContainer
import com.ae.logs.ui.AELogsFloatingButton
import com.ae.logs.ui.theme.AELogsTheme
import com.ae.logs.ui.theme.AELogsSpacing

/**
 * Top-level composable wrapper that enables the AELogs overlay.
 *
 * Wrap your entire app content with this composable.
 *
 * ```kotlin
 * @Composable
 * fun App() {
 *     AELogsProvider(
 *         inspector = AELogs.default,
 *         enabled = BuildConfig.DEBUG,
 *     ) {
 *         MaterialTheme { MainNavigation() }
 *     }
 * }
 * ```
 *
 * @param inspector     The [AELogs] instance to observe.
 * @param uiConfig      UI-specific configuration (button visibility, theme, etc.).
 * @param enabled       Set to `false` in release builds for zero overhead.
 * @param content       Your app's content.
 */
@Composable
public fun AELogsProvider(
    inspector: AELogs = AELogs.default,
    uiConfig: AELogsUiConfig = AELogsUiConfig(),
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    if (!enabled) {
        content()
        return
    }

    val controller = remember { AELogsController() }
    val isVisible by controller.isVisible.collectAsState()

    // Plugins are expected to be static after init, but we still use collectAsState()
    // because StateFlow.value is NOT observable by Compose — derivedStateOf won't help here.
    val plugins by inspector.plugins.collectAsState()
    val uiPlugins = remember(plugins) { plugins.filterIsInstance<UIPlugin>() }

    // Notify plugins of open/close lifecycle events
    LaunchedEffect(isVisible) {
        if (isVisible) inspector.notifyOpen() else inspector.notifyClose()
    }

    CompositionLocalProvider(LocalAELogsController provides controller) {
        AELogsTheme(colorScheme = uiConfig.colorScheme) {
            BoxWithConstraints(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .then(
                            if (uiConfig.enableLongPress) {
                                Modifier.pointerInput(Unit) {
                                    detectTapGestures(onLongPress = { controller.show() })
                                }
                            } else {
                                Modifier
                            },
                        ),
            ) {
                content()

                if (uiConfig.showFloatingButton) {
                    AELogsFloatingButton(
                        onClick = { controller.show() },
                        modifier =
                            Modifier
                                .align(uiConfig.floatingButtonAlignment)
                                .padding(end = AELogsSpacing.x5, bottom = uiConfig.floatingButtonOffset),
                    )
                }

                if (isVisible) {
                    AELogsContainer(
                        plugins = uiPlugins,
                        isLargeScreen = maxWidth > 600.dp,
                        presentationMode = uiConfig.presentationMode,
                        onDismiss = { controller.hide() },
                    )
                }
            }
        }
    }
}

/**
 * Extension to get all [UIPlugin]s from this inspector.
 * Available in logs-ui so logs-core stays Compose-free.
 */
public val AELogs.uiPlugins: List<UIPlugin>
    get() = plugins.value.filterIsInstance<UIPlugin>()
