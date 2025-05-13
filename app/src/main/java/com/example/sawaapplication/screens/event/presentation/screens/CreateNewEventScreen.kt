package com.example.sawaapplication.screens.event.presentation.screens

import android.Manifest
import android.net.Uri
import android.os.Build
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.sawaapplication.R
import com.example.sawaapplication.screens.event.presentation.vmModels.CreateEventViewModel
import com.example.sawaapplication.ui.screenComponent.CustomTextField
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import java.text.DateFormat
import java.util.Date


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CreateNewEventScreen(
    navController: NavHostController, communityId: String,
    viewModel: CreateEventViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val success = viewModel.success.value
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val shouldRequestGallery = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    var pickedLocation by remember { mutableStateOf<LatLng?>(null) }

    LaunchedEffect(communityId) {
        viewModel.communityId = communityId
    }
    LaunchedEffect(success) {
        if (success) {
            Toast.makeText(context, "Event Created!", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
            viewModel.success.value = false
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.imageUri = uri
    }
    var showDatePicker by remember { mutableStateOf(false) }
    val communityID = viewModel.communityId
    val formattedDate = viewModel.eventDate?.let {
        DateFormat.getDateInstance().format(Date(it))
    } ?: ""

    LaunchedEffect(Unit) {
        if (viewModel.shouldRequestLocation()) {
            locationPermissionState.launchPermissionRequest()

        }
        if (viewModel.shouldRequestGallery()){
            shouldRequestGallery.launchPermissionRequest()
        }
    }

    var showPermissionDialog by remember { mutableStateOf(false) }

    // Location Permission Dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Location Permission") },
            text = { Text("We need your location to pick the event location. Would you like to allow access?") },
            confirmButton = {
                TextButton(onClick = {
                    locationPermissionState.launchPermissionRequest()
                    showPermissionDialog = false
                }) {
                    Text("Allow")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Deny")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(integerResource(R.integer.padding).dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(integerResource(R.integer.verticalArrangement).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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

                        if (communityID != null) {
                            viewModel.createEvent(communityID)
                        }

                        // Use membersLimit for event creation
                    } else {
                        // Show error or ignore
                    }
                },
            ) { Text(stringResource(R.string.create)) }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            stringResource(R.string.newEvent),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
        )

        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            //event image
            Box(
                modifier = Modifier
                    .size(integerResource(R.integer.imageBoxSize).dp)
                    .clip(RoundedCornerShape(integerResource(R.integer.RoundedCornerShape).dp))
                    .clickable { imagePickerLauncher.launch("image/*") }
                    .background(Color.LightGray)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center,

                // Post image
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
                        imageVector = Icons.Outlined.AddAPhoto,
                        contentDescription = "Add photo icon",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            //event name
            CustomTextField(
                value = viewModel.name,
                onValueChange = { viewModel.name = it },
                label = stringResource(R.string.eventName),
            )

            //event description
            CustomTextField(
                value = viewModel.description,
                onValueChange = { viewModel.description = it },
                label = stringResource(R.string.eventDescription),
                singleLine = false,
            )

            //event location
            CustomTextField(
                value = viewModel.locationText,
                onValueChange = {},
                label = stringResource(id = R.string.eventLocation),
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Pick location",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            if (locationPermissionState.status.isGranted) {
                                viewModel.isMapVisible = true
                            } else {
                                showPermissionDialog = false
                            }
                        }
                    )
                }
            )

            when {
//                locationPermissionState.status.isGranted -> {
//
//                }

                locationPermissionState.status.shouldShowRationale -> {
                    // Display rationale if the user has previously denied permission
                    LaunchedEffect(Unit) {
                        Toast.makeText(
                            context,
                            "Location permission is needed to pick your event location.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                else -> {
                    // Handle if permission is permanently denied allow from settings
                    LaunchedEffect(Unit) {
                        Toast.makeText(
                            context,
                            "Please grant location access in settings.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            }

            //event date
            CustomTextField(
                value = formattedDate,
                onValueChange = {},
                label = stringResource(id = R.string.eventDate),
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Pick date",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { showDatePicker = true }
                    )
                }
            )
            if (showDatePicker) {
                DatePickerModal(
                    onDateSelected = {
                        viewModel.eventDate = it
                        showDatePicker = false
                    },
                    onDismiss = { showDatePicker = false }
                )
            }

            //event member limit
            CustomTextField(
                value = viewModel.membersLimitInput,
                onValueChange = { viewModel.membersLimitInput = it },
                label = stringResource(R.string.eventMembersLimit),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            // Google Map to pick location
            if (viewModel.isMapVisible) {
                AlertDialog(
                    onDismissRequest = { viewModel.isMapVisible = false }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                    ) {
                        GoogleMap(
                            modifier = Modifier
                                .fillMaxSize(),
                            onMapClick = { latLng ->
                                pickedLocation = latLng
                                viewModel.location = GeoPoint(latLng.latitude, latLng.longitude)
                                viewModel.locationText = "${latLng.latitude}, ${latLng.longitude}"
                                viewModel.isMapVisible = false  // Close the map after selecting location
                            },
                            cameraPositionState = rememberCameraPositionState {
                                position = CameraPosition.fromLatLngZoom(
                                    pickedLocation ?: LatLng(24.7136, 46.6753), 5f
                                )
                            }
                        )
                    }
                }
            }

        }
    }
