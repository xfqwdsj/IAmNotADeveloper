package top.ltfan.notdeveloper.ui.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
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

    inline fun group(crossinline content: LazyListScope.() -> Unit) {
        group(LazyGroup(content))
    }

    inline fun <T> groups(items: List<T>, crossinline itemContent: LazyListScope.(T) -> Unit) {
        items.forEach { item ->
            group {
                itemContent(item)
            }
        }
    }

    fun LazyListScope.build() {
        val scope = this@GroupedLazyListScope
        scope.lock()
        val groups = scope.groups.toList()
        groups.forEachIndexed { index, group ->
            if (index > 0) {
                val hash = group.hashCode()
                stickyHeader("spacer$hash", "spacer") {
                    Spacer(Modifier.apply {
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

data class LazyItem(
    val key: Any? = null,
    val contentType: Any? = null,
    val modifier: Modifier = Modifier,
    val content: @Composable LazyItemScope.() -> Unit,
)

interface LazyGroup {
    fun LazyListScope.build()
}

inline fun LazyGroup(crossinline content: LazyListScope.() -> Unit) = object : LazyGroup {
    override fun LazyListScope.build() {
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
        items += LazyItem(key, contentType, modifier, content)
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

    override fun LazyListScope.build() {
        val scope = this@CardLazyGroup
        scope.lock()
        val items = scope.items.toList()
        val hash = items.hashCode()
        if (!scope.useStickyHeader) {
            stickyHeader("reset$hash", "reset") {}
        }
        items.forEachIndexed { index, (key, contentType, modifier, content) ->
            val item: (Any?, Any?, @Composable LazyItemScope.() -> Unit) -> Unit =
                if (scope.useStickyHeader && (index == 0 || index == items.lastIndex)) { key, contentType, content ->
                    stickyHeader(key, contentType) { content() }
                } else {
                    ::item
                }

            item(key, contentType) {
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
                                originalOutline.roundRect.copy(
                                    bottomRightCornerRadius = CornerRadius.Zero,
                                    bottomLeftCornerRadius = CornerRadius.Zero,
                                )
                            )

                            true -> Outline.Rounded(
                                originalOutline.roundRect.copy(
                                    topLeftCornerRadius = CornerRadius.Zero,
                                    topRightCornerRadius = CornerRadius.Zero,
                                )
                            )
                        }

                        false -> Outline.Rounded(
                            when {
                                layoutDirection == LayoutDirection.Ltr && !reverse ||
                                        layoutDirection == LayoutDirection.Rtl && reverse -> {
                                    originalOutline.roundRect.copy(
                                        topRightCornerRadius = CornerRadius.Zero,
                                        bottomRightCornerRadius = CornerRadius.Zero,
                                    )
                                }

                                else -> {
                                    originalOutline.roundRect.copy(
                                        topLeftCornerRadius = CornerRadius.Zero,
                                        bottomLeftCornerRadius = CornerRadius.Zero,
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
