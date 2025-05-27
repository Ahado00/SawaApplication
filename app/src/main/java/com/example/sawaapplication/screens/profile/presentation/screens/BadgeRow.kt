package com.example.sawaapplication.screens.profile.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.sawaapplication.screens.profile.domain.model.Badge
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sawaapplication.R
import com.example.sawaapplication.screens.profile.presentation.vm.ProfileViewModel

@Composable
fun BadgeRow(badges: List<Badge>) {
    val context = LocalContext.current
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val defs by profileViewModel.badgeDefinitions.collectAsState()
    val earnedIds = badges.map { it.id }

    // The badge order
    val orderedIds = listOf(
        "3_days", "1_week", "2_weeks", "3_weeks", "1_month",
    )
    val orderedDefs = orderedIds.mapNotNull { id -> defs.find { it.id == id } }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        items(orderedDefs) { badgeDef ->
            val isEarned = earnedIds.contains(badgeDef.id)
            Card(
                modifier = Modifier
                    .size(100.dp)
                    .padding(end = 12.dp)
                    .clickable {
                        val resId = getBadgeToastMessageResId(badgeDef.id)
                        if (resId != 0) {
                            val desc = context.getString(resId as Int)
                            Toast.makeText(context, desc, Toast.LENGTH_SHORT).show()
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isEarned) {
                        AsyncImage(
                            model = badgeDef.iconUrl,
                            contentDescription = getBadgeDisplayName(badgeDef.id),
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Text("🔒", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = getBadgeDisplayName(badgeDef.id),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isEarned) LocalContentColor.current else Color.Gray
                    )
                }
            }
        }
    }
}
fun getBadgeToastMessageResId(badgeId: String): Comparable<*> {
    return when (badgeId) {
        "3_days"   -> R.string.badge_attendance_3_days
        "1_week"   -> R.string.badge_attendance_7_days
        "2_weeks"  -> R.string.badge_attendance_14_days
        "3_weeks"  -> R.string.badge_attendance_21_days
        "1_month"  -> R.string.badge_attendance_1_month
        else -> 0
    }
}

@Composable
fun getBadgeDisplayName(badgeId: String): String {
    return when (badgeId) {
        "3_days"   -> stringResource(R.string.badgeExplorer)
        "1_week"   -> stringResource(R.string.badgeRisingStar)
        "2_weeks"  -> stringResource(R.string.badgeSteadySoul)
        "3_weeks"  -> stringResource(R.string.badgePathfinder)
        "1_month"  -> stringResource(R.string.badgeLegend)
        else       -> ""
    }
}
