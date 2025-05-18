package com.example.sawaapplication.screens.notification.presentation.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sawaapplication.R
import com.example.sawaapplication.screens.notification.presentation.screens.component.NotificationCard
import com.example.sawaapplication.screens.notification.presentation.viewmodels.NotificationViewModel
import java.text.SimpleDateFormat

@SuppressLint("SimpleDateFormat")
@Composable
fun NotificationScreen(
    navController: NavController,
    notificationViewModel: NotificationViewModel = hiltViewModel(),
) {
    val notifications = notificationViewModel.notifications.collectAsState().value

    LaunchedEffect(Unit) {
        notificationViewModel.markAsRead()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(integerResource(R.integer.mediumSpace).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(integerResource(R.integer.mediumSpace).dp))

        if (notifications.isEmpty()) {
            Text(text = stringResource(R.string.noNotifications))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(notifications.size) { index ->
                    val notification = notifications[index]
                    NotificationCard(
                        name = stringResource(R.string.systemNotification),
                        time = SimpleDateFormat("hh:mm a").format(notification.timestamp),
                        action = notification.message,
                        profileImage = null,
                        postImage = null,
                        onClick = {}
                    )
                }
            }
        }
    }
}