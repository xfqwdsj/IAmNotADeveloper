package top.ltfan.notdeveloper.datastore

import android.content.Context
import androidx.datastore.core.FileStorage
import androidx.datastore.core.Serializer
import androidx.datastore.core.Storage
import androidx.datastore.dataStoreFile
import top.ltfan.material.m3.datastore.StoreCodec
import top.ltfan.material.m3.datastore.StoreFile
import java.io.InputStream
import java.io.OutputStream

/**
 * The Android file backend: stores every model under the standard
 * `filesDir/datastore/<name>` path via `FileStorage`.
 */
class AppFile(private val context: Context) : StoreFile {
    override fun <T> create(name: String, defaultValue: T, codec: StoreCodec<T>): Storage<T> =
        FileStorage(
            serializer = codec.asDataStoreSerializer(defaultValue),
            produceFile = { context.dataStoreFile(name) },
        )
}

private fun <T> StoreCodec<T>.asDataStoreSerializer(default: T) = object : Serializer<T> {
    override val defaultValue: T get() = default

    override suspend fun readFrom(input: InputStream): T {
        val bytes = input.readBytes()
        return if (bytes.isEmpty()) default else decode(bytes)
    }

    // FileStorage runs serializer calls on the store scope (Dispatchers.IO in
    // NotDevApplication), so the blocking write is intended.
    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun writeTo(t: T, output: OutputStream) {
        output.write(encode(t))
    }
}
