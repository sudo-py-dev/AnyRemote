package com.anyremote.remote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anyremote.remote.R
import com.anyremote.remote.data.RemoteDatabase.Brand
import com.anyremote.remote.data.RemoteDatabase.DeviceType
import com.anyremote.remote.service.FavoriteManager
import com.anyremote.remote.ui.helpers.*
import com.anyremote.remote.ui.theme.*
import com.anyremote.remote.ui.theme.ElectricBlue
import kotlinx.coroutines.launch

@Composable
fun BrandSelectionPanel(
    device: DeviceType,
    onBrandClick: (Brand) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLetter by remember { mutableStateOf<Char?>(null) }

    val filteredBrands =
        remember(searchQuery, selectedLetter, device.brands) {
            val filtered =
                device.brands.filter { brand ->
                    val matchesSearch =
                        if (searchQuery.isEmpty()) {
                            true
                        } else {
                            brand.nameEn.contains(searchQuery, ignoreCase = true)
                        }

                    val letter = selectedLetter
                    val matchesLetter =
                        if (letter == null || searchQuery.isNotEmpty()) {
                            true
                        } else {
                            brand.nameEn.startsWith(letter, ignoreCase = true)
                        }

                    matchesSearch && matchesLetter
                }

            if (searchQuery.isEmpty()) {
                filtered.sortedBy { it.nameEn }
            } else {
                filtered.sortedWith(
                    compareBy<Brand> { brand ->
                            if (brand.nameEn.startsWith(searchQuery, ignoreCase = true)) 0 else 1
                        }
                        .thenBy { brand -> if (brand.category == "popular") 0 else 1 }
                        .thenBy { brand -> brand.nameEn.lowercase() })
            }
        }

    val alphabet =
        remember(device.brands) {
            ('A'..'Z').filter { ch ->
                device.brands.any { it.nameEn.startsWith(ch, ignoreCase = true) }
            }
        }

    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
    ) {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    stringResource(R.string.search_placeholder),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            modifier =
                Modifier.fillMaxWidth()
                    .padding(top = 12.dp, bottom = 12.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp)),
            colors =
                TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                ),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = ElectricBlue)
            },
        )

        val groupedBrands =
            remember(filteredBrands) { filteredBrands.groupBy { it.nameEn.take(1).uppercase() } }

        val headerIndices =
            remember(groupedBrands) {
                val indices = mutableMapOf<String, Int>()
                var currentIndex = 0
                groupedBrands.forEach { (initial, brandsInGroup) ->
                    indices[initial] = currentIndex
                    currentIndex += 1
                    currentIndex += brandsInGroup.size
                }
                indices
            }

        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)) {
                        if (filteredBrands.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = stringResource(R.string.no_brands_found),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        } else {
                            groupedBrands.forEach { (initial, brandsInGroup) ->
                                item(key = "header_$initial") {
                                    Box(
                                        modifier =
                                            Modifier.fillMaxWidth()
                                                .padding(top = 12.dp, bottom = 4.dp),
                                    ) {
                                        Text(
                                            text = initial,
                                            color = ElectricBlue,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                        )
                                    }
                                }

                                items(brandsInGroup) { brand ->
                                    BrandListItem(
                                        brand = brand,
                                        onClick = { onBrandClick(brand) },
                                    )
                                }
                            }
                        }
                    }

                if (filteredBrands.isNotEmpty() && searchQuery.isEmpty()) {
                    Column(
                        modifier = Modifier.width(28.dp).fillMaxHeight().padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                            alphabet.forEach { ch ->
                                val isSelected = selectedLetter == ch
                                Box(
                                    modifier =
                                        Modifier.size(22.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) ElectricBlue else Color.Transparent)
                                            .clickable {
                                                if (FavoriteManager.isVibrationEnabled(context)) {
                                                    haptic.performHapticFeedback(
                                                        HapticFeedbackType.LongPress)
                                                }
                                                selectedLetter = if (isSelected) null else ch
                                                val headerIndex = headerIndices[ch.toString()]
                                                if (headerIndex != null) {
                                                    coroutineScope.launch {
                                                        listState.animateScrollToItem(headerIndex)
                                                    }
                                                }
                                            },
                                    contentAlignment = Alignment.Center) {
                                        Text(
                                            text = ch.toString(),
                                            color =
                                                if (isSelected) Color.White
                                                else
                                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                        alpha = 0.8f),
                                            fontWeight =
                                                if (isSelected) FontWeight.Bold
                                                else FontWeight.SemiBold,
                                            fontSize = 10.sp,
                                        )
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
    Box(
        modifier =
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = brand.nameEn,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp))
            }
    }
}
