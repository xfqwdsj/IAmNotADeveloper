package top.ltfan.notdeveloper.datastore

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.parcelableCreator
import kotlinx.serialization.Serializable
import top.ltfan.m3x.datastore.annotation.Store

@Parcelize
@Serializable
@Store(codec = AppCodec::class)
data class GlobalPreferences(
    val useGlobalPreferences: Boolean = false,
) : Parcelable {
    companion object {
        val CREATOR = parcelableCreator<GlobalPreferences>()
    }
}
