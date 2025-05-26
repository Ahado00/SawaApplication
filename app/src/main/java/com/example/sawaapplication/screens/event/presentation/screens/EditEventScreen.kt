package com.example.sawaapplication.screens.event.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.sawaapplication.screens.event.presentation.vmModels.CreateEventViewModel
import com.example.sawaapplication.screens.event.presentation.vmModels.EventViewModel

@Composable
fun EditEventScreen(
    eventId: String,
    communityId: String,
    navController: NavHostController,
    viewModel: CreateEventViewModel = hiltViewModel(),
    fetchEventViewModel: EventViewModel = hiltViewModel()
) {
    // Use state to hold the fetched event
    val eventState = fetchEventViewModel.event.collectAsState()

    // Fetch the event by ID when screen starts
    LaunchedEffect(eventId) {
        fetchEventViewModel.fetchEventById(communityId, eventId)?.let { event ->
            viewModel.loadEventForEdit(event)
            viewModel.communityId = communityId
        }
    }

    CreateNewEventScreen(
        navController = navController,
        communityId = communityId,
        isEditMode = true,
        eventToEdit = eventState.value, // Pass the loaded event
        onUpdateClick = { viewModel.updateEvent(eventId) }
    )
}
