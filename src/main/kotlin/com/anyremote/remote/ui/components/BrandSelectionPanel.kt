package com.anyremote.remote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

@Composable
fun BrandSelectionPanel(
    device: DeviceType,
    onBrandClick: (Brand) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLetter by remember { mutableStateOf<Char?>(null) }

    val filteredBrands =
        remember(searchQuery, selectedLetter, device.brands) {
            val filtered = device.brands.filter { brand ->
                val matchesSearch =
                    if (searchQuery.isEmpty()) {
                        true
                    } else {
                        brand.nameEn.contains(searchQuery, ignoreCase = true)
                    }

                val matchesLetter =
                    if (selectedLetter == null || searchQuery.isNotEmpty()) {
                        true
                    } else {
                        brand.nameEn.startsWith(selectedLetter!!, ignoreCase = true)
                    }

                matchesSearch && matchesLetter
            }

            if (searchQuery.isEmpty()) {
                filtered.sortedBy { it.nameEn }
            } else {
                filtered.sortedWith(
                    compareBy<Brand> { brand ->
                        // 1. Prioritize brands that start with the query
                        if (brand.nameEn.startsWith(searchQuery, ignoreCase = true)) 0 else 1
                    }.thenBy { brand ->
                        // 2. Prioritize hot category brands
                        if (brand.category.startsWith("hot")) 0 else 1
                    }.thenBy { brand ->
                        // 3. Natural alphabetical order
                        brand.nameEn.lowercase()
                    }
                )
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

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
    ) {
        // Sticky Search Field
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
                    .padding(top = 12.dp, bottom = 4.dp)
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

        // Modern Horizontally Scrollable Alphabet Index Capsules
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // "ALL" Option capsule
            val isAllSelected = selectedLetter == null
            item {
                Box(
                    modifier =
                        Modifier.height(34.dp)
                            .clip(RoundedCornerShape(17.dp))
                            .background(
                                if (isAllSelected) ElectricBlue
                                else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = 1.dp,
                                color =
                                    if (isAllSelected) ElectricBlue
                                    else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(17.dp),
                            )
                            .clickable {
                                if (FavoriteManager.isVibrationEnabled(context)) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                selectedLetter = null
                            }
                            .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.all_caps),
                        color =
                            if (isAllSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                    )
                }
            }

            // A-Z capsules
            items(alphabet) { ch ->
                val isSelected = selectedLetter == ch
                Box(
                    modifier =
                        Modifier.size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) ElectricBlue
                                else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = 1.dp,
                                color =
                                    if (isSelected) ElectricBlue
                                    else MaterialTheme.colorScheme.outline,
                                shape = CircleShape,
                            )
                            .clickable {
                                if (FavoriteManager.isVibrationEnabled(context)) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                selectedLetter = if (isSelected) null else ch
                            },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = ch.toString(),
                        color =
                            if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                    )
                }
            }
        }

        // List Header
        Text(
            text = stringResource(R.string.brands),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Group brands by first letter
        val groupedBrands =
            remember(filteredBrands) { filteredBrands.groupBy { it.nameEn.take(1).uppercase() } }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
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
                    // Styled Letter Section Header like in a Premium Contacts app
                    item(key = "header_$initial") {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
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

            // Bottom spacing
            item { Spacer(modifier = Modifier.height(24.dp)) }
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
                .padding(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier.size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = brand.nameEn.take(1).uppercase(),
                    color = ElectricBlue,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = brand.nameEn,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
        }
    }
}
