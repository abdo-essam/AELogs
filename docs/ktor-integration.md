# Ktor Integration

You can inspect Ktor HTTP traffic in AEDevLens by installing a custom `HttpClientPlugin` (Ktor interceptor).

## Setup

```kotlin
class DevLensKtorPlugin : HttpClientPlugin<Unit, Unit> {
    override val key = AttributeKey<Unit>("DevLensPlugin")
    override fun prepare(block: Unit.() -> Unit) = Unit

    override fun install(plugin: Unit, scope: HttpClient) {
        scope.sendPipeline.intercept(HttpSendPipeline.Monitoring) {
            val request = context
            try {
                proceed()
            } finally {
                val response = subject as? HttpClientCall
                AEDevLens.log(
                    tag = "Network",
                    message = "${request.method.value} ${request.url} → ${response?.response?.status}",
                    level = LogLevel.DEBUG,
                )
            }
        }
    }
}
```

## Install the Plugin

```kotlin
val client = HttpClient {
    install(DevLensKtorPlugin())
}
```

All requests and responses will now appear in the AEDevLens log viewer.
