package top.ltfan.notdeveloper.ui.util

import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
val TopAppBarColorsTransparent @Composable inline get() =
    TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Transparent,
        scrolledContainerColor = Color.Transparent,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurface,
    )

@OptIn(ExperimentalMaterial3Api::class)
val LargeTopAppBarColorsTransparent @Composable inline get() =
    TopAppBarDefaults.largeTopAppBarColors(
        containerColor = Color.Transparent,
        scrolledContainerColor = Color.Transparent,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurface,
    )

val CardColorsLowest @Composable inline get() =
    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)

val ListItemColorsTransparent @Composable inline get() =
    ListItemDefaults.colors(containerColor = Color.Transparent)
