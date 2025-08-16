package top.ltfan.notdeveloper.data

import android.content.Context
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.parcelableCreator
import kotlinx.serialization.Serializable
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

@Serializable
@Parcelize
data class UserInfo(
    val id: Int,
    val name: UserInfoName,
    val flags: Int,
) : Parcelable {
    companion object {
        val CREATOR = parcelableCreator<UserInfo>()

        val current = UserInfo(
            id = -2,
            name = UserInfoName.Current,
            flags = 0,
        )
    }

    constructor(
        id: Int,
        name: String?,
        flags: Int,
    ) : this(
        id = id,
        name = UserInfoName.StringName(name),
        flags = flags,
    )
}

@Serializable
@Parcelize
sealed class UserInfoName : Parcelable {
    @Serializable
    data class StringName(val name: String?) : UserInfoName() {
        override fun getNameBase(context: Context): String = name.toString()
    }

    @Serializable
    data object Current : UserInfoName() {
        override fun getNameBase(context: Context): String =
            context.getString(R.string.label_user_current)
    }

    protected abstract fun getNameBase(context: Context): String

    context(context: Context)
    fun getString() = getNameBase(context)

    context(viewModel: AppViewModel)
    fun getString() = getNameBase(viewModel.application)
}
