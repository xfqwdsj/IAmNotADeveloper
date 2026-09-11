package top.ltfan.notdeveloper.ui.page

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.datastore.AppSettingsItem
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

object Settings : Main() {
    override val navigationLabel = R.string.label_nav_settings
    override val navigationIcon = Icons.Default.Settings

    val lazyListState = LazyListState()

    @Composable
    context(contentPadding: PaddingValues)
    override fun AppViewModel.Content() {
        TODO("Not yet implemented")
    }

    @Composable
    context(viewModel: AppViewModel)
    fun SettingsItem(item: AppSettingsItem) {}
}
