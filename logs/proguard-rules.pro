-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

# Keep public API
-keep class com.ae.logs.AELogs { *; }
-keep class com.ae.logs.AELogsProviderKt { *; }
-keep class com.ae.logs.core.** { *; }
-keep class com.ae.logs.plugins.logs.model.** { *; }
-keep class com.ae.logs.plugins.logs.store.LogStore { *; }
-keep class com.ae.logs.plugins.logs.LogsPlugin { *; }

# Keep plugin interfaces for consumers
-keep interface com.ae.logs.core.AELogsPlugin { *; }
-keep interface com.ae.logs.core.UIPlugin { *; }
-keep interface com.ae.logs.core.DataPlugin { *; }

# Kotlinx serialization
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keep class kotlinx.serialization.** { *; }
