package top.ltfan.notdeveloper.datastore

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.cbor.Cbor
import top.ltfan.material.m3.datastore.StoreCodec

/** The app-wide CBOR codec used by every `@Store` model. */
@OptIn(ExperimentalSerializationApi::class)
object AppCodec {
    fun <T : Any> create(kSerializer: KSerializer<T>): StoreCodec<T> = object : StoreCodec<T> {
        private val cbor = Cbor {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

        override fun encode(value: T): ByteArray = cbor.encodeToByteArray(kSerializer, value)

        override fun decode(bytes: ByteArray): T = cbor.decodeFromByteArray(kSerializer, bytes)
    }
}
