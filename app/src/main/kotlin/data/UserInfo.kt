package top.ltfan.notdeveloper.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.parcelableCreator

@Parcelize
data class UserInfo(
    val id: Int,
    val name: String?,
    val flags: Int,
) : Parcelable {
    companion object {
        @JvmStatic
        val CREATOR = parcelableCreator<UserInfo>()
    }
}
