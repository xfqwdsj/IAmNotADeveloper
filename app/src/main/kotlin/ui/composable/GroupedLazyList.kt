package top.ltfan.notdeveloper.ui.composable

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import top.ltfan.dslutilities.LockableValueDsl

@Composable
fun GroupedLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    spacing: Dp = 0.dp,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    overscrollEffect: OverscrollEffect? = rememberOverscrollEffect(),
    content: GroupedLazyListScope.() -> Unit,
) {
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
    reverseLayout: Boolean = false,
    spacing: Dp = 0.dp,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    overscrollEffect: OverscrollEffect? = rememberOverscrollEffect(),
    content: GroupedLazyListScope.() -> Unit,
) {
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
                isVertical = false,
                reverse = reverseLayout,
                spacing = spacing,
            ).apply(content)
        ) {
            this@LazyColumn.build()
        }
    }
}

@LazyScopeMarker
class GroupedLazyListScope(
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

    fun item(
        key: Any? = null,
        contentType: Any? = null,
        content: @Composable LazyItemScope.() -> Unit,
    ) {
        group { item(key, contentType, content) }
    }

    inline fun <T> items(
        items: List<T>,
        crossinline key: (item: T) -> Any? = { null },
        crossinline contentType: (item: T) -> Any? = { null },
        crossinline itemContent: @Composable LazyItemScope.(T) -> Unit,
    ) {
        group { items(items, key, contentType, itemContent) }
    }

    fun LazyListScope.build() {
        val scope = this@GroupedLazyListScope
        scope.lock()
        with(LazyListGroupScope(this)) {
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
) : LazyListScope by scope {
    inline fun <T> items(
        items: List<T>,
        key: (item: T) -> Any? = { null },
        contentType: (item: T) -> Any? = { null },
        crossinline itemContent: @Composable LazyItemScope.(T) -> Unit,
    ) {
        items.forEach { item ->
            item(key(item), contentType(item)) {
                itemContent(item)
            }
        }
    }
}

private data class StickyHeaderContent(val contentType: Any?)

data class LazyItem(
    val key: Any? = null,
    val contentType: Any? = null,
    val modifier: @Composable LazyItemScope.() -> Modifier = { Modifier },
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
    private val shape: @Composable () -> Shape = { CardDefaults.shape },
    private val colors: @Composable () -> CardColors = { CardDefaults.cardColors() },
    private val elevation: @Composable () -> CardElevation = { CardDefaults.cardElevation() },
    private val border: @Composable () -> BorderStroke? = { null },
) : LockableValueDsl(), LazyGroup {
    private val items by list<LazyItem>()

    fun item(
        key: Any? = null,
        contentType: Any? = null,
        modifier: @Composable LazyItemScope.() -> Modifier = { Modifier },
        content: @Composable LazyItemScope.() -> Unit,
    ) {
        items += LazyItem(key, contentType, modifier, false, content)
    }

    fun item(
        key: Any? = null,
        contentType: Any? = null,
        modifier: Modifier = Modifier,
        content: @Composable LazyItemScope.() -> Unit,
    ) {
        item(key, contentType, { modifier }, content)
    }

    fun header(
        key: Any? = null,
        contentType: Any? = null,
        modifier: @Composable LazyItemScope.() -> Modifier = { Modifier },
        sticky: Boolean = false,
        text: @Composable () -> Unit,
    ) {
        items.removeIf { it.contentType is CardHeaderContent }
        items.add(
            index = 0,
            element = LazyItem(
                key = key,
                contentType = CardHeaderContent(contentType ?: "card-header"),
                modifier = { modifier().fillMaxWidth() },
                sticky = sticky,
            ) {
                Column {
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.titleMedium.merge(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        ),
                    ) {
                        Spacer(Modifier.height(16.dp))
                        Box(Modifier.padding(horizontal = 16.dp)) { text() }
                        Spacer(Modifier.height(12.dp))
                    }
                }
            },
        )
    }

    fun header(
        @StringRes text: Int,
        key: Any? = "card-header-${text}",
        contentType: Any? = "card-header",
        modifier: @Composable LazyItemScope.() -> Modifier = { Modifier },
        sticky: Boolean = false,
    ) {
        header(key, contentType, modifier, sticky) {
            Text(stringResource(text))
        }
    }

    fun header(
        @StringRes text: Int,
        key: Any? = "card-header-${text}",
        contentType: Any? = "card-header",
        modifier: Modifier = Modifier,
        sticky: Boolean = false,
    ) {
        header(
            text = text,
            key = key,
            contentType = contentType,
            modifier = { modifier },
            sticky = sticky,
        )
    }

    inline fun <T> items(
        items: List<T>,
        key: (item: T) -> Any? = { null },
        contentType: (item: T) -> Any? = { null },
        crossinline modifier: @Composable LazyItemScope.(item: T) -> Modifier = { Modifier },
        crossinline itemContent: @Composable LazyItemScope.(T) -> Unit,
    ) {
        items.forEach { item ->
            item(key(item), contentType(item), { modifier(item) }) {
                itemContent(item)
            }
        }
    }

    fun stickyHeader(
        key: Any? = null,
        contentType: Any? = null,
        modifier: @Composable LazyItemScope.() -> Modifier = { Modifier },
        content: @Composable LazyItemScope.() -> Unit,
    ) {
        items += LazyItem(key, contentType, modifier, true, content)
    }

    fun stickyHeader(
        key: Any? = null,
        contentType: Any? = null,
        modifier: Modifier = Modifier,
        content: @Composable LazyItemScope.() -> Unit,
    ) {
        stickyHeader(key, contentType, { modifier }, content)
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
                    modifier = modifier(),
                    shape = shape,
                    colors = scope.colors(),
                    elevation = scope.elevation(),
                    border = scope.border(),
                ) { content() }
            }
        }
    }

    @Composable
    private fun getShape(footer: Boolean): Shape {
        val reverse = if (footer) !reverse else reverse
        val shape = shape()
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

private data class CardHeaderContent(val contentType: Any?)

inline fun GroupedLazyListScope.card(
    useStickyHeader: Boolean = false,
    noinline shape: @Composable () -> Shape = { CardDefaults.shape },
    noinline colors: @Composable () -> CardColors = { CardDefaults.cardColors() },
    noinline elevation: @Composable () -> CardElevation = { CardDefaults.cardElevation() },
    noinline border: @Composable () -> BorderStroke? = { null },
    content: CardLazyGroup.() -> Unit,
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

inline fun <T> GroupedLazyListScope.cards(
    groups: List<T>,
    useStickyHeader: (T) -> Boolean = { false },
    crossinline shape: @Composable (T) -> Shape = { CardDefaults.shape },
    crossinline colors: @Composable (T) -> CardColors = { CardDefaults.cardColors() },
    crossinline elevation: @Composable (T) -> CardElevation = { CardDefaults.cardElevation() },
    crossinline border: @Composable (T) -> BorderStroke? = { null },
    content: CardLazyGroup.(T) -> Unit,
) = groups.forEach { group ->
    card(
        useStickyHeader = useStickyHeader(group),
        shape = { shape(group) },
        colors = { colors(group) },
        elevation = { elevation(group) },
        border = { border(group) },
    ) {
        content(group)
    }
}
