package top.ltfan.notdeveloper.ui.page

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

@Serializable
sealed class Page {
    @Transient
    open val metadata: Map<String, Any> = emptyMap()

    @Composable
    abstract fun AppViewModel.Content()

    context(viewModel: AppViewModel)
    fun navEntry() = NavEntry(
        key = this,
        metadata = metadata,
    ) {
        viewModel.Content()
    }
}

sealed class Main : Page() {
    abstract val navigationLabel: Int
    @get:DrawableRes
    abstract val navigationIcon: Int

    companion object {
        val pages by lazy { listOf(Overview, Apps, Settings) }
    }

    context(viewModel: AppViewModel)
    open fun secondClick() {
    }
}
