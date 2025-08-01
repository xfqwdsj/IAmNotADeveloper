package top.ltfan.notdeveloper.ui.page

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import kotlinx.serialization.Serializable
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

@Serializable
sealed class Page {
    @Composable
    abstract fun AppViewModel.Content()
    
    fun navEntry(viewModel: AppViewModel) = NavEntry(this) {
        viewModel.Content()
    }
}
