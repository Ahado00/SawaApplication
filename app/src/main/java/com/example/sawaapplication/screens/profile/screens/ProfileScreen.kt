package com.example.sawaapplication.screens.profile.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.sawaapplication.R
import com.example.sawaapplication.navigation.Screen
import com.example.sawaapplication.screens.authentication.presentation.vmModels.LogOutViewModel
import com.example.sawaapplication.screens.home.presentation.screens.component.CustomTabRow
import com.example.sawaapplication.screens.home.presentation.vmModels.HomeViewModel
import com.example.sawaapplication.screens.profile.vm.ProfileViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val userName by profileViewModel.userName.collectAsState()
    val userEmail by profileViewModel.userEmail.collectAsState()
    val aboutMe by profileViewModel.aboutMe.collectAsState()
    val imageUrl by profileViewModel.profileImageUrl.collectAsState()
    val userCurrentId by profileViewModel.currentUserId.collectAsState()

    val logOutViewModel: LogOutViewModel = hiltViewModel()
    var showMenu by remember { mutableStateOf(false) }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Posts", "Likes")

    LaunchedEffect(Unit) {
        profileViewModel.loadCurrentUserId()
    }
    LaunchedEffect(selectedTabIndex, userCurrentId) {
        if (userCurrentId.isNotEmpty()) {
            when (selectedTabIndex) {
                0 -> homeViewModel.fetchPostsByUser(userCurrentId)
                1 -> homeViewModel.fetchLikedPostsByUser(userCurrentId)
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = { showMenu = !showMenu },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(imageVector = Icons.Filled.Settings, contentDescription = "Settings")

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Dark Mode") },
                    onClick = {

                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Log Out") },
                    onClick = {
                        showMenu = false
                        logOutViewModel.preformLogOut(navController)
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(integerResource(R.integer.profilePadding).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(integerResource(R.integer.topSpacing).dp))

            Box(
                modifier = Modifier.size(integerResource(R.integer.photoBoxSize).dp)
            ) {

                Image(
                    painter = if (imageUrl != null)
                        rememberAsyncImagePainter(imageUrl)
                    else
                        painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = "Profile image",
                    contentScale = ContentScale.Crop,

                    modifier = Modifier
                        .clip(CircleShape)
                        .size(integerResource(R.integer.photoBoxSize).dp)
                )

                IconButton(
                    onClick = { navController.navigate(Screen.EditProfile.route) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(
                            color = Color(0xFFFF5722),
                            shape = RoundedCornerShape(48.dp)
                        )
                        .size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Create,
                        contentDescription = "Edit",
                        modifier = Modifier
                            .size(16.dp),
                        tint = MaterialTheme.colorScheme.background
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = userName ?: "Unknown",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Text(
                text = userEmail ?: "No email",
                textAlign = TextAlign.Center,
                fontSize = integerResource(R.integer.textSize1).sp
            )
            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.aboutMe),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = integerResource(R.integer.textSize2).sp
            )

            Text(
                text = aboutMe ?: "",
                textAlign = TextAlign.Center,
                fontSize = integerResource(R.integer.textSize2).sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
                Spacer(Modifier.height(12.dp))

                // Tab Row for "Posts" and "Likes"
            CustomTabRow(
                tabs = tabs,
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it }
            )
                // Posts and Liked Posts
                when (selectedTabIndex) {
                    0 -> MyPostsTab(homeViewModel, navController, userCurrentId)
                    1 -> PostsTabLike(homeViewModel, navController, userCurrentId)
                }
            }
        }
    }

