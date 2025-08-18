package top.ltfan.notdeveloper.datastore

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.parcelableCreator
import kotlinx.serialization.Serializable
import top.ltfan.notdeveloper.datastore.model.DataStoreCompanion

@Parcelize
@Serializable
data class GlobalPreferences(
    val useGlobalPreferences: Boolean = false,
) : Parcelable {
    companion object : DataStoreCompanion<GlobalPreferences> {
        override val fileName = "global_preferences"
        override val default = GlobalPreferences()
        val CREATOR = parcelableCreator<GlobalPreferences>()
    }
}
