package com.example.sawaapplication.screens.communities.presentation.screens.component

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.sawaapplication.R
import com.example.sawaapplication.ui.theme.PrimaryOrange
import com.example.sawaapplication.ui.theme.white

@Composable
fun CommunityFAB(
    selectedTab: Int,
    communityId: String,
    navController: NavHostController
) {
    FloatingActionButton(
        onClick = {
            val route = when (selectedTab) {
                0 -> "create_post/$communityId"
                1 -> "create_event/$communityId"
                else -> return@FloatingActionButton
            }
            navController.navigate(route)
        },
        modifier = Modifier.size(integerResource(R.integer.floatingActionButtonSize).dp),
        shape = CircleShape,
        containerColor = PrimaryOrange,
        contentColor = white,
        elevation = FloatingActionButtonDefaults.elevation(integerResource(R.integer.floatingActionButtonElevation).dp)
    ) {
        Icon(
            imageVector = when (selectedTab) {
                0 -> Icons.Default.Edit
                1 -> Icons.Default.Event
                else -> Icons.Default.Edit
            },
            contentDescription = when (selectedTab) {
                0 -> "Add Post"
                1 -> "Add Event"
                else -> "Add"
            }
        )
    }
}