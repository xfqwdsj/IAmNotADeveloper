package top.ltfan.notdeveloper.ui.page

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavEntry
import kotlinx.serialization.Serializable
import top.ltfan.notdeveloper.R
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

@Serializable
sealed class Page {
    open val appBarLabel: Int? = null

    @Composable
    context(contentPadding: PaddingValues)
    abstract fun AppViewModel.Content()

    fun navEntry(viewModel: AppViewModel, contentPadding: PaddingValues) = NavEntry(this) {
        context(contentPadding) {
            viewModel.Content()
        }
    }
}

sealed class Main : Page() {
    override val appBarLabel = R.string.app_name
    abstract val navigationLabel: Int
    abstract val navigationIcon: ImageVector

    companion object {
        val pages by lazy { listOf(Overview) }
    }
}
