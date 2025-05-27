package com.example.sawaapplication.screens.notification.presentation.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sawaapplication.R
import com.example.sawaapplication.screens.notification.presentation.screens.component.NotificationCard
import com.example.sawaapplication.screens.notification.presentation.viewmodels.NotificationViewModel
import com.example.sawaapplication.screens.profile.presentation.vm.ProfileViewModel
import com.example.sawaapplication.ui.theme.black
import java.text.SimpleDateFormat

@SuppressLint("SimpleDateFormat")
@Composable
fun NotificationScreen(
    profileViewModel: ProfileViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val userId by profileViewModel.currentUserId.collectAsState()
    val notifications by notificationViewModel.notifications.collectAsState()
    val rems by notificationViewModel.reminders.collectAsState()

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            notificationViewModel.fetchNotifications()
            notificationViewModel.loadReminders(userId)
        }
    }
    LaunchedEffect(Unit) {
        notificationViewModel.markAsRead()
    }
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Reminders Section
        if (rems.isNotEmpty()) {
            item {
                Text(
                    stringResource(R.string.eventAttendanceReminder),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(rems) { rem ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.tertiaryContainer)

                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.didYouAttend) + rem.message + "?",
                            Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(48.dp, 32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable {
                                    notificationViewModel.answerReminder(
                                        userId,
                                        rem,
                                        true
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.yes),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.background,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .size(48.dp, 32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    notificationViewModel.answerReminder(
                                        userId,
                                        rem,
                                        false
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.no),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))
            }
        }

        // General Notifications Section
        if (notifications.isNotEmpty()) {
            item {
                Text(
                    stringResource(R.string.notifications),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(notifications) { notification ->
                NotificationCard(
                    name = stringResource(R.string.systemNotification),
                    time = SimpleDateFormat("hh:mm a").format(notification.timestamp),
                    action = notification.message,
                    profileImage = null,
                    postImage = null,
                )
            }
        }

        // No notifications
        if (rems.isEmpty() && notifications.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.noNotifications),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
