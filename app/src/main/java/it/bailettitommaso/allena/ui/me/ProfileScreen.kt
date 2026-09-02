package it.bailettitommaso.allena.ui.me

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import it.bailettitommaso.allena.ui.components.ErrorText
import it.bailettitommaso.allena.ui.components.AllenaButton
import it.bailettitommaso.allena.ui.components.AllenaTextField
import it.bailettitommaso.allena.ui.theme.AllenaTheme

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onChangePassword: () -> Unit = {},
    onSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val loggedOut by viewModel.loggedOut.collectAsStateWithLifecycle()
    var photoSheetOpen by remember { mutableStateOf(false) }

    LaunchedEffect(loggedOut) {
        if (loggedOut) onLogout()
    }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        viewModel.onCaptureResult(saved)
    }
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(viewModel::onPhotoPicked)
    }

    ProfileContent(
        profile = profile,
        onLogout = viewModel::logout,
        onChangePassword = onChangePassword,
        onSettings = onSettings,
        onStartEdit = viewModel::startEdit,
        onNameChange = viewModel::onNameChange,
        onCancelEdit = viewModel::cancelEdit,
        onSave = viewModel::save,
        onAvatarClick = { photoSheetOpen = true },
        modifier = modifier,
    )

    if (photoSheetOpen) {
        PhotoSourceSheet(
            canRemove = profile.avatarUrl != null,
            onDismiss = { photoSheetOpen = false },
            onTakePhoto = {
                photoSheetOpen = false
                takePicture.launch(viewModel.prepareCaptureUri())
            },
            onPickPhoto = {
                photoSheetOpen = false
                pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onRemove = {
                photoSheetOpen = false
                viewModel.removeAvatar()
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoSourceSheet(
    canRemove: Boolean,
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickPhoto: () -> Unit,
    onRemove: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        ListItem(
            headlineContent = { Text("Take photo") },
            leadingContent = { Icon(Icons.Filled.PhotoCamera, contentDescription = null) },
            modifier = Modifier.clickable(onClick = onTakePhoto),
        )
        ListItem(
            headlineContent = { Text("Choose from gallery") },
            leadingContent = { Icon(Icons.Filled.PhotoLibrary, contentDescription = null) },
            modifier = Modifier.clickable(onClick = onPickPhoto),
        )
        if (canRemove) {
            ListItem(
                headlineContent = { Text("Remove photo") },
                leadingContent = { Icon(Icons.Filled.Delete, contentDescription = null) },
                modifier = Modifier.clickable(onClick = onRemove),
            )
        }
    }
}

@Composable
private fun ProfileContent(
    profile: ProfileUiState,
    onLogout: () -> Unit,
    onChangePassword: () -> Unit = {},
    onSettings: () -> Unit = {},
    onStartEdit: () -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onCancelEdit: () -> Unit = {},
    onSave: () -> Unit = {},
    onAvatarClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (profile.isLoading) {
            CircularProgressIndicator()
        } else {
            ProfileDetails(
                profile = profile,
                onLogout = onLogout,
                onChangePassword = onChangePassword,
                onSettings = onSettings,
                onStartEdit = onStartEdit,
                onNameChange = onNameChange,
                onCancelEdit = onCancelEdit,
                onSave = onSave,
                onAvatarClick = onAvatarClick,
            )
        }
    }
}

@Composable
private fun ProfileDetails(
    profile: ProfileUiState,
    onLogout: () -> Unit,
    onChangePassword: () -> Unit,
    onSettings: () -> Unit,
    onStartEdit: () -> Unit,
    onNameChange: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onSave: () -> Unit,
    onAvatarClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        ProfileHeader(
            name = profile.name,
            email = profile.email,
            avatarUrl = profile.avatarUrl,
            isAvatarBusy = profile.isAvatarBusy,
            onAvatarClick = onAvatarClick,
        )

        if (!profile.isEditing) {
            profile.error?.let { ErrorText(message = it.message(), modifier = Modifier.fillMaxWidth()) }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            if (profile.isEditing) {
                ProfileNameField(
                    value = profile.nameDraft,
                    onValueChange = onNameChange,
                    enabled = !profile.isSaving,
                )
            } else {
                ProfileRow(
                    label = "Name",
                    value = profile.name.ifBlank { "—" },
                    trailing = {
                        IconButton(onClick = onStartEdit) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit name")
                        }
                    },
                )
            }
            HorizontalDivider()
            ProfileRow(label = "Email", value = profile.email.ifBlank { "—" })
        }

        if (profile.isEditing) {
            profile.error?.let { ErrorText(message = it.message(), modifier = Modifier.fillMaxWidth()) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = onCancelEdit,
                    enabled = !profile.isSaving,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Cancel")
                }
                AllenaButton(
                    text = "Save",
                    onClick = onSave,
                    enabled = profile.canSave,
                    loading = profile.isSaving,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text("Change password") },
                leadingContent = { Icon(Icons.Filled.Lock, contentDescription = null) },
                trailingContent = {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                },
                modifier = Modifier.clickable(onClick = onChangePassword),
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Settings") },
                leadingContent = { Icon(Icons.Filled.Settings, contentDescription = null) },
                trailingContent = {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                },
                modifier = Modifier.clickable(onClick = onSettings),
            )
        }

        Spacer(Modifier.weight(1f))

        AllenaButton(
            text = "Log out",
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ProfileHeader(
    name: String,
    email: String,
    avatarUrl: String?,
    isAvatarBusy: Boolean,
    onAvatarClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable(enabled = !isAvatarBusy, onClick = onAvatarClick),
                contentAlignment = Alignment.Center,
            ) {
                if (avatarUrl != null) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Profile photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Text(
                        text = name.trim().firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }

                if (isAvatarBusy) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(enabled = !isAvatarBusy, onClick = onAvatarClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.PhotoCamera,
                    contentDescription = "Change profile photo",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Text(
            text = name.ifBlank { "—" },
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ProfileNameField(value: String, onValueChange: (String) -> Unit, enabled: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Name",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AllenaTextField(
            value = value,
            onValueChange = onValueChange,
            label = "Name",
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ProfileRow(label: String, value: String, trailing: @Composable (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = if (trailing == null) 16.dp else 4.dp, top = 12.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
            )
            trailing?.invoke()
        }
    }
}

private fun ProfileError.message(): String = when (this) {
    ProfileError.OFFLINE -> "No connection. Check your network and try again."
    ProfileError.GENERIC -> "Something went wrong. Please try again."
    ProfileError.PHOTO_REJECTED -> "That photo was rejected. Use a JPG, PNG or WebP under 5 MB."
}

@Preview(showBackground = true)
@Composable
private fun ProfileContentPreview() {
    AllenaTheme {
        ProfileContent(
            profile = ProfileUiState(isLoading = false, name = "Mario", email = "mario.rossi@example.com"),
            onLogout = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileContentDarkPreview() {
    AllenaTheme {
        ProfileContent(
            profile = ProfileUiState(isLoading = false, name = "Mario", email = "mario.rossi@example.com"),
            onLogout = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileContentEditingPreview() {
    AllenaTheme {
        ProfileContent(
            profile = ProfileUiState(
                isLoading = false,
                name = "Mario",
                email = "mario.rossi@example.com",
                isEditing = true,
                nameDraft = "Mario Rossi",
            ),
            onLogout = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileContentEditingErrorPreview() {
    AllenaTheme {
        ProfileContent(
            profile = ProfileUiState(
                isLoading = false,
                name = "Mario",
                email = "mario.rossi@example.com",
                isEditing = true,
                nameDraft = "Mario Rossi",
                error = ProfileError.OFFLINE,
            ),
            onLogout = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileContentLoadingPreview() {
    AllenaTheme {
        ProfileContent(profile = ProfileUiState(isLoading = true), onLogout = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileContentWithPhotoPreview() {
    AllenaTheme {
        ProfileContent(
            profile = ProfileUiState(
                isLoading = false,
                name = "Mario",
                email = "mario.rossi@example.com",
                avatarUrl = "https://example.test/avatar.jpg",
            ),
            onLogout = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileContentAvatarBusyPreview() {
    AllenaTheme {
        ProfileContent(
            profile = ProfileUiState(
                isLoading = false,
                name = "Mario",
                email = "mario.rossi@example.com",
                isAvatarBusy = true,
            ),
            onLogout = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileContentAvatarErrorPreview() {
    AllenaTheme {
        ProfileContent(
            profile = ProfileUiState(
                isLoading = false,
                name = "Mario",
                email = "mario.rossi@example.com",
                error = ProfileError.PHOTO_REJECTED,
            ),
            onLogout = {},
        )
    }
}
