package com.ae.logs.plugins.network

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.ae.logs.core.PluginContext
import com.ae.logs.core.UIPlugin
import com.ae.logs.plugins.network.store.NetworkStore
import com.ae.logs.plugins.network.ui.NetworkContent
import com.ae.logs.plugins.network.ui.NetworkViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Plugin for monitoring network requests inside the AELogs panel.
 *
 * ## Installation
 * ```kotlin
 * AELogs.default.install(NetworkPlugin())
 * ```
 *
 * ## Recording from your OkHttp/Ktor interceptor:
 * ```kotlin
 * val api = AELogs.default.getPlugin<NetworkPlugin>()?.api
 * val id = api?.newId() ?: return
 * api.request(id, call.request.url.toString(), NetworkMethod.GET)
 * // ... later ...
 * api.response(id, statusCode = response.code, durationMs = elapsed)
 * ```
 */
public class NetworkPlugin(
    maxEntries: Int = 200,
) : UIPlugin {
    override val id: String = ID
    override val name: String = "Network"
    override val icon: ImageVector = Icons.Default.Wifi

    private val _badgeCount = MutableStateFlow<Int?>(null)
    override val badgeCount: StateFlow<Int?> = _badgeCount

    private val store = NetworkStore(capacity = maxEntries)
    private var viewModel: NetworkViewModel? = null

    /** Public API for recording requests/responses from interceptors. */
    public val api: NetworkApi = NetworkApi(store)

    override fun onAttach(context: PluginContext) {
        viewModel = NetworkViewModel(store, context.scope)
        // Badge tracks live entry count
        context.scope.launch {
            store.entries.collect { entries ->
                _badgeCount.value = entries.size.takeIf { it > 0 }
            }
        }
    }

    override fun onClear() {
        store.clear()
        _badgeCount.value = null
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val vm = viewModel ?: return
        NetworkContent(viewModel = vm, modifier = modifier)
    }

    public companion object {
        public const val ID: String = "ae_logs_network"
    }
}
