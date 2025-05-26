package com.example.sawaapplication.screens.event.presentation.screens

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.sawaapplication.R
import com.example.sawaapplication.screens.event.domain.model.Event
import com.example.sawaapplication.screens.event.presentation.vmModels.CreateEventViewModel
import com.example.sawaapplication.screens.notification.presentation.viewmodels.NotificationViewModel
import com.example.sawaapplication.ui.screenComponent.CustomTextField
import com.example.sawaapplication.utils.getCityNameFromGeoPoint
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CreateNewEventScreen(
    navController: NavHostController,
    communityId: String,
    viewModel: CreateEventViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel(),
    isEditMode: Boolean = false,
    eventToEdit: Event? = null,
    onUpdateClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val success by viewModel.success.collectAsState()
    val communityID = viewModel.communityId
    val loading by viewModel.loading.collectAsState()


    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val photoPermissionState = rememberPermissionState(Manifest.permission.READ_MEDIA_IMAGES)

    var pickedLocation by remember { mutableStateOf<LatLng?>(null) }
    var showPhotoPermissionDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.imageUri = uri
    }
    val formattedDate = viewModel.eventDate?.let {
        DateFormat.getDateInstance().format(Date(it))
    } ?: ""

    LaunchedEffect(Unit) {
        viewModel.communityId = communityId

        if (isEditMode && viewModel.name.isBlank()) {
            eventToEdit?.let {
                viewModel.editingEvent = it
                viewModel.loadEventForEdit(it)
            }
        }
    }

    // Photo Permission Dialog
    if (showPhotoPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoPermissionDialog = false },
            title = { Text(stringResource(R.string.photoPermission)) },
            text = { Text(stringResource(R.string.askPhotoPermission)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.markPhotoPermissionRequested()
                    photoPermissionState.launchPermissionRequest()
                    showPhotoPermissionDialog = false
                }) { Text(stringResource(R.string.allow)) }
            },
            dismissButton = {
                TextButton(onClick = { showPhotoPermissionDialog = false }) {
                    Text(stringResource(R.string.deny))
                }
            }
        )
    }

    // Location Permission Dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text(stringResource(R.string.locationPermission)) },
            text = { Text(stringResource(R.string.askLocationPermission)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.markLocationPermissionRequested()
                    locationPermissionState.launchPermissionRequest()
                    showPermissionDialog = false
                }) { Text(stringResource(R.string.allow)) }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text(stringResource(R.string.deny))
                }
            })
    }
    // Photo Permission Dialog
    if (showPhotoPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoPermissionDialog = false },
            title = { Text(stringResource(R.string.photoPermission)) },
            text = { Text(stringResource(R.string.askPhotoPermission)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.markPhotoPermissionRequested()
                    photoPermissionState.launchPermissionRequest()
                    showPhotoPermissionDialog = false
                }) {
                    Text(stringResource(R.string.allow))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPhotoPermissionDialog = false
                }) {
                    Text(stringResource(R.string.deny))
                }
            })
    }

    val eventCreated = stringResource(R.string.eventCreated)
    LaunchedEffect(communityId, success) {
        viewModel.communityId = communityId
        if (success) {
            // Notify creator about the event creation
            notificationViewModel.notifyEventCreated(viewModel.name)

            // Notify community members about the new event
            notificationViewModel.notifyCommunityMembers(
                communityId = communityId,
                eventName = viewModel.name
            )

            // Show success toast message
            Toast.makeText(context, eventCreated, Toast.LENGTH_SHORT).show()

            // Navigate back to previous screen
            navController.popBackStack()

            viewModel.resetSuccess()
        } else {
            Toast.makeText(context, "Event creation failed", Toast.LENGTH_SHORT).show()
        }


    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(integerResource(R.integer.padding).dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(integerResource(R.integer.verticalArrangement).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { navController.popBackStack() }) {
                Text(stringResource(R.string.cancel))
            }
            Button(
                onClick = {
                    if (viewModel.membersLimit != null && viewModel.membersLimit!! > 0) {
                        if (isEditMode) onUpdateClick()
                        else viewModel.createEvent(communityId)
                    } else {
                        Toast.makeText(
                            context,
                            "Please enter a valid member limit.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            ) {
                Text(text = if (isEditMode) stringResource(R.string.ok) else stringResource(R.string.create))
            }
        }

        Spacer(Modifier.height(integerResource(R.integer.mediumSpace).dp))

        // Title
        Text(
            stringResource(R.string.newEvent),
            fontWeight = FontWeight.Bold,
            fontSize = integerResource(R.integer.newEventTextSize).sp,
        )

        Column(
            modifier = Modifier.padding(integerResource(R.integer.newEventTextSize).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val askPhotoPermissionFromSettings =
                stringResource(R.string.askPhotoPermissionFromSettings)

            // Event Image
            Box(
                modifier = Modifier
                    .size(integerResource(R.integer.imageBoxSize).dp)
                    .clip(RoundedCornerShape(integerResource(R.integer.RoundedCornerShape).dp))
                    .clickable {
                        if (photoPermissionState.status.isGranted) {
                            imagePickerLauncher.launch("image/*")
                        } else {
                            if (viewModel.shouldRequestPhoto()) showPhotoPermissionDialog = true
                            else Toast.makeText(
                                context,
                                askPhotoPermissionFromSettings,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    .background(Color.LightGray)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center,
            ) {
                if (viewModel.imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(viewModel.imageUri),
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Outlined.AddAPhoto,
                        contentDescription = "Add photo icon",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Input Fields
            CustomTextField(
                viewModel.name,
                { viewModel.name = it },
                stringResource(R.string.eventName)
            )
            CustomTextField(
                viewModel.description,
                { viewModel.description = it },
                stringResource(R.string.eventDescription),
                singleLine = false
            )

            // Location Picker
            val askLocationPermissionFromSettings =
                stringResource(R.string.askLocationPermissionFromSettings)
            CustomTextField(
                value = context.getCityNameFromGeoPoint(viewModel.location),
                onValueChange = { },
                label = stringResource(R.string.eventLocation),
                readOnly = true,
                trailingIcon = {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            if (locationPermissionState.status.isGranted) viewModel.isMapVisible =
                                true
                            else if (viewModel.shouldRequestLocation()) showPermissionDialog = true
                            else Toast.makeText(
                                context,
                                askLocationPermissionFromSettings,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                }
            )

            CustomTextField(
                value = formattedDate,
                onValueChange = {}, // readOnly
                label = stringResource(R.string.eventDate),
                readOnly = true,
                trailingIcon = {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { showDatePicker = true }
                    )
                }
            )


            CustomTextField(
                viewModel.eventTime,
                { viewModel.eventTime = it },
                stringResource(R.string.eventTime),
                readOnly = true,
                trailingIcon = {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { showTimePicker = true })
                }
            )

            CustomTextField(
                viewModel.membersLimitInput,
                { viewModel.membersLimitInput = it },
                stringResource(R.string.eventMembersLimit),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        // Date Picker Modal
        if (showDatePicker) {
            DatePickerModal(onDateSelected = {
                viewModel.eventDate = it
                showDatePicker = false
            }, onDismiss = { showDatePicker = false })
        }

        // Time Picker Modal
        if (showTimePicker) {
            TimePickerModal(onTimeSelected = { hour, minute ->
                val formatted = String.format("%02d:%02d:00", hour, minute)
                viewModel.eventTime = formatted
                showTimePicker = false
            }, onDismiss = { showTimePicker = false })
        }

        // Google Map Modal
        // Google Map to pick location
        if (viewModel.isMapVisible) {
            Dialog(onDismissRequest = { viewModel.isMapVisible = false }) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    tonalElevation = 8.dp,
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Pick a Location", style = MaterialTheme.typography.titleMedium)

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                        ) {
                            val cameraPositionState = rememberCameraPositionState {
                                position = CameraPosition.fromLatLngZoom(
                                    pickedLocation ?: LatLng(24.4403644, 39.6411140),
                                    10f
                                )
                            }

                            GoogleMap(
                                modifier = Modifier.fillMaxSize(),
                                cameraPositionState = cameraPositionState,
                                onMapClick = { latLng ->
                                    pickedLocation = latLng
                                    viewModel.location = GeoPoint(latLng.latitude, latLng.longitude)
                                    viewModel.locationText =
                                        "${latLng.latitude}, ${latLng.longitude}"
                                }
                            ) {
                                pickedLocation?.let {
                                    Marker(
                                        state = MarkerState(position = it),
                                        title = "Selected Location"
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = {
                                pickedLocation = null
                                viewModel.location = GeoPoint(0.0, 0.0)
                                viewModel.locationText = ""
                            }) {
                                Text("Reselect")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            TextButton(onClick = {
                                viewModel.isMapVisible = false
                            }) {
                                Text("OK")
                            }
                        }
                    }
                }
            }
        }
    }

    if (loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}