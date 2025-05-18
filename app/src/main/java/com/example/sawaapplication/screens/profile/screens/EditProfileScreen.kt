package com.example.sawaapplication.screens.profile.screens

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.sawaapplication.R
import com.example.sawaapplication.screens.authentication.presentation.vmModels.LogOutViewModel
import com.example.sawaapplication.screens.notification.presentation.viewmodels.NotificationViewModel
import com.example.sawaapplication.screens.profile.vm.ProfileViewModel
import com.example.sawaapplication.screens.profile.vm.ThemeViewModel
import com.example.sawaapplication.ui.screenComponent.CustomTextField
import com.example.sawaapplication.ui.theme.Red
import com.example.sawaapplication.ui.theme.firstOrange
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    isDarkTheme: Boolean,
    changeAppTheme: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val nameState by viewModel.userName.collectAsState()
    val aboutState by viewModel.aboutMe.collectAsState()
    val profileImageUrl by viewModel.profileImageUrl.collectAsState()

    var name by remember { mutableStateOf(nameState ?: "") }
    var about by remember { mutableStateOf(aboutState ?: "") }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val photoPermissionState = rememberPermissionState(Manifest.permission.READ_MEDIA_IMAGES)
    var showPhotoPermissionDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri

    }

    // Photo Permission Dialog
    if (showPhotoPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoPermissionDialog = false },
            title = { Text("Photo Permission") },
            text = { Text("We need access to your photos so you can add an image for the event.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.markPhotoPermissionRequested()
                    photoPermissionState.launchPermissionRequest()
                    showPhotoPermissionDialog = false
                }) {
                    Text("Allow")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPhotoPermissionDialog = false
                }) {
                    Text("Deny")
                }
            }
        )
    }

    // LaunchedEffect is used here to synchronize the ViewModel state (nameState, aboutState)
    // into the local editable form fields (name, about).
    // This ensures that whenever the ViewModel data updates (e.g., after FireStore loads),
    // the UI text fields are automatically populated with the latest data.
    // Without this, the fields would only initialize once and not reflect later updates.
    LaunchedEffect(nameState, aboutState) {
        name = nameState ?: ""
        about = aboutState ?: ""
    }

//    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

    val isArabic by themeViewModel.isArabic.collectAsState()

//    val imagePickerLauncher =
//        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
//            imageUri = it
//        }

    val logOutViewModel: LogOutViewModel = hiltViewModel()

    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Clickable Image to Upload
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .clickable {
                    if (photoPermissionState.status.isGranted) {
                        imagePickerLauncher.launch("image/*")
                    } else {
                        if (viewModel.shouldRequestPhoto()) {
                            showPhotoPermissionDialog = true
                        } else {
                            Toast.makeText(context, "Please allow photo access in settings", Toast.LENGTH_LONG).show()
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            when {
                imageUri != null -> {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Selected Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                profileImageUrl != null -> {
                    Image(
                        painter = rememberAsyncImagePainter(profileImageUrl),
                        contentDescription = "Profile Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    Text(
                        text = "Tap to upload\nimage",
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        CustomTextField(
            value = name,
            onValueChange = { name = it },
            label = "Name",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        CustomTextField(
            value = about,
            onValueChange = { about = it },
            label = "About me",
            maxLines = 5,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    RoundedCornerShape(16.dp)
                )
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.darkMode),
                    fontSize = 16.sp,
                )

                IconSwitch(
                    checked = isDarkTheme,
                    onCheckedChange = { changeAppTheme()
                        themeViewModel.toggleTheme() },
                    iconOn = Icons.Default.DarkMode,
                    iconOff = Icons.Default.LightMode,
                    modifier = Modifier.size(width = 50.dp, height = 30.dp)
                )
//                SettingsThemeSwitches(
//                    isDark = isDarkTheme,
//                    onCheckedChange = { changeAppTheme()
//                        themeViewModel.toggleTheme()}
//                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    RoundedCornerShape(16.dp)
                )
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.language),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                SettingsSwitches(
                    isArabic = isArabic,
                    onLanguageToggle = {
                        themeViewModel.toggleLanguage()
                        val newLang = if (isArabic) "en" else "ar"
                        changeAppLocale(context, newLang)
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(42.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    logOutViewModel.preformLogOut(navController)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                border = BorderStroke(
                    1.dp, Red
                ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Logout",
                    tint = Red
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.logout),
                    color = Red
                )
            }
            Button(
                onClick = {
                    if (imageUri != null) {
                        viewModel.uploadProfileImage(
                            imageUri!!,
                            onSuccess = {
                                viewModel.updateName(name)
                                viewModel.updateAboutMe(about)
                                notificationViewModel.notifyProfileUpdate() // Store notification
                                Toast.makeText(
                                    context,
                                    "Saved Successfully!",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                navController.popBackStack()
                            },
                            onFailure = {
                                Toast.makeText(
                                    context,
                                    "Failed to upload image",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    } else {
                        viewModel.updateName(name)
                        viewModel.updateAboutMe(about)
                        notificationViewModel.notifyProfileUpdate() // Store notification
                        Toast.makeText(context, "Saved Successfully!", Toast.LENGTH_SHORT)
                            .show()
                        navController.popBackStack()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = firstOrange,
                ),

                ) {
                Text(
                    text = stringResource(id = R.string.save),
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }
        }
    }
}

