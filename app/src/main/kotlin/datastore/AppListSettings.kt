package top.ltfan.notdeveloper.datastore

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Parcelable
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import top.ltfan.notdeveloper.R
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
        override operator fun invoke(viewModel: AppViewModel) = compareBy<PackageInfo> {
            val packageManager = viewModel.application.packageManager
            val applicationInfo = packageManager.getApplicationInfo(it.packageName, 0)
            applicationInfo.loadLabel(packageManager).toString()
        }

        context(viewModel: AppViewModel)
        override fun List<PackageInfo>.sorted() = sortedWith(
            invoke(viewModel) then Package(viewModel) then Updated(viewModel)
        )
    },
    Package(R.string.item_apps_bottom_sheet_filter_sort_package) {
        override operator fun invoke(viewModel: AppViewModel) =
            compareBy<PackageInfo> { it.packageName }

        context(viewModel: AppViewModel)
        override fun List<PackageInfo>.sorted() = sortedWith(
            invoke(viewModel) then Label(viewModel) then Updated(viewModel)
        )
    },
    Updated(R.string.item_apps_bottom_sheet_filter_sort_updated) {
        override operator fun invoke(viewModel: AppViewModel) =
            compareByDescending<PackageInfo> { it.lastUpdateTime }

        context(viewModel: AppViewModel)
        override fun List<PackageInfo>.sorted() = sortedWith(
            invoke(viewModel) then Package(viewModel) then Label(viewModel)
        )
    };

    abstract operator fun invoke(viewModel: AppViewModel): Comparator<PackageInfo>

    context(viewModel: AppViewModel)
    abstract fun List<PackageInfo>.sorted(): List<PackageInfo>
}

enum class AppFilter(@param:StringRes val labelRes: Int) {
    All(R.string.item_apps_bottom_sheet_filter_all) {
        context(viewModel: AppViewModel)
        override fun List<PackageInfo>.filtered(): List<PackageInfo> = emptyList()
    },
    Configured(R.string.item_apps_bottom_sheet_filter_configured) {
        context(viewModel: AppViewModel)
        override fun List<PackageInfo>.filtered() =
            error("Configured filter should not be used directly")
    },
    Unconfigured(R.string.item_apps_bottom_sheet_filter_unconfigured) {
        context(viewModel: AppViewModel)
        override fun List<PackageInfo>.filtered() =
            error("Unconfigured filter should not be used directly")
    },
    System(R.string.item_apps_bottom_sheet_filter_system) {
        context(viewModel: AppViewModel)
        override fun List<PackageInfo>.filtered() = filter {
            (it.applicationInfo?.flags ?: 0) and
                    (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0
        }
    };

    context(viewModel: AppViewModel)
    abstract fun List<PackageInfo>.filtered(): List<PackageInfo>

    companion object {
        val usableEntries = entries.drop(1).toSet()
    }
}
