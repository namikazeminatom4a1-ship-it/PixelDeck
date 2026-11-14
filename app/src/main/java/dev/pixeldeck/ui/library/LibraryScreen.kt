package dev.pixeldeck.ui.library

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.pixeldeck.data.model.Game
import dev.pixeldeck.data.model.GameSystem
import dev.pixeldeck.data.settings.AppSettings
import dev.pixeldeck.data.settings.BackgroundStyle
import dev.pixeldeck.design.component.XmbBackground
import dev.pixeldeck.design.component.XmbBackgroundStyle
import dev.pixeldeck.ui.common.formatPlayTime
import dev.pixeldeck.ui.components.GameArtwork
import dev.pixeldeck.ui.theme.palette
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class XmbCategory(
    val id: String,
    val label: String,
    val system: GameSystem? = null,
    val icon: ImageVector? = null,
)

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    settings: AppSettings,
    onGameClick: (Long) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var searchOpen by rememberSaveable { mutableStateOf(false) }

    val palette = settings.palette()
    val accent = palette.accent
    val secondary = palette.secondary

    val backgroundStyle = when (settings.backgroundStyle) {
        BackgroundStyle.WAVES -> XmbBackgroundStyle.WAVES
        BackgroundStyle.AURORA -> XmbBackgroundStyle.AURORA
        BackgroundStyle.SOLID -> XmbBackgroundStyle.SOLID
    }

    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        if (uri != null) viewModel.scanFolder(uri)
    }

    LaunchedEffect(state.message) {
        state.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeMessage()
        }
    }

    val categories = remember(state.systems) {
        buildList {
            add(XmbCategory(LibraryCategories.ALL, "All", icon = Icons.Rounded.Apps))
            add(XmbCategory(LibraryCategories.RECENT, "Recent", icon = Icons.Rounded.History))
            add(XmbCategory(LibraryCategories.FAVORITES, "Favorites", icon = Icons.Rounded.Favorite))
            state.systems.forEach { system ->
                add(
                    XmbCategory(
                        id = LibraryCategories.system(system.id),
                        label = system.shortName,
                        system = system,
                    ),
                )
            }
        }
    }

    val selectedIndex = categories.indexOfFirst { it.id == state.selectedCategory }
        .coerceAtLeast(0)
    val categoryListState = rememberLazyListState()
    LaunchedEffect(selectedIndex, categories.size) {
        categoryListState.animateScrollToItem(selectedIndex)
    }

    val gameListState = rememberLazyListState()
    val focusedIndex by remember {
        derivedStateOf { gameListState.firstVisibleItemIndex }
    }
    LaunchedEffect(state.selectedCategory) {
        gameListState.scrollToItem(0)
    }

    val clockFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val clock by produceState(initialValue = clockFormatter.format(Date())) {
        while (true) {
            value = clockFormatter.format(Date())
            delay(15_000)
        }
    }

    XmbBackground(
        modifier = Modifier.fillMaxSize(),
        accent = accent,
        secondary = secondary,
        style = backgroundStyle,
        intensity = settings.waveIntensity,
        speed = settings.waveSpeed,
        glow = settings.glowEnabled,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding(),
        ) {
            XmbTopBar(
                accent = accent,
                clock = if (settings.showClock) clock else null,
                isScanning = state.isScanning,
                searchOpen = searchOpen,
                onSearch = {
                    if (searchOpen) {
                        searchOpen = false
                        viewModel.setQuery("")
                    } else {
                        searchOpen = true
                    }
                },
                onAddFolder = { folderPicker.launch(null) },
                onRescan = viewModel::rescanAll,
                onOpenSettings = onOpenSettings,
            )

            AnimatedVisibility(
                visible = searchOpen,
                enter = fadeIn() + slideInVertically { -it / 2 },
                exit = fadeOut() + slideOutVertically { -it / 2 },
            ) {
                XmbSearchField(
                    query = state.query,
                    accent = accent,
                    onQueryChange = viewModel::setQuery,
                    onClose = {
                        searchOpen = false
                        viewModel.setQuery("")
                    },
                )
            }

            Spacer(Modifier.height(if (settings.showCategoryLabels) 6.dp else 14.dp))

            LazyRow(
                state = categoryListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (settings.showCategoryLabels) 116.dp else 86.dp),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(if (settings.showCategoryLabels) 16.dp else 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items(categories, key = { it.id }) { category ->
                    XmbCategoryItem(
                        category = category,
                        selected = category.id == state.selectedCategory,
                        accent = accent,
                        showLabel = settings.showCategoryLabels,
                        glow = settings.glowEnabled,
                        onClick = { viewModel.selectCategory(category.id) },
                    )
                }
            }

            XmbSectionHeader(
                title = categories.firstOrNull { it.id == state.selectedCategory }?.label ?: "All",
                count = state.games.size,
                accent = accent,
            )

            when {
                state.isEmpty -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    XmbEmptyLibrary(
                        accent = accent,
                        onAddFolder = { folderPicker.launch(null) },
                    )
                }

                state.isFilteredEmpty -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    XmbEmptyCategory(
                        query = state.query,
                        accent = accent,
                        onClear = {
                            viewModel.setQuery("")
                            viewModel.selectCategory(LibraryCategories.ALL)
                        },
                    )
                }

                else -> LazyColumn(
                    state = gameListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    flingBehavior = rememberSnapFlingBehavior(gameListState, SnapPosition.Start),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(if (settings.compactList) 6.dp else 10.dp),
                ) {
                    itemsIndexed(state.games, key = { _, game -> game.id }) { index, game ->
                        XmbGameRow(
                            game = game,
                            focused = index == focusedIndex,
                            compact = settings.compactList,
                            accent = accent,
                            onClick = { onGameClick(game.id) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun XmbTopBar(
    accent: Color,
    clock: String?,
    isScanning: Boolean,
    searchOpen: Boolean,
    onSearch: () -> Unit,
    onAddFolder: () -> Unit,
    onRescan: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 12.dp, top = 10.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "PIXELDECK",
                color = accent,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 5.sp,
            )
            Text(
                text = "XROSS MEDIA BAR",
                color = Color.White.copy(alpha = 0.42f),
                fontSize = 9.sp,
                letterSpacing = 2.sp,
            )
        }

        Spacer(Modifier.weight(1f))

        if (clock != null) {
            Text(
                text = clock,
                color = Color.White.copy(alpha = 0.75f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.width(6.dp))
        }

        XmbIconButton(
            icon = if (searchOpen) Icons.Rounded.Close else Icons.Rounded.Search,
            contentDescription = if (searchOpen) "Close search" else "Search",
            tint = if (searchOpen) accent else Color.White.copy(alpha = 0.72f),
            onClick = onSearch,
        )
        XmbIconButton(
            icon = Icons.Rounded.FolderOpen,
            contentDescription = "Add folder",
            tint = Color.White.copy(alpha = 0.72f),
            onClick = onAddFolder,
        )
        if (isScanning) {
            Box(
                modifier = Modifier.size(38.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(17.dp),
                    color = accent,
                    strokeWidth = 2.dp,
                )
            }
        } else {
            XmbIconButton(
                icon = Icons.Rounded.Refresh,
                contentDescription = "Rescan folders",
                tint = Color.White.copy(alpha = 0.72f),
                onClick = onRescan,
            )
        }
        XmbIconButton(
            icon = Icons.Rounded.Settings,
            contentDescription = "Settings",
            tint = Color.White.copy(alpha = 0.72f),
            onClick = onOpenSettings,
        )
    }
}

@Composable
private fun XmbIconButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 20.dp),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun XmbSearchField(
    query: String,
    accent: Color,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        placeholder = {
            Text("Search your library", color = Color.White.copy(alpha = 0.38f))
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = accent.copy(alpha = 0.8f),
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                XmbIconButton(
                    icon = Icons.Rounded.Close,
                    contentDescription = "Clear",
                    tint = Color.White.copy(alpha = 0.6f),
                    onClick = { onQueryChange("") },
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White.copy(alpha = 0.9f),
            cursorColor = accent,
            focusedBorderColor = accent.copy(alpha = 0.75f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
            focusedContainerColor = Color.Black.copy(alpha = 0.32f),
            unfocusedContainerColor = Color.Black.copy(alpha = 0.26f),
        ),
    )
}

@Composable
private fun XmbCategoryItem(
    category: XmbCategory,
    selected: Boolean,
    accent: Color,
    showLabel: Boolean,
    glow: Boolean,
    onClick: () -> Unit,
) {
    val iconSize by animateDpAsState(
        targetValue = if (selected) 66.dp else 48.dp,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 480f),
        label = "category-size",
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "category-scale",
    )
    val labelAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.55f,
        label = "category-label",
    )
    val shape = RoundedCornerShape(if (selected) 20.dp else 16.dp)
    val iconTint by animateColorAsState(
        targetValue = if (selected) accent else Color.White.copy(alpha = 0.72f),
        label = "category-tint",
    )

    Column(
        modifier = Modifier
            .width(if (showLabel) 84.dp else 68.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 40.dp),
                onClick = onClick,
            )
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .shadow(
                    elevation = if (selected && glow) 18.dp else 0.dp,
                    shape = shape,
                    clip = false,
                    ambientColor = accent,
                    spotColor = accent,
                )
                .clip(shape)
                .background(
                    if (selected) {
                        Brush.linearGradient(
                            listOf(accent.copy(alpha = 0.26f), accent.copy(alpha = 0.08f)),
                        )
                    } else {
                        Brush.linearGradient(
                            listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.03f)),
                        )
                    },
                )
                .border(
                    width = 1.dp,
                    color = if (selected) accent.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.08f),
                    shape = shape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (category.system != null) {
                Text(
                    text = category.system.shortName,
                    color = iconTint,
                    fontSize = if (selected) 18.sp else 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                )
            } else {
                Icon(
                    imageVector = category.icon ?: Icons.Rounded.SportsEsports,
                    contentDescription = category.label,
                    tint = iconTint,
                    modifier = Modifier.size(if (selected) 30.dp else 22.dp),
                )
            }
        }

        if (showLabel) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = category.label,
                color = if (selected) accent else Color.White.copy(alpha = labelAlpha),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun XmbSectionHeader(
    title: String,
    count: Int,
    accent: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title.uppercase(),
            color = accent,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(accent.copy(alpha = 0.4f), Color.Transparent),
                    ),
                ),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = count.toString(),
            color = Color.White.copy(alpha = 0.55f),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun XmbGameRow(
    game: Game,
    focused: Boolean,
    compact: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(18.dp)
    val scale by animateFloatAsState(
        targetValue = if (focused) 1.02f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
        label = "game-scale",
    )
    val background by animateColorAsState(
        targetValue = if (focused) accent.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.045f),
        label = "game-bg",
    )
    val borderColor by animateColorAsState(
        targetValue = if (focused) accent.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.07f),
        label = "game-border",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(background)
            .border(1.dp, borderColor, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick,
            )
            .padding(
                horizontal = 12.dp,
                vertical = if (compact) 6.dp else 9.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(
                    width = if (compact) 58.dp else 76.dp,
                    height = if (compact) 42.dp else 56.dp,
                )
                .clip(RoundedCornerShape(10.dp)),
        ) {
            GameArtwork(
                game = game,
                modifier = Modifier.fillMaxSize(),
                corner = 10.dp,
                overlayTitle = false,
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = game.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (focused) FontWeight.SemiBold else FontWeight.Medium,
                color = Color.White.copy(alpha = if (focused) 1f else 0.86f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = game.system.shortName,
                    style = MaterialTheme.typography.labelSmall,
                    color = accent.copy(alpha = if (focused) 0.95f else 0.65f),
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "  ·  ",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.3f),
                )
                Text(
                    text = if (game.lastPlayedAt == null) {
                        "Never played"
                    } else {
                        formatPlayTime(game.playTimeMillis)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (game.favorite) {
            Icon(
                imageVector = Icons.Rounded.Favorite,
                contentDescription = "Favorite",
                tint = accent,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(8.dp))
        }

        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = if (focused) 0.6f else 0.26f),
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun XmbEmptyLibrary(
    accent: Color,
    onAddFolder: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.12f))
                .border(1.dp, accent.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.SportsEsports,
                contentDescription = null,
                tint = accent.copy(alpha = 0.8f),
                modifier = Modifier.size(46.dp),
            )
        }
        Spacer(Modifier.height(22.dp))
        Text(
            text = "Your deck is empty",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Add a folder full of ROMs and PixelDeck will sort everything by system.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(Modifier.height(22.dp))
        Button(
            onClick = onAddFolder,
            colors = ButtonDefaults.buttonColors(
                containerColor = accent,
                contentColor = Color(0xFF0A0A0F),
            ),
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Add games folder", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun XmbEmptyCategory(
    query: String,
    accent: Color,
    onClear: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (query.isBlank()) "Nothing in this category yet." else "No matches for \"$query\".",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.75f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = onClear,
            colors = ButtonDefaults.buttonColors(
                containerColor = accent.copy(alpha = 0.18f),
                contentColor = accent,
            ),
        ) {
            Text("Back to all games")
        }
    }
}
