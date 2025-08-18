package top.ltfan.notdeveloper.datastore

import android.content.pm.ApplicationInfo
import android.os.Parcelable
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.data.PackageInfoWrapper
import top.ltfan.notdeveloper.data.UserInfo
import top.ltfan.notdeveloper.datastore.model.DataStoreCompanion
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

@Parcelize
@Serializable
data class AppListSettings(
    val selectedUser: UserInfo = UserInfo.current,
    val sort: AppSort = AppSort.Label,
    val filtered: Set<AppFilter> = emptySet(),
) : Parcelable {
    companion object : DataStoreCompanion<AppListSettings> {
        override val fileName = "app_list_settings.pb"

        override val default = AppListSettings(
            selectedUser = UserInfo.current,
            sort = AppSort.Label,
            filtered = emptySet(),
        )
    }
}

enum class AppSort(@param:StringRes val labelRes: Int) {
    Label(R.string.item_apps_bottom_sheet_filter_sort_label) {
        override operator fun invoke(viewModel: AppViewModel) = compareBy<PackageInfoWrapper> {
            val packageManager = viewModel.application.packageManager
            it.info.applicationInfo!!.loadLabel(packageManager).toString()
        }

        context(viewModel: AppViewModel)
        override fun Collection<PackageInfoWrapper>.sorted() = sortedWith(
            invoke(viewModel) then Package(viewModel) then Updated(viewModel)
        )
    },
    Package(R.string.item_apps_bottom_sheet_filter_sort_package) {
        override operator fun invoke(viewModel: AppViewModel) =
            compareBy<PackageInfoWrapper> { it.info.packageName }

        context(viewModel: AppViewModel)
        override fun Collection<PackageInfoWrapper>.sorted() = sortedWith(
            invoke(viewModel) then Label(viewModel) then Updated(viewModel)
        )
    },
    Updated(R.string.item_apps_bottom_sheet_filter_sort_updated) {
        override operator fun invoke(viewModel: AppViewModel) =
            compareByDescending<PackageInfoWrapper> { it.info.lastUpdateTime }

        context(viewModel: AppViewModel)
        override fun Collection<PackageInfoWrapper>.sorted() = sortedWith(
            invoke(viewModel) then Package(viewModel) then Label(viewModel)
        )
    };

    abstract operator fun invoke(viewModel: AppViewModel): Comparator<PackageInfoWrapper>

    context(viewModel: AppViewModel)
    abstract fun Collection<PackageInfoWrapper>.sorted(): List<PackageInfoWrapper>
}

enum class AppFilter(@param:StringRes val labelRes: Int) {
    All(R.string.item_apps_bottom_sheet_filter_all) {
        context(viewModel: AppViewModel)
        override fun Sequence<PackageInfoWrapper>.filtered(): Sequence<PackageInfoWrapper> =
            emptySequence()
    },
    Configured(R.string.item_apps_bottom_sheet_filter_configured) {
        context(viewModel: AppViewModel)
        override fun Sequence<PackageInfoWrapper>.filtered() =
            error("Configured filter should not be used directly")
    },
    Unconfigured(R.string.item_apps_bottom_sheet_filter_unconfigured) {
        context(viewModel: AppViewModel)
        override fun Sequence<PackageInfoWrapper>.filtered() =
            error("Unconfigured filter should not be used directly")
    },
    System(R.string.item_apps_bottom_sheet_filter_system) {
        context(viewModel: AppViewModel)
        override fun Sequence<PackageInfoWrapper>.filtered() = filter {
            (it.info.applicationInfo?.flags ?: 0) and
                    (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0
        }
    };

    context(viewModel: AppViewModel)
    abstract fun Sequence<PackageInfoWrapper>.filtered(): Sequence<PackageInfoWrapper>

    companion object {
        val toggleableEntries = entries.drop(1).toSet()
        val groupingEntries = setOf(Configured, Unconfigured)
    }
}
