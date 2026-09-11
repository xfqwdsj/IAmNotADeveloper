package top.ltfan.notdeveloper.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import top.ltfan.material.m3.core.visual.pageGlass

/**
 * A single overlay hosted by an [OverlayHostState]; [content] is read on every
 * composition so the host always draws the latest lambda.
 */
class OverlayEntry {
    internal var content: @Composable () -> Unit = {}
}

/** Ordered stack of overlays drawn above the host content, in one window. */
class OverlayHostState {
    internal val entries: SnapshotStateList<OverlayEntry> = mutableStateListOf()

    fun add(entry: OverlayEntry) {
        if (!entries.contains(entry)) entries.add(entry)
    }

    fun remove(entry: OverlayEntry) {
        entries.remove(entry)
    }
}

val LocalOverlayHost = staticCompositionLocalOf<OverlayHostState?> { null }

/**
 * Hosts overlay content above [content] inside the same composition and
 * window, in insertion order.
 */
@Composable
fun OverlayHost(state: OverlayHostState, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalOverlayHost provides state) {
        Box {
            content()
            state.entries.toList().forEach { entry ->
                key(entry) { entry.content() }
            }
        }
    }
}

/**
 * In-window dialog: a scrim that dismisses on tap plus centered content.
 * It registers itself with the enclosing [OverlayHost]; without a host it
 * draws directly, which keeps previews usable.
 */
@Composable
fun GlassDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    scrimColor: Color = Color.Black.copy(alpha = 0.3f),
    content: @Composable BoxScope.() -> Unit,
) {
    val host = LocalOverlayHost.current
    val entry = remember { OverlayEntry() }
    entry.content = {
        Box(
            modifier = modifier
                .fillMaxSize()
                .semantics { isTraversalGroup = true },
            contentAlignment = Alignment.Center,
        ) {
            Spacer(
                Modifier
                    .matchParentSize()
                    .background(scrimColor)
                    .pointerInput(Unit) {
                        detectTapGestures { onDismissRequest() }
                    },
            )
            content()
        }
    }

    if (host == null) {
        entry.content()
        return
    }

    DisposableEffect(host) {
        host.add(entry)
        onDispose { host.remove(entry) }
    }
}

/**
 * In-window bottom sheet: a scrim plus a glass panel anchored to the bottom
 * edge, hosted by the enclosing [OverlayHost].
 */
@Composable
fun GlassBottomSheet(
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val host = LocalOverlayHost.current
    val entry = remember { OverlayEntry() }
    entry.content = {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .semantics { isTraversalGroup = true },
        ) {
            Spacer(
                Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .pointerInput(Unit) {
                        detectTapGestures { onDismissRequest() }
                    },
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .pageGlass(
                        shape = MaterialTheme.shapes.extraLarge,
                        containerColor = MaterialTheme.colorScheme.surface,
                    )
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom),
                    )
                    .padding(bottom = 8.dp),
                content = content,
            )
        }
    }

    if (host == null) {
        entry.content()
        return
    }

    DisposableEffect(host) {
        host.add(entry)
        onDispose { host.remove(entry) }
    }
}
