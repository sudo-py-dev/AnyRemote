package com.anyremote.remote.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.anyremote.remote.R
import com.anyremote.remote.data.RemoteDatabase.Brand
import com.anyremote.remote.data.RemoteDatabase.DeviceType
import com.anyremote.remote.service.FavoriteManager
import com.anyremote.remote.ui.helpers.defaultRemoteName

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditRemoteScreen(
    device: DeviceType,
    brand: Brand,
    onDismiss: () -> Unit,
    onSave: (newName: String) -> Unit,
) {
    val context = LocalContext.current
    val currentName =
        remember(device, brand) {
            FavoriteManager.getCustomName(context, device.id, brand.id, defaultRemoteName(context, device, brand))
        }
    var nameInput by remember { mutableStateOf(currentName) }
    val canSave = nameInput.isNotBlank()
    fun save() {
        if (canSave) onSave(nameInput.trim())
    }

    BackHandler { onDismiss() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_remote)) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
                    }
                },
                actions = {
                    TextButton(onClick = ::save, enabled = canSave, modifier = Modifier.padding(end = 8.dp)) {
                        Text(stringResource(R.string.save))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text(stringResource(R.string.remote_name_label)) },
                singleLine = true,
                isError = !canSave,
                keyboardOptions =
                    KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { save() }),
                modifier = Modifier.fillMaxWidth(),
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.quick_rename_section),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val locations =
                    listOf(
                        R.string.location_living_room,
                        R.string.location_bedroom,
                        R.string.location_study_room,
                        R.string.location_dining_room,
                        R.string.location_office,
                    ).map { stringResource(it) }
                val defaultName = remember(device, brand) { defaultRemoteName(context, device, brand) }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // "Default" goes back to the brand + device name rather than a literal "Default".
                    FilterChip(
                        selected = nameInput == defaultName,
                        onClick = { nameInput = defaultName },
                        label = { Text(stringResource(R.string.location_default)) },
                        leadingIcon =
                            if (nameInput == defaultName) {
                                { Icon(Icons.Default.Check, null, Modifier.size(FilterChipDefaults.IconSize)) }
                            } else null,
                    )
                    locations.forEach { location ->
                        val selected = nameInput == location
                        FilterChip(
                            selected = selected,
                            onClick = { nameInput = location },
                            label = { Text(location) },
                            leadingIcon =
                                if (selected) {
                                    { Icon(Icons.Default.Check, null, Modifier.size(FilterChipDefaults.IconSize)) }
                                } else null,
                        )
                    }
                }
            }
        }
    }
}
