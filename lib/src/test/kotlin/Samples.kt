import android.content.Context
import android.widget.RemoteViews
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.extension.KarooExtension
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.DataPoint
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.ViewConfig
import io.hammerhead.karooext.streamData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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

fun flowDataTypeImplUsage(karooSystem: KarooSystemService) {
    class DoubledSpeed(extension: String) : DataTypeImpl(extension, "doubled-speed") {
        override fun streamFlow(): Flow<StreamState> {
            return karooSystem.streamData(DataType.Type.SPEED).map { state ->
                if (state is StreamState.Streaming) {
                    val speed = state.dataPoint.singleValue ?: 0.0
                    StreamState.Streaming(DataPoint(dataTypeId, mapOf(DataType.Field.SINGLE to speed * 2)))
                } else {
                    state
                }
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
