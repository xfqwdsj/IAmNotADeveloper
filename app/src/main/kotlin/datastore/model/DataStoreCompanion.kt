package top.ltfan.notdeveloper.datastore.model

import kotlinx.serialization.KSerializer
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

interface DataStoreCompanion<T> {
    val fileName: String

    val default: T

    fun serializer(): KSerializer<T>

    context(viewModel: AppViewModel)
    fun createDataStore() = AppDataStore(
        viewModel = viewModel,
        fileName = fileName,
        defaultValue = default,
        serializer = serializer(),
    )
}
