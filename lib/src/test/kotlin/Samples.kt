import android.content.Context
import android.widget.RemoteViews
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.extension.KarooExtension
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.ViewConfig

fun dataTypeImplUsage() {
    class EmptyDataType(extension: String) : DataTypeImpl(extension, "empty-datatype") {
        override fun startStream(emitter: Emitter<StreamState>) {
            emitter.onNext(StreamState.Searching)
            emitter.setCancellable {
                println("cleanup")
            }
        }
    }
}

fun visualDataTypeImplUsage(remoteViews: RemoteViews) {
    class EmptyDataType(extension: String) : DataTypeImpl(extension, "empty-visual-datatype") {
        override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
            emitter.onNext(UpdateGraphicConfig(showHeader = false))
            emitter.updateView(remoteViews)
            emitter.setCancellable {
                println("cleanup")
            }
        }
    }
}

fun karooExtensionUsage() {
    class EmptyExtension : KarooExtension("empty-extension", "5.0")
}

fun karooSystemUsage(context: Context) {
    val karooSystem by lazy { KarooSystemService(context) }
    // On lifecycle start
    karooSystem.connect {
        println("karoo system connected")
    }
    // On lifecycle end
    karooSystem.disconnect()
}
