package top.ltfan.notdeveloper.ui.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyScopeMarker
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import top.ltfan.dslutilities.LockableValueDsl
import top.ltfan.notdeveloper.log.Log
import top.ltfan.notdeveloper.ui.util.bottom
import top.ltfan.notdeveloper.ui.util.end
import top.ltfan.notdeveloper.ui.util.start
import top.ltfan.notdeveloper.ui.util.top

@Composable
fun GroupedLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    stickyHeadersContentPaddingOffset: Boolean = false,
    reverseLayout: Boolean = false,
    spacing: Dp = 0.dp,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    overscrollEffect: OverscrollEffect? = rememberOverscrollEffect(),
    content: GroupedLazyListScope.() -> Unit,
) {
    val stickyOffsetWithPadding = rememberStickyOffset(
        isVertical = true,
        reverse = reverseLayout,
        state = state,
        contentPadding = contentPadding,
        stickyHeadersContentPaddingOffset = stickyHeadersContentPaddingOffset,
    )

    val stickyOffset = remember { derivedStateOf { stickyOffsetWithPadding.value.first } }
    val contentPadding by remember { derivedStateOf { stickyOffsetWithPadding.value.second } }

    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
        overscrollEffect = overscrollEffect,
    ) {
        with(
            GroupedLazyListScope(
                stickyOffset = stickyOffset,
                isVertical = true,
                reverse = reverseLayout,
                spacing = spacing,
            ).apply(content)
        ) {
            this@LazyColumn.build()
        }
    }
}

@Composable
fun GroupedLazyRow(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    stickyHeadersContentPaddingOffset: Boolean = false,
    reverseLayout: Boolean = false,
    spacing: Dp = 0.dp,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    overscrollEffect: OverscrollEffect? = rememberOverscrollEffect(),
    content: GroupedLazyListScope.() -> Unit,
) {
    val stickyOffsetWithPadding = rememberStickyOffset(
        isVertical = false,
        reverse = reverseLayout,
        state = state,
        contentPadding = contentPadding,
        stickyHeadersContentPaddingOffset = stickyHeadersContentPaddingOffset,
    )

    val stickyOffset = remember { derivedStateOf { stickyOffsetWithPadding.value.first } }
    val contentPadding by remember { derivedStateOf { stickyOffsetWithPadding.value.second } }

    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
        overscrollEffect = overscrollEffect,
    ) {
        with(
            GroupedLazyListScope(
                stickyOffset = stickyOffset,
                isVertical = false,
                reverse = reverseLayout,
                spacing = spacing,
            ).apply(content)
        ) {
            this@LazyColumn.build()
        }
    }
}

@Composable
fun rememberStickyOffset(
    isVertical: Boolean,
    reverse: Boolean,
    state: LazyListState,
    contentPadding: PaddingValues,
    stickyHeadersContentPaddingOffset: Boolean,
): State<Pair<DpOffset, PaddingValues>> {
    val start = contentPadding.start
    val top = contentPadding.top
    val end = contentPadding.end
    val bottom = contentPadding.bottom
    val density = LocalDensity.current

    return remember {
        derivedStateOf {
            if (!stickyHeadersContentPaddingOffset) return@derivedStateOf DpOffset.Zero to contentPadding

            var newStart = start
            var newTop = top
            var newEnd = end
            var newBottom = bottom

            val target = when (isVertical) {
                true -> when (reverse) {
                    false -> top.also { newTop = 0.dp }
                    true -> bottom.also { newBottom = 0.dp }
                }

                false -> when (reverse) {
                    false -> start.also { newStart = 0.dp }
                    true -> end.also { newEnd = 0.dp }
                }
            }

            val offset = with(density) {
                state.layoutInfo.visibleItemsInfo
                    .firstOrNull { it.contentType is StickyHeaderContent }
                    ?.offset?.toDp()
            }

            val result = if (offset == null || offset >= target) {
                0.dp
            } else {
                target - offset
            }

            if (isVertical) {
                DpOffset(0.dp, result)
            } else {
                DpOffset(result, 0.dp)
            } to PaddingValues(
                start = newStart,
                top = newTop,
                end = newEnd,
                bottom = newBottom,
            )
        }
    }
}

@LazyScopeMarker
class GroupedLazyListScope(
    val stickyOffset: State<DpOffset>,
    val isVertical: Boolean,
    val reverse: Boolean,
    val spacing: Dp,
) : LockableValueDsl() {
    private val groups by list<LazyGroup>()

    fun group(group: LazyGroup) {
        groups += group
    }

    inline fun group(crossinline content: LazyListGroupScope.() -> Unit) {
        group(LazyGroup(content))
    }

    inline fun <T> groups(items: List<T>, crossinline itemContent: LazyListGroupScope.(T) -> Unit) {
        items.forEach { item ->
            group {
                itemContent(item)
            }
        }
    }

    fun LazyListScope.build() {
        val scope = this@GroupedLazyListScope
        scope.lock()
        with(LazyListGroupScope(this, this@GroupedLazyListScope.stickyOffset)) {
            val groups = scope.groups.toList()
            groups.forEachIndexed { index, group ->
                if (index > 0) {
                    val hash = group.hashCode()
                    stickyHeader("spacer$hash", "spacer") {
                        Spacer(Modifier.run {
                            if (scope.isVertical) {
                                height(scope.spacing)
                            } else {
                                width(scope.spacing)
                            }
                        })
                    }
                }

                with(group) {
                    build()
                }
            }
        }
    }
}

@LazyScopeMarker
class LazyListGroupScope(
    private val scope: LazyListScope,
    private val stickyOffset: State<DpOffset>,
) : LazyListScope by scope {
    override fun stickyHeader(
        key: Any?, contentType: Any?, content: @Composable LazyItemScope.(Int) -> Unit
    ) {
        scope.stickyHeader(key, StickyHeaderContent(contentType)) { index ->
            val offset by this@LazyListGroupScope.stickyOffset
            val isVertical = offset.y != 0.dp
            val content = @Composable {
                Spacer(
                    if (isVertical) {
                        Modifier.height(offset.y)
                    } else {
                        Modifier.width(offset.x)
                    }
                )
                content(index)
            }

            if (isVertical) {
                Column { content() }
            } else {
                Row { content() }
            }
        }
    }
}

private data class StickyHeaderContent(val contentType: Any?)

data class LazyItem(
    val key: Any? = null,
    val contentType: Any? = null,
    val modifier: Modifier = Modifier,
    val sticky: Boolean = false,
    val content: @Composable LazyItemScope.() -> Unit,
)

interface LazyGroup {
    fun LazyListGroupScope.build()
}

inline fun LazyGroup(crossinline content: LazyListGroupScope.() -> Unit) = object : LazyGroup {
    override fun LazyListGroupScope.build() {
        content()
    }
}

@LazyScopeMarker
class CardLazyGroup(
    val useStickyHeader: Boolean,
    private val isVertical: Boolean,
    private val reverse: Boolean,
    private val shape: Shape?,
    private val colors: CardColors?,
    private val elevation: CardElevation?,
    private val border: BorderStroke?,
) : LockableValueDsl(), LazyGroup {
    private val items by list<LazyItem>()

    fun item(
        key: Any? = null,
        contentType: Any? = null,
        modifier: Modifier = Modifier,
        content: @Composable LazyItemScope.() -> Unit,
    ) {
        items += LazyItem(key, contentType, modifier, false, content)
    }

    inline fun <T> items(
        items: List<T>,
        key: (item: T) -> Any? = { null },
        contentType: (item: T) -> Any? = { null },
        modifier: (item: T) -> Modifier = { Modifier },
        crossinline itemContent: @Composable LazyItemScope.(T) -> Unit,
    ) {
        items.forEach { item ->
            item(key(item), contentType(item), modifier(item)) {
                itemContent(item)
            }
        }
    }

    fun stickyHeader(
        key: Any? = null,
        contentType: Any? = null,
        modifier: Modifier = Modifier,
        content: @Composable LazyItemScope.() -> Unit,
    ) {
        items += LazyItem(key, contentType, modifier, true, content)
    }

    override fun LazyListGroupScope.build() {
        val scope = this@CardLazyGroup
        scope.lock()
        val items = scope.items.toList()
        val hash = items.hashCode()
        if (!scope.useStickyHeader) {
            stickyHeader("reset$hash", "reset") {}
        }
        items.forEachIndexed { index, (key, contentType, modifier, sticky, content) ->
            val itemFunction: (Any?, Any?, @Composable LazyItemScope.() -> Unit) -> Unit =
                if (
                    sticky || (scope.useStickyHeader && (index == 0 || index == items.lastIndex))
                ) { key, contentType, content ->
                    stickyHeader(key, contentType) { content() }
                } else {
                    ::item
                }

            itemFunction(key, contentType) {
                val shape = when (index) {
                    0 -> scope.getShape(false)
                    items.lastIndex -> scope.getShape(true)
                    else -> RectangleShape
                }

                Card(
                    modifier = modifier,
                    shape = shape,
                    colors = scope.colors ?: CardDefaults.cardColors(),
                    elevation = scope.elevation ?: CardDefaults.cardElevation(),
                    border = scope.border,
                ) { content() }
            }
        }
    }

    @Composable
    private fun getShape(footer: Boolean): Shape {
        val reverse = if (footer) !reverse else reverse
        val shape = shape ?: CardDefaults.shape
        return object : Shape {
            override fun createOutline(
                size: Size, layoutDirection: LayoutDirection, density: Density
            ): Outline {
                val originalOutline = shape.createOutline(
                    size.copy(
                        width = if (isVertical) size.width else size.width * 2,
                        height = if (isVertical) size.height * 2 else size.height,
                    ),
                    layoutDirection,
                    density,
                )
                return when (originalOutline) {
                    is Outline.Rounded -> when (isVertical) {
                        true -> when (reverse) {
                            false -> Outline.Rounded(
                                RoundRect(
                                    rect = size.toRect(),
                                    topLeft = originalOutline.roundRect.topLeftCornerRadius,
                                    topRight = originalOutline.roundRect.topRightCornerRadius,
                                )
                            )

                            true -> Outline.Rounded(
                                RoundRect(
                                    rect = size.toRect(),
                                    bottomRight = originalOutline.roundRect.bottomRightCornerRadius,
                                    bottomLeft = originalOutline.roundRect.bottomLeftCornerRadius,
                                )
                            )
                        }

                        false -> Outline.Rounded(
                            when {
                                layoutDirection == LayoutDirection.Ltr && !reverse ||
                                        layoutDirection == LayoutDirection.Rtl && reverse -> {
                                    RoundRect(
                                        rect = size.toRect(),
                                        topLeft = originalOutline.roundRect.topLeftCornerRadius,
                                        bottomLeft = originalOutline.roundRect.bottomLeftCornerRadius,
                                    )
                                }

                                else -> {
                                    RoundRect(
                                        rect = size.toRect(),
                                        topRight = originalOutline.roundRect.topRightCornerRadius,
                                        bottomRight = originalOutline.roundRect.bottomRightCornerRadius,
                                    )
                                }
                            }
                        )
                    }

                    is Outline.Rectangle -> originalOutline
                    is Outline.Generic -> Outline.Rectangle(originalOutline.bounds)
                }
            }
        }
    }
}

inline fun GroupedLazyListScope.card(
    useStickyHeader: Boolean = false,
    shape: Shape? = null,
    colors: CardColors? = null,
    elevation: CardElevation? = null,
    border: BorderStroke? = null,
    crossinline content: CardLazyGroup.() -> Unit,
) {
    group(
        CardLazyGroup(
            useStickyHeader = useStickyHeader,
            isVertical = isVertical,
            reverse = reverse,
            shape = shape,
            colors = colors,
            elevation = elevation,
            border = border,
        ).apply(content)
    )
}
