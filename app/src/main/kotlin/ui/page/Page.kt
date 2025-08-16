package top.ltfan.notdeveloper.ui.page

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavEntry
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

@Serializable
sealed class Page {
    @Transient
    open val metadata: Map<String, Any> = emptyMap()

    @Composable
    context(contentPadding: PaddingValues)
    abstract fun AppViewModel.Content()

    context(viewModel: AppViewModel)
    fun navEntry(contentPadding: PaddingValues) = NavEntry(
        key = this,
        metadata = metadata,
    ) {
        context(contentPadding) {
            viewModel.Content()
        }
    }
}

sealed class Main : Page() {
    abstract val navigationLabel: Int
    abstract val navigationIcon: ImageVector

    companion object {
        val pages by lazy { listOf(Overview, Apps) }
    }
}
