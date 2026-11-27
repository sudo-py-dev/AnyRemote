package com.anyremote.remote.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.anyremote.remote.R
import com.anyremote.remote.data.searchBrands
import com.anyremote.remote.data.MAX_BRAND_QUERY_LENGTH
import com.anyremote.remote.util.limitCodePoints
import com.anyremote.remote.data.RemoteDatabase.Brand
import com.anyremote.remote.data.RemoteDatabase.DeviceType
import com.anyremote.remote.ui.helpers.rememberVibratingHapticFeedback
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BrandSelectionPanel(
    device: DeviceType,
    onBrandClick: (Brand) -> Unit,
) {
    var searchQuery by rememberSaveable(device.id) { mutableStateOf("") }
    val query = searchQuery.trim()

    val filteredBrands = remember(query, device) { device.searchBrands(query) }

    // Group by initial only when browsing; search results are ranked, not alphabetical.
    val groupedBrands =
        remember(filteredBrands, query) {
            if (query.isEmpty()) filteredBrands.groupBy { it.nameEn.take(1).uppercase() } else emptyMap()
        }
    val headerIndices =
        remember(groupedBrands) {
            var index = 0
            groupedBrands.mapValues { (_, brands) -> index.also { index += 1 + brands.size } }
        }

    val haptic = rememberVibratingHapticFeedback()
    val focusManager = LocalFocusManager.current
    fun selectBrand(brand: Brand) {
        focusManager.clearFocus()
        onBrandClick(brand)
    }
    val browseState = rememberLazyListState()
    val searchState = rememberLazyListState()
    val listState = if (query.isEmpty()) browseState else searchState
    LaunchedEffect(query) {
        if (query.isNotEmpty()) searchState.scrollToItem(0)
    }
    val coroutineScope = rememberCoroutineScope()

    // The last section may be too short to reach the top of the viewport.
    val currentLetter by remember(headerIndices) {
        derivedStateOf {
            when {
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
                    items(filteredBrands) { brand ->
                        BrandListItem(brand = brand, onClick = { selectBrand(brand) })
                    }
                } else {
                    groupedBrands.forEach { (initial, brandsInGroup) ->
                        stickyHeader(key = "header_$initial") {
                            Text(
                                text = initial,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier =
                                    Modifier.fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
                            )
                        }
                        items(brandsInGroup) { brand ->
                            BrandListItem(brand = brand, onClick = { selectBrand(brand) })
                        }
                    }
                }
            }

            if (query.isEmpty() && groupedBrands.size > 1) {
                val letters = groupedBrands.keys.toList()
                val railState = rememberLazyListState()
                LaunchedEffect(currentLetter) {
                    val index = letters.indexOf(currentLetter)
                    val layout = railState.layoutInfo
                    val fullyVisible = layout.visibleItemsInfo.any {
                        it.index == index && it.offset >= layout.viewportStartOffset &&
                            it.offset + it.size <= layout.viewportEndOffset
                    }
                    if (index >= 0 && !fullyVisible) railState.scrollToItem(index)
                }

                LazyColumn(
                    state = railState,
                    modifier = Modifier.width(44.dp).fillMaxHeight().padding(end = 4.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    items(letters, key = { it }) { letter ->
                        val isCurrent = letter == currentLetter
                        Box(
                            modifier = Modifier.fillMaxWidth().height(32.dp)
                                .clip(CircleShape)
                                .semantics { selected = isCurrent }
                                .clickable(role = Role.Button) {
                                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    coroutineScope.launch { listState.scrollToItem(headerIndices.getValue(letter)) }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier.size(26.dp).background(
                                    if (isCurrent) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    CircleShape,
                                ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = letter,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
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
