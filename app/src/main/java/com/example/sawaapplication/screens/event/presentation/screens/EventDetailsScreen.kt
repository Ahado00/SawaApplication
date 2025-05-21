package com.example.sawaapplication.screens.event.presentation.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.sawaapplication.screens.home.presentation.vmModels.HomeViewModel

@Composable
fun EventDetailScreen(
    communityId: String,
    eventId: String,
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
) {

    LaunchedEffect(Unit) {
        viewModel.loadEvents(communityId)
    }

    val event = viewModel.getEventById(eventId)
    val context = LocalContext.current

    android.util.Log.d("EventDetailScreen", "eventId: $eventId, event: $event")

    if (event == null) {
        Text(
            "Event not found",
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column {

            Text(text = event.title, style = MaterialTheme.typography.headlineMedium)

            //I need to add event image
            Image(
                painter = rememberAsyncImagePainter(model = event.imageUri),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
            )

            Text(text = event.description, style = MaterialTheme.typography.bodyMedium)
            Text(text = "Date: ${event.date}", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                val geoUri = Uri.parse("google.navigation:q=${event.latitude},${event.longitude}")
                val intent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                    setPackage("com.google.android.apps.maps")
                }
                startActivity(context, intent, null)
            }) {
                Text("Provide Location")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // I need to change the back button
            Button(onClick = { navController.popBackStack() }) {
                Text("Back")
            }
        }
    }
}
