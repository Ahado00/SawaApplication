package com.example.sawaapplication.screens.notification.presentation.screens.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sawaapplication.R

@Composable
fun NotificationCard(
    name: String? = null,
    time: String,
    action: String,
    profileImage: Painter? = null,
    postImage: Painter? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(integerResource(R.integer.extraSmallSpace).dp)
            .border(
                width = integerResource(R.integer.boxBorderWidth).dp,
                color = Color.LightGray,
                shape = MaterialTheme.shapes.medium
            )
            .clip(MaterialTheme.shapes.medium)
            .padding(integerResource(R.integer.smallerSpace).dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Profile Image
            if (profileImage != null) {
                Image(
                    painter = profileImage,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(integerResource(R.integer.profileImageSizeLeft).dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(integerResource(R.integer.largeSpace).dp))


            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (!name.isNullOrBlank()) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(integerResource(R.integer.extraSmallSpace).dp))
                        }

                        Text(
                            text = action,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Time
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = integerResource(R.integer.smallerSpace).dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(integerResource(R.integer.actionTimeSpacer).dp))

            // Post Image
            if (postImage != null) {
                Image(
                    painter = postImage,
                    contentDescription = "Post Picture",
                    modifier = Modifier
                        .size(integerResource(R.integer.profileImageSizeRight).dp)
                        .clip(RectangleShape)
                )
            }
        }
    }
}
