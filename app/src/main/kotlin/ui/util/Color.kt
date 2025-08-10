package top.ltfan.notdeveloper.ui.util

import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

val LowestCardColors @Composable inline get() =
    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
