package com.ae.logs.ui

import androidx.compose.runtime.Composable
import com.ae.logs.PresentationMode
import com.ae.logs.core.UIPlugin
import com.ae.logs.ui.presentation.BottomSheetStrategy
import com.ae.logs.ui.presentation.DialogStrategy
import com.ae.logs.ui.presentation.PresentationStrategy

/**
 * Dispatches to the correct [PresentationStrategy] based on screen size and [PresentationMode].
 *
 * - [PresentationMode.Adaptive] → bottom sheet on compact, dialog on large screens
 * - [PresentationMode.BottomSheet] → always bottom sheet
 * - [PresentationMode.Dialog] → always dialog
 */
@Composable
internal fun AELogsContainer(
    plugins: List<UIPlugin>,
    isLargeScreen: Boolean,
    presentationMode: PresentationMode,
    onDismiss: () -> Unit,
) {
    val strategy: PresentationStrategy =
        when (presentationMode) {
            PresentationMode.BottomSheet -> BottomSheetStrategy
            PresentationMode.Dialog -> DialogStrategy
            PresentationMode.Adaptive -> if (isLargeScreen) DialogStrategy else BottomSheetStrategy
        }

    strategy.Present(onDismiss = onDismiss) {
        AELogsContent(
            plugins = plugins,
            onDismiss = onDismiss,
        )
    }
}
