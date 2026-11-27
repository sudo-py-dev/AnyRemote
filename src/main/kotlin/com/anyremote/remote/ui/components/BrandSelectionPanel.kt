package com.anyremote.remote.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.anyremote.remote.R
import com.anyremote.remote.data.BrandSearchIndex
import com.anyremote.remote.data.MAX_BRAND_QUERY_LENGTH
import com.anyremote.remote.util.limitCodePoints
import com.anyremote.remote.data.RemoteDatabase.Brand
import com.anyremote.remote.data.RemoteDatabase.DeviceType
import com.anyremote.remote.ui.helpers.rememberVibratingHapticFeedback
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BrandSelectionPanel(
    device: DeviceType,
    onBrandClick: (Brand) -> Unit,
) {
    var searchQuery by rememberSaveable(device.id) { mutableStateOf("") }
    val query = searchQuery.trim()

    val searchIndex = remember(device) { BrandSearchIndex(device) }
    val filteredBrands = remember(searchIndex, query) { searchIndex.search(query) }
    val groupedBrands = remember(searchIndex) {
        searchIndex.alphabetical.groupBy { it.nameEn.take(1).uppercase() }
    }
    val headerIndices = remember(groupedBrands) {
        var next = 0
        groupedBrands.mapValues { (_, brands) -> next.also { next += 1 + brands.size } }
    }

    val focusManager = LocalFocusManager.current
    fun selectBrand(brand: Brand) {
        focusManager.clearFocus()
        onBrandClick(brand)
    }

    val browseState = rememberLazyListState()
    val searchState = rememberLazyListState()
    val listState = if (query.isEmpty()) browseState else searchState
    LaunchedEffect(query) {
        if (query.isNotEmpty()) searchState.requestScrollToItem(0)
    }
    var requestedLetter by remember(device.id) { mutableStateOf<String?>(null) }
    LaunchedEffect(browseState) {
        snapshotFlow { browseState.isScrollInProgress }.collect { scrolling ->
            if (scrolling) requestedLetter = null
        }
    }

    val currentLetter = remember(browseState, headerIndices) {
        derivedStateOf {
            requestedLetter ?: when {
                browseState.canScrollBackward && !browseState.canScrollForward -> headerIndices.keys.lastOrNull()
                else -> headerIndices.entries.lastOrNull { it.value <= browseState.firstVisibleItemIndex }?.key
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it.limitCodePoints(MAX_BRAND_QUERY_LENGTH) },
            placeholder = { Text(stringResource(R.string.search_placeholder)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear_search))
                    }
                }
            },
            singleLine = true,
            shape = CircleShape,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            colors =
                TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        if (filteredBrands.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    Icons.Default.SearchOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp),
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.no_brands_found),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@Column
        }

        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                if (query.isNotEmpty()) {
                    items(filteredBrands, key = { it.id }, contentType = { "brand" }) { brand ->
                        BrandListItem(brand = brand, onClick = { selectBrand(brand) })
                    }
                } else {
                    groupedBrands.forEach { (initial, brandsInGroup) ->
                        stickyHeader(key = "header_$initial", contentType = "brandHeader") {
                            Text(
                                text = initial,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier =
                                    Modifier.fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface)
                                        .semantics { heading() }
                                        .padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
                            )
                        }
                        items(brandsInGroup, key = { it.id }, contentType = { "brand" }) { brand ->
                            BrandListItem(brand = brand, onClick = { selectBrand(brand) })
                        }
                    }
                }
            }

            if (query.isEmpty() && groupedBrands.size > 1) {
                val letters = remember(headerIndices) { headerIndices.keys.toList() }
                AlphabetIndex(
                    letters = letters,
                    currentLetter = currentLetter,
                    modifier = Modifier.padding(end = 4.dp),
                    onLetterSelected = { letter ->
                        requestedLetter = letter
                        browseState.requestScrollToItem(headerIndices.getValue(letter))
                    },
                )
            }
        }
    }
}

@Composable
private fun AlphabetIndex(
    letters: List<String>,
    currentLetter: State<String?>,
    onLetterSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberLazyListState()
    val haptic = rememberVibratingHapticFeedback()
    val indices = remember(letters) { letters.withIndex().associate { it.value to it.index } }

    LaunchedEffect(state, currentLetter, indices) {
        snapshotFlow { state.layoutInfo.visibleItemsInfo.isNotEmpty() }.first { it }
        snapshotFlow { currentLetter.value }.collect { letter ->
            val index = indices[letter] ?: return@collect
            if (state.isScrollInProgress) return@collect

            val layout = state.layoutInfo
            val firstVisible = layout.visibleItemsInfo.firstOrNull() ?: return@collect
            val selected = layout.visibleItemsInfo.firstOrNull { it.index == index }
            if (selected != null &&
                selected.offset >= layout.viewportStartOffset &&
                selected.offset + selected.size <= layout.viewportEndOffset
            ) return@collect

            val height = layout.viewportEndOffset - maxOf(layout.viewportStartOffset, 0)
            val rows = (height / firstVisible.size.coerceAtLeast(1)).coerceAtLeast(1)
            val first = if (index <= firstVisible.index) index else (index - rows + 1).coerceAtLeast(0)
            state.requestScrollToItem(first)
        }
    }

    LazyColumn(
        state = state,
        modifier = modifier.width(48.dp).fillMaxHeight().selectableGroup(),
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        items(letters, key = { it }) { letter ->
            val selected by remember(currentLetter, letter) {
                derivedStateOf { currentLetter.value == letter }
            }
            Box(
                modifier = Modifier.size(48.dp)
                    .clip(CircleShape)
                    .selectable(selected = selected, role = Role.Button) {
                        if (!selected) haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        onLetterSelected(letter)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier.size(34.dp).background(
                        if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        CircleShape,
                    ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = letter,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun BrandListItem(
    brand: Brand,
    onClick: () -> Unit,
) {
    ListItem(
        onClick = onClick,
        leadingContent = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(brand.nameEn.take(1).uppercase(), style = MaterialTheme.typography.titleMedium)
                }
            }
        },
    ) {
        Text(brand.nameEn)
    }
}
