package top.ltfan.notdeveloper.datastore.serializer

import androidx.datastore.core.Serializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.cbor.Cbor
import java.io.InputStream
import java.io.OutputStream

class DatastoreSerializer<T>(
    override val defaultValue: T,
    val serializer: KSerializer<T>,
) : Serializer<T> {
    @OptIn(ExperimentalSerializationApi::class)
    private val cbor = Cbor {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun readFrom(input: InputStream): T {
        return try {
            val bytes = input.readBytes()
            if (bytes.isEmpty()) return defaultValue
            cbor.decodeFromByteArray(serializer, bytes)
        } catch (e: SerializationException) {
            defaultValue
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun writeTo(t: T, output: OutputStream) {
        output.write(cbor.encodeToByteArray(serializer, t))
    }
}
