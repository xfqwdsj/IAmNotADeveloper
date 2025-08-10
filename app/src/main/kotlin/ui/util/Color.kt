package top.ltfan.notdeveloper.ui.util

import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LowestCardColors @Composable inline get() =
    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)

val TransparentListItemColors @Composable inline get() =
    ListItemDefaults.colors(containerColor = Color.Transparent)
