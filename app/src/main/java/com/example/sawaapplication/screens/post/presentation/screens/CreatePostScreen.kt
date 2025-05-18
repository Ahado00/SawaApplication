package com.example.sawaapplication.screens.post.presentation.screens

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.sawaapplication.R
import com.example.sawaapplication.screens.post.presentation.vmModels.CreatePostViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CreatePostScreen(
    navController: NavController,
    communityId : String
) {
    val context = LocalContext.current
    val viewModel: CreatePostViewModel = hiltViewModel()
    val imageUri by remember { derivedStateOf { viewModel.imageUri } }

    val photoPermissionState = rememberPermissionState(Manifest.permission.READ_MEDIA_IMAGES)
    var showPhotoPermissionDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.imageUri = uri

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
            }
        )
    }

    LaunchedEffect(communityId) {
        viewModel.communityId = communityId
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(integerResource(R.integer.padding).dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {

        // Top Row: Cancel + Post button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(integerResource(R.integer.padding).dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            TextButton(onClick = { navController.popBackStack() }) {
                Text(stringResource(R.string.cancel))
            }

            Button(
                onClick = {
                    viewModel.createPost(communityId)
                    navController.navigate("community_screen/$communityId"){
                        popUpTo("create_post/$communityId") {//make sure to remove "create post screen" from the stack
                            inclusive = true
                        }
                        launchSingleTop = true // make sure only one instance of "CommunityScreen" is in the stack
                    }
                },
            ) { Text(stringResource(R.string.post)) }

        }

        // Row for Community Image + Add Photo Icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(integerResource(R.integer.padding).dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Community Image
            Image(
                painter = rememberAsyncImagePainter("https://api.dicebear.com/7.x/lorelei/svg"),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(integerResource(R.integer.communityImageSize).dp)
                    .clip(RoundedCornerShape(integerResource(R.integer.roundedCornerShapeCircle)))
                    .background(Color.Blue)
            )
            // Add Photo Icon
            val askPhotoPermissionFromSettings = stringResource(R.string.askPhotoPermissionFromSettings)
            Icon(
                imageVector = Icons.Outlined.AddAPhoto,
                contentDescription = "Add photo",
                modifier = Modifier
                    .clickable {
                        if (photoPermissionState.status.isGranted) {
                            imagePickerLauncher.launch("image/*")
                        } else {
                            if (viewModel.shouldRequestPhoto()) {
                                showPhotoPermissionDialog = true
                            } else {
                                Toast.makeText(context, askPhotoPermissionFromSettings, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    .padding(integerResource(R.integer.smallerSpace).dp),
                tint = Color.Gray
            )
        }

        //Post Content
        OutlinedTextField(
            value = viewModel.content,
            onValueChange = { viewModel.content = it },
            placeholder = { Text(stringResource(R.string.postContentPlaceholder), color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .height(integerResource(R.integer.postContentSize).dp)
                .padding(bottom = integerResource(R.integer.padding).dp),
            singleLine = false,
            shape = RoundedCornerShape(integerResource(R.integer.postContentRoundedCornerShape).dp),
        )

        // Show selected image preview if available
        if (imageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = "Selected Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(integerResource(R.integer.postContentSize).dp)
                    .padding(bottom = integerResource(R.integer.padding).dp)
                    .clip(RoundedCornerShape(integerResource(R.integer.postSelectedImageRoundedCornerShape).dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}