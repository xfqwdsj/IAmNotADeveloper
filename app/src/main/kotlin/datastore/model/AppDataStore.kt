package top.ltfan.notdeveloper.datastore.model

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.lifecycle.viewModelScope
import kotlinx.serialization.KSerializer
import top.ltfan.notdeveloper.datastore.serializer.DatastoreSerializer
import top.ltfan.notdeveloper.ui.viewmodel.AppViewModel

class AppDataStore<T>(
    viewModel: AppViewModel,
    fileName: String,
    val defaultValue: T,
    serializer: KSerializer<T>,
) : DataStore<T> by DataStoreFactory.create(
    serializer = DatastoreSerializer(
        defaultValue = defaultValue,
        serializer = serializer,
    ),
    scope = viewModel.viewModelScope,
    produceFile = { viewModel.application.dataStoreFile(fileName) },
)
