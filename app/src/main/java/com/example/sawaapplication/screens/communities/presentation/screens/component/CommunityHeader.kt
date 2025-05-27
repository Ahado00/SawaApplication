package com.example.sawaapplication.screens.communities.presentation.screens.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.sawaapplication.R
import com.example.sawaapplication.screens.communities.presentation.screens.CommunityImageSection
import com.example.sawaapplication.screens.communities.presentation.screens.CommunityInfoSection
import com.example.sawaapplication.ui.theme.PrimaryOrange

@Composable
fun CommunityHeader(
    communityDetail: com.example.sawaapplication.screens.communities.domain.model.Community?,
    isAdmin: Boolean,
    isUserJoined: Boolean,
    navController: NavHostController,
    communityId: String,
    onJoinCommunity: () -> Unit,
    onShowLeaveCommunityDialog: () -> Unit
) {
    Spacer(Modifier.height(16.dp))

    // Community Image with Edit Button
    CommunityImageSection(
        communityDetail = communityDetail,
        isAdmin = isAdmin,
        navController = navController,
        communityId = communityId
    )

    Spacer(Modifier.height(integerResource(R.integer.itemSpacerH2nd).dp))

    // Community Info
    CommunityInfoSection(communityDetail = communityDetail)

    Spacer(Modifier.height(integerResource(R.integer.itemSpacerH).dp))

    // Action Buttons
    CommunityActionButtons(
        isAdmin = isAdmin,
        isUserJoined = isUserJoined,
        navController = navController,
        communityId = communityId,
        onJoinCommunity = onJoinCommunity,
        onShowLeaveCommunityDialog = onShowLeaveCommunityDialog
    )
}

@Composable
fun CommunityActionButtons(
    isAdmin: Boolean,
    isUserJoined: Boolean,
    navController: NavHostController,
    communityId: String,
    onJoinCommunity: () -> Unit,
    onShowLeaveCommunityDialog: () -> Unit
) {
    when {
        isAdmin -> {
            AdminChatButton(navController, communityId)
        }

        isUserJoined -> {
            MemberActionButtons(
                navController = navController,
                communityId = communityId,
                onShowLeaveCommunityDialog = onShowLeaveCommunityDialog
            )
        }

        else -> {
            JoinCommunityButton(onJoinCommunity = onJoinCommunity)
        }
    }
}

@Composable
fun AdminChatButton(navController: NavHostController, communityId: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        OutlinedButton(
            onClick = { navController.navigate("chat/$communityId") },
            shape = RoundedCornerShape(integerResource(R.integer.roundedCornerShapeCircle)),
            border = BorderStroke(integerResource(R.integer.buttonStroke).dp, PrimaryOrange),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            contentPadding = PaddingValues(
                horizontal = integerResource(R.integer.buttonPaddingH).dp,
                vertical = integerResource(R.integer.buttonPaddingV).dp
            ),
            elevation = ButtonDefaults.buttonElevation(integerResource(R.integer.buttonElevation).dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                painter = painterResource(R.drawable.chats),
                contentDescription = "chat icon",
                tint = PrimaryOrange,
                modifier = Modifier.size(integerResource(R.integer.iconSize).dp)
            )
            Spacer(Modifier.width(integerResource(R.integer.itemSpacerH3ed).dp))
            Text(stringResource(R.string.chat))
        }
    }
}

@Composable
fun MemberActionButtons(
    navController: NavHostController,
    communityId: String,
    onShowLeaveCommunityDialog: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(integerResource(R.integer.padding).dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Leave Community Button
        OutlinedButton(
            onClick = onShowLeaveCommunityDialog,
            shape = RoundedCornerShape(integerResource(R.integer.roundedCornerShapeCircle)),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            border = BorderStroke(integerResource(R.integer.buttonStroke).dp, PrimaryOrange),
            contentPadding = PaddingValues(
                horizontal = integerResource(R.integer.buttonPaddingH).dp,
                vertical = integerResource(R.integer.buttonPaddingV).dp
            ),
            elevation = ButtonDefaults.buttonElevation(integerResource(R.integer.buttonElevation).dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                painter = painterResource(R.drawable.unjoind),
                contentDescription = "leave icon",
                tint = PrimaryOrange,
                modifier = Modifier.size(integerResource(R.integer.iconSize).dp)
            )
            Spacer(Modifier.width(integerResource(R.integer.itemSpacerH3ed).dp))
            Text(stringResource(R.string.joined))
        }

        Spacer(Modifier.width(8.dp))

        // Chat Button
        OutlinedButton(
            onClick = { navController.navigate("chat/$communityId") },
            shape = RoundedCornerShape(integerResource(R.integer.roundedCornerShapeCircle)),
            border = BorderStroke(integerResource(R.integer.buttonStroke).dp, PrimaryOrange),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            contentPadding = PaddingValues(
                horizontal = integerResource(R.integer.buttonPaddingH).dp,
                vertical = integerResource(R.integer.buttonPaddingV).dp
            ),
            elevation = ButtonDefaults.buttonElevation(integerResource(R.integer.buttonElevation).dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                painter = painterResource(R.drawable.chats),
                contentDescription = "chat icon",
                tint = PrimaryOrange,
                modifier = Modifier.size(integerResource(R.integer.iconSize).dp)
            )
            Spacer(Modifier.width(integerResource(R.integer.itemSpacerH3ed).dp))
            Text(stringResource(R.string.chat))
        }
    }
}

@Composable
fun JoinCommunityButton(onJoinCommunity: () -> Unit) {
    Button(
        onClick = onJoinCommunity,
        shape = RoundedCornerShape(integerResource(R.integer.roundedCornerShapeCircle)),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
        contentPadding = PaddingValues(
            horizontal = integerResource(R.integer.buttonPaddingH).dp,
            vertical = integerResource(R.integer.buttonPaddingV).dp
        ),
        elevation = ButtonDefaults.buttonElevation(integerResource(R.integer.buttonElevation).dp)
    ) {
        Icon(Icons.Default.PersonAdd, contentDescription = null)
        Spacer(Modifier.width(integerResource(R.integer.itemSpacerH3ed).dp))
        Text(
            stringResource(R.string.joinCommunity),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

